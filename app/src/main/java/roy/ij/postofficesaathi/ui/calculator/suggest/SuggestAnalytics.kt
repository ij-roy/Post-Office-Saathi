package roy.ij.postofficesaathi.ui.calculator.suggest

import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.data.agent.Agent
import roy.ij.postofficesaathi.domain.calculator.SchemeType

fun planSuggestedParams(
    investmentAmount: Double,
    resultCount: Int,
    topScheme: SchemeType?,
    topMaturityAmount: Double?
): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.InvestmentAmount to investmentAmount,
        AnalyticsParam.ResultCount to resultCount,
        AnalyticsParam.TopScheme to topScheme?.name,
        AnalyticsParam.TopMaturityAmount to topMaturityAmount
    )

fun planSuggestFailedParams(amountText: String, errorArea: String, throwable: Throwable? = null): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.InvestmentAmount to amountText,
        AnalyticsParam.ErrorArea to errorArea,
        AnalyticsParam.ErrorType to (throwable?.javaClass?.simpleName ?: "Validation")
    )

fun agentSearchParams(
    pincode: String,
    resultCount: Int,
    errorMessage: String?
): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.Pincode to pincode,
        AnalyticsParam.ResultCount to resultCount,
        AnalyticsParam.ErrorType to errorMessage
    )

fun agentContactParams(agent: Agent, type: String, throwable: Throwable? = null): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.AgentId to agent.id,
        AnalyticsParam.AgentContactType to type,
        AnalyticsParam.AgentPincode to agent.pincode,
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )

fun planDetailOpenedParams(suggestion: PlanSuggestionUi): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to suggestion.schemeType.name,
        AnalyticsParam.InvestmentAmount to suggestion.result.totalDeposited,
        AnalyticsParam.MaturityAmount to suggestion.result.maturityAmount
    )
