package roy.ij.postofficesaathi.ui.calculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.calculator.GitHubRatesRepository
import roy.ij.postofficesaathi.data.calculator.RatesRepository
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.SchemeRateResolver
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure

data class CalculatorHomeUiState(
    val isLoading: Boolean = true,
    val cards: List<CalculatorSchemeCardUi> = emptyList(),
    val message: String? = null,
    val messageId: Long = 0L
)

data class CalculatorSchemeCardUi(
    val schemeType: SchemeType,
    val title: String,
    val subtitle: String,
    val rateLabel: String,
    val compoundingLabel: String,
    val isDiscontinued: Boolean = false
)

class CalculatorHomeViewModel(
    private val ratesRepository: RatesRepository,
    private val analytics: SaathiAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorHomeUiState())
    val uiState: StateFlow<CalculatorHomeUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = ratesRepository.loadRates()
            val cards = HomeSchemes.map { scheme ->
                if (scheme == SchemeType.SIMPLE_INTEREST) {
                    CalculatorSchemeCardUi(
                        schemeType = scheme,
                        title = "Custom Calculator",
                        subtitle = "Simple or compound interest",
                        rateLabel = "Your rate",
                        compoundingLabel = "Flexible"
                    )
                } else {
                    val lookup = SchemeRateResolver.resolve(
                        history = result.history,
                        schemeType = scheme,
                        date = LocalDate.now(),
                        tdTenure = TDTenure.FiveYears
                    )
                    CalculatorSchemeCardUi(
                        schemeType = scheme,
                        title = scheme.displayName,
                        subtitle = scheme.shortName,
                        rateLabel = "${lookup.ratePercent}% p.a.",
                        compoundingLabel = lookup.compoundingFrequency.toDisplayLabel(),
                        isDiscontinued = scheme == SchemeType.MSSC
                    )
                }
            }
            val pendingToast = ratesRepository.pendingRateUpdateToast()
            if (pendingToast != null) ratesRepository.clearPendingRateUpdateToast()
            analytics.logEvent(AnalyticsEvent.CalculatorOpened)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cards = cards,
                    message = pendingToast,
                    messageId = if (pendingToast != null) it.messageId + 1 else it.messageId
                )
            }
        }
    }

    fun onSchemeSelected(schemeType: SchemeType) {
        analytics.logEvent(
            AnalyticsEvent.SchemeSelected,
            mapOf(AnalyticsParam.SchemeType to schemeType.name)
        )
    }

    class Factory(
        private val context: Context,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalculatorHomeViewModel(GitHubRatesRepository(context.applicationContext), analytics) as T
    }
}

val HomeSchemes = listOf(
    SchemeType.RD,
    SchemeType.TD,
    SchemeType.MIS,
    SchemeType.NSC,
    SchemeType.KVP,
    SchemeType.PPF,
    SchemeType.SSY,
    SchemeType.SCSS,
    SchemeType.SB,
    SchemeType.SIMPLE_INTEREST,
    SchemeType.MSSC
)

private fun CompoundingFrequency.toDisplayLabel(): String =
    when (this) {
        CompoundingFrequency.SIMPLE -> "Simple interest"
        CompoundingFrequency.MONTHLY -> "Monthly payout"
        CompoundingFrequency.QUARTERLY -> "Quarterly compounding"
        CompoundingFrequency.ANNUAL -> "Annual compounding"
        CompoundingFrequency.AT_MATURITY -> "At maturity"
    }

