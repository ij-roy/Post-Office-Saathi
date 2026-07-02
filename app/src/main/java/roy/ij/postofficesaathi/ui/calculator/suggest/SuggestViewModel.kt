package roy.ij.postofficesaathi.ui.calculator.suggest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsSanitizer
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.agent.Agent
import roy.ij.postofficesaathi.data.agent.AgentRepository
import roy.ij.postofficesaathi.data.agent.GitHubAgentRepository
import roy.ij.postofficesaathi.data.calculator.GitHubRatesRepository
import roy.ij.postofficesaathi.data.calculator.RatesRepository
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.InterestEngine
import roy.ij.postofficesaathi.domain.calculator.SchemeRateResolver
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure

data class SuggestUiState(
    val amount: String = "",
    val pincode: String = "",
    val isSuggesting: Boolean = false,
    val isSearchingAgents: Boolean = false,
    val suggestions: List<PlanSuggestionUi> = emptyList(),
    val agents: List<Agent> = emptyList(),
    val agentMessage: String? = null,
    val amountError: String? = null,
    val pincodeError: String? = null
)

data class PlanSuggestionUi(
    val schemeType: SchemeType,
    val title: String,
    val maturityAmount: Double,
    val ratePercent: Double,
    val result: CalculatorResult
)

class SuggestViewModel(
    private val ratesRepository: RatesRepository,
    private val agentRepository: AgentRepository,
    private val analytics: SaathiAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow(SuggestUiState())
    val uiState: StateFlow<SuggestUiState> = _uiState.asStateFlow()

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }, amountError = null) }
    }

    fun updatePincode(value: String) {
        _uiState.update { it.copy(pincode = value.filter(Char::isDigit).take(6), pincodeError = null) }
    }

    fun suggestPlans() {
        viewModelScope.launch(Dispatchers.Default) {
            val amount = _uiState.value.amount.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                _uiState.update { it.copy(amountError = "Please enter a valid amount.") }
                return@launch
            }
            _uiState.update { it.copy(isSuggesting = true, amountError = null) }
            val history = ratesRepository.loadRates().history
            val date = LocalDate.now()
            val schemes = listOf(SchemeType.TD, SchemeType.NSC, SchemeType.KVP, SchemeType.MIS, SchemeType.SCSS, SchemeType.MSSC)
            val suggestions = schemes.mapNotNull { scheme ->
                runCatching {
                    val lookup = SchemeRateResolver.resolve(history, scheme, date, TDTenure.FiveYears)
                    val input = CalculatorInput(
                        schemeType = scheme,
                        amount = amount,
                        startDate = date,
                        ratePercent = lookup.ratePercent,
                        compoundingFrequency = lookup.compoundingFrequency,
                        tdTenure = TDTenure.FiveYears
                    )
                    val result = InterestEngine.calculate(input)
                    PlanSuggestionUi(
                        schemeType = scheme,
                        title = if (scheme == SchemeType.TD) "Time Deposit 5 Years" else scheme.displayName,
                        maturityAmount = result.totalReceived,
                        ratePercent = lookup.ratePercent,
                        result = result
                    )
                }.getOrNull()
            }.sortedByDescending { it.maturityAmount }
            analytics.logEvent(
                AnalyticsEvent.PlanSuggested,
                mapOf(
                    AnalyticsParam.Flow to AnalyticsFlow.Calculator,
                    AnalyticsParam.InvestmentAmountBucket to AnalyticsSanitizer.amountBucket(amount),
                    AnalyticsParam.ResultCountBucket to AnalyticsSanitizer.countBucket(suggestions.size)
                )
            )
            _uiState.update { it.copy(isSuggesting = false, suggestions = suggestions) }
        }
    }

    fun searchAgents() {
        viewModelScope.launch {
            val pincode = _uiState.value.pincode
            if (pincode.length != 6) {
                _uiState.update { it.copy(pincodeError = "Enter a 6 digit pincode.") }
                return@launch
            }
            _uiState.update { it.copy(isSearchingAgents = true, pincodeError = null, agentMessage = null) }
            val result = agentRepository.searchByPincode(pincode)
            analytics.logEvent(
                AnalyticsEvent.AgentSearchPerformed,
                mapOf(
                    AnalyticsParam.Flow to AnalyticsFlow.Calculator,
                    AnalyticsParam.PincodePrefix to AnalyticsSanitizer.pincodePrefix(pincode),
                    AnalyticsParam.ResultCountBucket to AnalyticsSanitizer.countBucket(result.agents.size),
                    AnalyticsParam.ErrorType to result.errorMessage
                )
            )
            _uiState.update {
                it.copy(
                    isSearchingAgents = false,
                    agents = result.agents,
                    agentMessage = result.errorMessage ?: if (result.agents.isEmpty()) "No agents listed for this pincode yet." else null
                )
            }
        }
    }

    fun logAgentContact(agent: Agent, type: String) {
        analytics.logEvent(
            AnalyticsEvent.AgentContacted,
            mapOf(
                AnalyticsParam.AgentId to agent.id,
                AnalyticsParam.AgentContactType to type,
                AnalyticsParam.PincodePrefix to AnalyticsSanitizer.pincodePrefix(agent.pincode)
            )
        )
    }

    fun logPlanDetailOpened(suggestion: PlanSuggestionUi) {
        analytics.logEvent(
            AnalyticsEvent.PlanDetailOpened,
            mapOf(
                AnalyticsParam.SchemeType to suggestion.schemeType.name,
                AnalyticsParam.InvestmentAmountBucket to AnalyticsSanitizer.amountBucket(suggestion.result.totalDeposited)
            )
        )
    }

    class Factory(
        private val context: Context,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SuggestViewModel(
                ratesRepository = GitHubRatesRepository(context.applicationContext),
                agentRepository = GitHubAgentRepository(),
                analytics = analytics
            ) as T
    }
}
