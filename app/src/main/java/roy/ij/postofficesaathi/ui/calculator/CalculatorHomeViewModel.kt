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
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.calculator.GitHubRatesRepository
import roy.ij.postofficesaathi.data.calculator.RatesRepository
import roy.ij.postofficesaathi.domain.calculator.RateHistory
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
    val description: String,
    val rateLabel: String,
    val isDiscontinued: Boolean = false
)

object CalculatorHomeCardMapper {
    fun cardFor(scheme: SchemeType, history: RateHistory): CalculatorSchemeCardUi {
        if (scheme == SchemeType.SIMPLE_INTEREST) {
            return CalculatorSchemeCardUi(
                schemeType = scheme,
                title = "Custom",
                description = "Simple or compound interest",
                rateLabel = ""
            )
        }

        val lookup = SchemeRateResolver.resolve(
            history = history,
            schemeType = scheme,
            date = LocalDate.now(),
            tdTenure = TDTenure.FiveYears
        )
        return CalculatorSchemeCardUi(
            schemeType = scheme,
            title = scheme.shortName,
            description = scheme.displayName,
            rateLabel = "${lookup.ratePercent}%",
            isDiscontinued = scheme == SchemeType.MSSC
        )
    }
}

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
            val cards = HomeSchemes.map { scheme -> CalculatorHomeCardMapper.cardFor(scheme, result.history) }
            val pendingToast = ratesRepository.pendingRateUpdateToast()
            if (pendingToast != null) ratesRepository.clearPendingRateUpdateToast()
            analytics.logEvent(
                AnalyticsEvent.CalculatorOpened,
                mapOf(
                    AnalyticsParam.Flow to AnalyticsFlow.Calculator,
                    AnalyticsParam.EntryPoint to "home"
                )
            )
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
