package roy.ij.postofficesaathi.ui.calculator.scheme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsSanitizer
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.calculator.GitHubRatesRepository
import roy.ij.postofficesaathi.data.calculator.RatesRepository
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.CompoundFrequencyOption
import roy.ij.postofficesaathi.domain.calculator.CustomCalculatorType
import roy.ij.postofficesaathi.domain.calculator.InterestEngine
import roy.ij.postofficesaathi.domain.calculator.RateHistory
import roy.ij.postofficesaathi.domain.calculator.RateLookupResult
import roy.ij.postofficesaathi.domain.calculator.SchemeRateResolver
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure

data class SchemeCalculatorUiState(
    val schemeType: SchemeType,
    val title: String = schemeType.displayName,
    val amount: String = "",
    val rateOverride: String = "",
    val isRateOverridden: Boolean = false,
    val startDate: LocalDate = LocalDate.now(),
    val minDate: LocalDate? = null,
    val toDate: LocalDate = LocalDate.now().plusYears(1),
    val tdTenure: TDTenure = TDTenure.FiveYears,
    val customType: CustomCalculatorType = CustomCalculatorType.Simple,
    val customYears: String = "1",
    val compoundFrequencyOption: CompoundFrequencyOption = CompoundFrequencyOption.Annually,
    val installmentsPaid: String = "60",
    val yearsCompleted: String = "0",
    val girlsBirthDate: LocalDate? = null,
    val scssExtended: Boolean = false,
    val officialRate: RateLookupResult? = null,
    val rateVersion: String? = null,
    val isLoading: Boolean = true,
    val isCalculating: Boolean = false,
    val pendingResult: CalculatorResult? = null,
    val errors: Map<String, String> = emptyMap(),
    val message: String? = null,
    val messageId: Long = 0L
) {
    val activeRatePercent: Double?
        get() = if (isRateOverridden) rateOverride.toDoubleOrNull() else officialRate?.ratePercent
}

sealed interface SchemeCalculatorExternalAction {
    data class ShowResult(val result: CalculatorResult) : SchemeCalculatorExternalAction
}

class SchemeCalculatorViewModel(
    private val schemeType: SchemeType,
    initialAmount: Double?,
    private val ratesRepository: RatesRepository,
    private val analytics: SaathiAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SchemeCalculatorUiState(
            schemeType = schemeType,
            amount = initialAmount?.takeIf { it > 0.0 }?.toLong()?.toString().orEmpty(),
            isRateOverridden = schemeType == SchemeType.SIMPLE_INTEREST || schemeType == SchemeType.COMPOUND_INTEREST,
            rateOverride = if (schemeType == SchemeType.SIMPLE_INTEREST || schemeType == SchemeType.COMPOUND_INTEREST) "7.5" else ""
        )
    )
    val uiState: StateFlow<SchemeCalculatorUiState> = _uiState.asStateFlow()

    private val _externalActions = MutableSharedFlow<SchemeCalculatorExternalAction>()
    val externalActions: SharedFlow<SchemeCalculatorExternalAction> = _externalActions.asSharedFlow()

    private var history: RateHistory? = null

    init {
        loadRates()
    }

    fun loadRates() {
        viewModelScope.launch {
            if (schemeType == SchemeType.SIMPLE_INTEREST || schemeType == SchemeType.COMPOUND_INTEREST) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            val loadResult = ratesRepository.loadRates()
            history = loadResult.history
            val minDate = loadResult.history.earliestDateFor(schemeType)
            _uiState.update { state ->
                val safeDate = minDate?.let { if (state.startDate.isBefore(it)) it else state.startDate } ?: state.startDate
                state.copy(
                    minDate = minDate,
                    startDate = safeDate,
                    rateVersion = loadResult.history.version,
                    isLoading = false
                )
            }
            resolveOfficialRate()
        }
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, errors = it.errors - FieldAmount) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { state ->
            val minDate = state.minDate
            val safeDate = if (minDate != null && date.isBefore(minDate)) minDate else date
            state.copy(startDate = safeDate, errors = state.errors - FieldDate)
        }
        resolveOfficialRate()
    }

    fun updateToDate(date: LocalDate) {
        _uiState.update {
            if (it.schemeType == SchemeType.RD) {
                val months = ChronoUnit.MONTHS.between(it.startDate, date).toInt().coerceIn(0, 60)
                it.copy(toDate = date, installmentsPaid = months.toString(), errors = it.errors - FieldInstallments)
            } else {
                it.copy(toDate = date)
            }
        }
    }

    fun updateTdTenure(tenure: TDTenure) {
        _uiState.update { it.copy(tdTenure = tenure) }
        analytics.logEvent(
            AnalyticsEvent.TDTenureChanged,
            mapOf(
                AnalyticsParam.Flow to AnalyticsFlow.Calculator,
                AnalyticsParam.SchemeType to SchemeType.TD.name,
                AnalyticsParam.TDTenure to tenure.jsonKey
            )
        )
        resolveOfficialRate()
    }

    fun updateCustomType(type: CustomCalculatorType) {
        _uiState.update {
            it.copy(
                customType = type,
                schemeType = if (type == CustomCalculatorType.Simple) SchemeType.SIMPLE_INTEREST else SchemeType.COMPOUND_INTEREST
            )
        }
    }

    fun updateRateOverride(value: String) {
        _uiState.update {
            it.copy(
                rateOverride = value.filter { char -> char.isDigit() || char == '.' },
                errors = it.errors - FieldRate
            )
        }
    }

    fun enableRateOverride() {
        val official = _uiState.value.officialRate?.ratePercent?.toString().orEmpty()
        _uiState.update { it.copy(isRateOverridden = true, rateOverride = official) }
    }

    fun resetRateOverride() {
        _uiState.update { it.copy(isRateOverridden = false, rateOverride = "") }
    }

    fun updateInstallmentsPaid(value: String) {
        _uiState.update { it.copy(installmentsPaid = value.filter(Char::isDigit), errors = it.errors - FieldInstallments) }
    }

    fun updateYearsCompleted(value: String) {
        _uiState.update { it.copy(yearsCompleted = value.filter(Char::isDigit)) }
    }

    fun updateCustomYears(value: String) {
        _uiState.update { it.copy(customYears = value.filter { char -> char.isDigit() || char == '.' }, errors = it.errors - FieldYears) }
    }

    fun updateCompoundFrequency(option: CompoundFrequencyOption) {
        _uiState.update { it.copy(compoundFrequencyOption = option) }
    }

    fun updateScssExtended(extended: Boolean) {
        _uiState.update { it.copy(scssExtended = extended) }
    }

    fun calculate() {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isCalculating = true) }
            val state = _uiState.value
            val errors = validate(state)
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(errors = errors, isCalculating = false) }
                return@launch
            }
            val rate = state.activeRatePercent ?: 0.0
            val input = CalculatorInput(
                schemeType = effectiveSchemeType(state),
                amount = state.amount.toDouble(),
                startDate = state.startDate,
                ratePercent = rate,
                compoundingFrequency = state.officialRate?.compoundingFrequency ?: defaultFrequency(state),
                tdTenure = state.tdTenure,
                customType = state.customType,
                customYears = state.customYears.toDoubleOrNull() ?: 1.0,
                compoundFrequencyOption = state.compoundFrequencyOption,
                installmentsPaid = state.installmentsPaid.toIntOrNull() ?: 60,
                yearsCompleted = state.yearsCompleted.toIntOrNull() ?: 0,
                girlsBirthDate = state.girlsBirthDate,
                toDate = state.toDate,
                scssExtended = state.scssExtended
            )
            val result = InterestEngine.calculate(input)
            logCalculation(state, result)
            if (state.officialRate?.usedFallback == true && !state.isRateOverridden) {
                logRateFallback(state.officialRate)
                showMessage("Rate unavailable for this date. Using current rate.")
            }
            _uiState.update { it.copy(pendingResult = result) }
        }
    }

    fun onCalculationAnimationComplete() {
        val result = _uiState.value.pendingResult ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isCalculating = false, pendingResult = null) }
            _externalActions.emit(SchemeCalculatorExternalAction.ShowResult(result))
        }
    }

    private fun resolveOfficialRate() {
        if (schemeType == SchemeType.SIMPLE_INTEREST || schemeType == SchemeType.COMPOUND_INTEREST) return
        val localHistory = history ?: return
        val state = _uiState.value
        val lookup = SchemeRateResolver.resolve(localHistory, schemeType, state.startDate, state.tdTenure)
        _uiState.update { it.copy(officialRate = lookup) }
    }

    private fun validate(state: SchemeCalculatorUiState): Map<String, String> = buildMap {
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0.0) put(FieldAmount, "Please enter a valid amount.")
        val rate = state.activeRatePercent
        if (rate == null || rate <= 0.0) put(FieldRate, "Please enter a valid rate.")
        state.minDate?.let {
            if (state.startDate.isBefore(it)) put(FieldDate, "No rate data available before ${it}.")
        }
        if (state.schemeType == SchemeType.RD && (state.installmentsPaid.toIntOrNull() ?: 0) > 60) {
            put(FieldInstallments, "Installments paid cannot exceed total installments.")
        }
        if (state.schemeType == SchemeType.MSSC && state.startDate.isAfter(LocalDate.parse("2023-09-30"))) {
            put(FieldDate, "MSSC was discontinued on 30 Sept 2023. Please select an earlier start date.")
        }
        if (state.customYears.toDoubleOrNull() == null || (state.customYears.toDoubleOrNull() ?: 0.0) <= 0.0) {
            if (state.schemeType == SchemeType.SIMPLE_INTEREST || state.schemeType == SchemeType.COMPOUND_INTEREST) {
                put(FieldYears, "Please enter a valid time period.")
            }
        }
    }

    private fun effectiveSchemeType(state: SchemeCalculatorUiState): SchemeType =
        when {
            schemeType == SchemeType.SIMPLE_INTEREST && state.customType == CustomCalculatorType.Compound -> SchemeType.COMPOUND_INTEREST
            schemeType == SchemeType.COMPOUND_INTEREST && state.customType == CustomCalculatorType.Simple -> SchemeType.SIMPLE_INTEREST
            else -> schemeType
        }

    private fun defaultFrequency(state: SchemeCalculatorUiState): CompoundingFrequency =
        if (state.customType == CustomCalculatorType.Simple) CompoundingFrequency.SIMPLE else CompoundingFrequency.QUARTERLY

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message, messageId = it.messageId + 1L) }
    }

    private fun logCalculation(state: SchemeCalculatorUiState, result: CalculatorResult) {
        analytics.logEvent(
            AnalyticsEvent.CalculationPerformed,
            mapOf(
                AnalyticsParam.Flow to AnalyticsFlow.Calculator,
                AnalyticsParam.SchemeType to result.schemeType.name,
                AnalyticsParam.TDTenure to state.tdTenure.jsonKey.takeIf { state.schemeType == SchemeType.TD },
                AnalyticsParam.InvestmentAmountBucket to AnalyticsSanitizer.amountBucket(result.totalDeposited),
                AnalyticsParam.TenureBucket to AnalyticsSanitizer.tenureBucket(state.customYears.toDoubleOrNull() ?: state.tdTenure.years.toDouble()),
                AnalyticsParam.RatesVersion to state.rateVersion,
                AnalyticsParam.UsedFallback to (state.officialRate?.usedFallback == true)
            )
        )
    }

    private fun logRateFallback(lookup: RateLookupResult?) {
        if (lookup == null) return
        analytics.logEvent(
            AnalyticsEvent.CalculatorRateFallbackUsed,
            mapOf(
                AnalyticsParam.SchemeType to lookup.schemeType.name,
                AnalyticsParam.InputYear to lookup.requestedDate.year,
                AnalyticsParam.InputMonth to lookup.requestedDate.monthValue,
                AnalyticsParam.RatesVersion to lookup.rateDatasetVersion,
                AnalyticsParam.FallbackRate to lookup.ratePercent,
                AnalyticsParam.FallbackEffectiveFrom to lookup.effectiveFrom.toString()
            )
        )
    }

    class Factory(
        private val context: Context,
        private val schemeType: SchemeType,
        private val initialAmount: Double?,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SchemeCalculatorViewModel(
                schemeType = schemeType,
                initialAmount = initialAmount,
                ratesRepository = GitHubRatesRepository(context.applicationContext),
                analytics = analytics
            ) as T
    }

    private companion object {
        const val FieldAmount = "amount"
        const val FieldRate = "rate"
        const val FieldDate = "date"
        const val FieldInstallments = "installments"
        const val FieldYears = "years"
    }
}
