package roy.ij.postofficesaathi.ui.calculator.scheme

import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.SchemeType

fun calculatorOpenedParams(
    schemeType: SchemeType?,
    entryPoint: String,
    initialAmount: Double? = null,
    ratesVersion: String? = null,
    usedFallback: Boolean? = null
): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to schemeType?.name,
        AnalyticsParam.EntryPoint to entryPoint,
        AnalyticsParam.InitialAmount to initialAmount,
        AnalyticsParam.RatesVersion to ratesVersion,
        AnalyticsParam.UsedFallback to usedFallback
    )

fun calculatorInputChangedParams(
    schemeType: SchemeType,
    fieldName: String,
    fieldValue: Any?,
    ratesVersion: String? = null
): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to schemeType.name,
        AnalyticsParam.FieldName to fieldName,
        AnalyticsParam.FieldValue to fieldValue,
        AnalyticsParam.RatesVersion to ratesVersion
    )

fun calculationSucceededParams(
    input: CalculatorInput,
    result: CalculatorResult,
    ratesVersion: String?,
    usedFallback: Boolean
): Map<String, Any?> =
    calculatorInputParams(input) + calculatorResultParams(result) + mapOf(
        AnalyticsParam.RatesVersion to ratesVersion,
        AnalyticsParam.UsedFallback to usedFallback
    )

fun calculationFailedParams(
    state: SchemeCalculatorUiState,
    errors: Map<String, String>
): Map<String, Any?> =
    calculatorStateParams(state) + mapOf(
        AnalyticsParam.ErrorArea to "calculator_validation",
        AnalyticsParam.ErrorType to "Validation",
        AnalyticsParam.ErrorFields to errors.keys.sorted().joinToString(",")
    )

fun resultShareParams(result: CalculatorResult, shareChannel: String, throwable: Throwable? = null): Map<String, Any?> =
    calculatorResultParams(result) + mapOf(
        AnalyticsParam.ShareChannel to shareChannel,
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )

private fun calculatorInputParams(input: CalculatorInput): Map<String, Any?> =
    commonInputParams(input) + schemeInputParams(input)

private fun commonInputParams(input: CalculatorInput): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to input.schemeType.name,
        AnalyticsParam.Amount to input.amount,
        AnalyticsParam.InterestRate to input.ratePercent,
        AnalyticsParam.StartDate to input.startDate.toString(),
        AnalyticsParam.ToDate to input.toDate?.toString(),
        AnalyticsParam.CompoundingFrequency to input.compoundingFrequency.name
    )

private fun schemeInputParams(input: CalculatorInput): Map<String, Any?> =
    when (input.schemeType) {
        SchemeType.RD -> mapOf(
            AnalyticsParam.MonthlyDeposit to input.amount,
            AnalyticsParam.InstallmentsPaid to input.installmentsPaid
        )
        SchemeType.TD -> mapOf(
            AnalyticsParam.DepositAmount to input.amount,
            AnalyticsParam.TDTenure to input.tdTenure.jsonKey
        )
        SchemeType.MIS,
        SchemeType.NSC,
        SchemeType.KVP,
        SchemeType.SCSS,
        SchemeType.MSSC -> mapOf(
            AnalyticsParam.DepositAmount to input.amount,
            AnalyticsParam.ScssExtended to input.scssExtended.takeIf { input.schemeType == SchemeType.SCSS }
        )
        SchemeType.PPF,
        SchemeType.SSY -> mapOf(
            AnalyticsParam.YearlyDeposit to input.amount,
            AnalyticsParam.YearsCompleted to input.yearsCompleted
        )
        SchemeType.SB -> mapOf(
            AnalyticsParam.BalanceAmount to input.amount,
            AnalyticsParam.ToDate to input.toDate?.toString()
        )
        SchemeType.SIMPLE_INTEREST,
        SchemeType.COMPOUND_INTEREST,
        SchemeType.PMI -> mapOf(
            AnalyticsParam.PrincipalAmount to input.amount,
            AnalyticsParam.CustomType to input.customType.name,
            AnalyticsParam.CustomYears to input.customYears,
            AnalyticsParam.CompoundFrequency to input.compoundFrequencyOption.name
                .takeIf { input.schemeType == SchemeType.COMPOUND_INTEREST }
        )
        SchemeType.RD_REBATE -> mapOf(
            AnalyticsParam.MonthlyDeposit to input.amount,
            AnalyticsParam.InstallmentsPaid to input.installmentsPaid
        )
    }

private fun calculatorResultParams(result: CalculatorResult): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to result.schemeType.name,
        AnalyticsParam.InterestRate to result.ratePercent,
        AnalyticsParam.TotalDeposited to result.totalDeposited,
        AnalyticsParam.InterestEarned to result.interestEarned,
        AnalyticsParam.MaturityAmount to result.maturityAmount,
        AnalyticsParam.TotalReceived to result.totalReceived,
        AnalyticsParam.MonthlyIncome to result.monthlyIncome,
        AnalyticsParam.MaturityDate to result.maturityDate.toString()
    )

private fun calculatorStateParams(state: SchemeCalculatorUiState): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Calculator,
        AnalyticsParam.SchemeType to state.schemeType.name,
        AnalyticsParam.Amount to state.amount,
        AnalyticsParam.InterestRate to if (state.isRateOverridden) state.rateOverride else state.activeRatePercent,
        AnalyticsParam.StartDate to state.startDate.toString(),
        AnalyticsParam.ToDate to state.toDate.toString(),
        AnalyticsParam.TDTenure to state.tdTenure.jsonKey.takeIf { state.schemeType == SchemeType.TD },
        AnalyticsParam.CustomType to state.customType.name
            .takeIf { state.schemeType == SchemeType.SIMPLE_INTEREST || state.schemeType == SchemeType.COMPOUND_INTEREST },
        AnalyticsParam.CustomYears to state.customYears
            .takeIf { state.schemeType == SchemeType.SIMPLE_INTEREST || state.schemeType == SchemeType.COMPOUND_INTEREST },
        AnalyticsParam.CompoundFrequency to state.compoundFrequencyOption.name
            .takeIf { state.schemeType == SchemeType.COMPOUND_INTEREST },
        AnalyticsParam.InstallmentsPaid to state.installmentsPaid.takeIf { state.schemeType == SchemeType.RD },
        AnalyticsParam.YearsCompleted to state.yearsCompleted
            .takeIf { state.schemeType == SchemeType.PPF || state.schemeType == SchemeType.SSY },
        AnalyticsParam.ScssExtended to state.scssExtended.takeIf { state.schemeType == SchemeType.SCSS },
        AnalyticsParam.RatesVersion to state.rateVersion,
        AnalyticsParam.UsedFallback to (state.officialRate?.usedFallback == true)
    )
