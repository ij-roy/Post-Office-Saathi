package roy.ij.postofficesaathi.domain.calculator

import java.time.LocalDate

enum class SchemeType(
    val displayName: String,
    val shortName: String,
    val onHome: Boolean = true
) {
    RD("Recurring Deposit", "RD"),
    TD("Time Deposit", "TD"),
    MIS("Monthly Income Scheme", "MIS"),
    NSC("National Savings Certificate", "NSC"),
    KVP("Kisan Vikas Patra", "KVP"),
    PPF("Public Provident Fund", "PPF"),
    SSY("Sukanya Samriddhi Yojana", "SSY"),
    SCSS("Senior Citizens Savings Scheme", "SCSS"),
    SB("Savings Account", "SB"),
    SIMPLE_INTEREST("Custom Calculator", "SI"),
    COMPOUND_INTEREST("Custom Calculator", "CI"),
    MSSC("Mahila Samman Savings Certificate", "MSSC"),
    RD_REBATE("RD Rebate", "RD Rebate", onHome = false),
    PMI("Post Maturity Interest", "PMI", onHome = false);

    companion object {
        fun fromRoute(value: String): SchemeType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RD
    }
}

enum class TDTenure(val jsonKey: String, val years: Int, val label: String) {
    OneYear("1Y", 1, "1 Year"),
    TwoYears("2Y", 2, "2 Years"),
    ThreeYears("3Y", 3, "3 Years"),
    FiveYears("5Y", 5, "5 Years")
}

enum class CompoundingFrequency(val label: String) {
    SIMPLE("Simple interest"),
    MONTHLY("Monthly payout"),
    QUARTERLY("Quarterly compounding"),
    ANNUAL("Annual compounding"),
    AT_MATURITY("At maturity")
}

enum class CustomCalculatorType {
    Simple,
    Compound
}

enum class CompoundFrequencyOption(val label: String, val periodsPerYear: Int) {
    Monthly("Monthly", 12),
    Quarterly("Quarterly", 4),
    HalfYearly("Half-yearly", 2),
    Annually("Annually", 1)
}

data class SchemeRate(
    val schemeType: SchemeType,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val rate: Double,
    val tdTenureRates: Map<String, Double> = emptyMap(),
    val compoundingFrequency: CompoundingFrequency
)

data class RateHistory(
    val version: String,
    val rates: List<SchemeRate>
) {
    fun earliestDateFor(schemeType: SchemeType): LocalDate? =
        rates.filter { it.schemeType == schemeType }.minOfOrNull { it.effectiveFrom }

    fun currentRateFor(schemeType: SchemeType): SchemeRate? =
        rates
            .filter { it.schemeType == schemeType }
            .maxWithOrNull(compareBy<SchemeRate> { it.effectiveTo == null }.thenBy { it.effectiveFrom })
}

data class RateLookupResult(
    val schemeType: SchemeType,
    val requestedDate: LocalDate,
    val ratePercent: Double,
    val compoundingFrequency: CompoundingFrequency,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val usedFallback: Boolean,
    val rateDatasetVersion: String
)

data class CalculatorInput(
    val schemeType: SchemeType,
    val amount: Double,
    val startDate: LocalDate,
    val ratePercent: Double,
    val compoundingFrequency: CompoundingFrequency,
    val tdTenure: TDTenure = TDTenure.FiveYears,
    val customType: CustomCalculatorType = CustomCalculatorType.Simple,
    val customYears: Double = 1.0,
    val compoundFrequencyOption: CompoundFrequencyOption = CompoundFrequencyOption.Annually,
    val installmentsPaid: Int = 60,
    val yearsCompleted: Int = 0,
    val girlsBirthDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val scssExtended: Boolean = false
)

data class CalculatorInputSummary(
    val schemeType: SchemeType = SchemeType.RD,
    val amount: Double = 0.0,
    val startDate: LocalDate = LocalDate.MIN,
    val toDate: LocalDate? = null,
    val installmentsPaid: Int = 0,
    val tdTenure: TDTenure = TDTenure.FiveYears,
    val customType: CustomCalculatorType = CustomCalculatorType.Simple,
    val customYears: Double = 0.0,
    val compoundFrequencyOption: CompoundFrequencyOption = CompoundFrequencyOption.Annually,
    val compoundingFrequency: CompoundingFrequency = CompoundingFrequency.SIMPLE,
    val scssExtended: Boolean = false
)

data class FYInterestRow(
    val financialYear: String,
    val interestAccrued: Double,
    val cumulativeTotal: Double
)

data class ContinuationProjection(
    val year: Int,
    val withDeposits: Double,
    val withoutDeposits: Double
)

data class CalculatorResult(
    val schemeType: SchemeType,
    val title: String,
    val ratePercent: Double,
    val rateLabel: String,
    val totalDeposited: Double,
    val interestEarned: Double,
    val maturityAmount: Double,
    val totalReceived: Double = maturityAmount,
    val maturityDate: LocalDate,
    val monthlyIncome: Double? = null,
    val fyWiseBreakdown: List<FYInterestRow> = emptyList(),
    val continuationProjections: Map<Int, ContinuationProjection> = emptyMap(),
    val notes: List<String> = emptyList(),
    val inputSummary: CalculatorInputSummary = CalculatorInputSummary()
)
