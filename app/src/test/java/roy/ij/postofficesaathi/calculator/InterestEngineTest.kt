package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.CompoundFrequencyOption
import roy.ij.postofficesaathi.domain.calculator.CustomCalculatorType
import roy.ij.postofficesaathi.domain.calculator.InterestEngine
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure

class InterestEngineTest {
    @Test
    fun timeDepositUsesIndiaPostAnnualPayoutMaturityForSelectedTenure() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.TD,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 7.5,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                tdTenure = TDTenure.FiveYears
            )
        )

        assertEquals(SchemeType.TD, result.schemeType)
        assertEquals(100_000.0, result.totalDeposited, 0.001)
        assertEquals(138_567.93, result.maturityAmount, 0.01)
        assertEquals(LocalDate.parse("2031-04-01"), result.maturityDate)
    }

    @Test
    fun timeDepositMatchesIndiaPostMaturityTableValues() {
        val cases = listOf(
            TDTenure.OneYear to Triple(6.9, 1, 107_081.0),
            TDTenure.TwoYears to Triple(7.0, 2, 114_372.0),
            TDTenure.ThreeYears to Triple(7.1, 3, 121_874.0),
            TDTenure.FiveYears to Triple(7.5, 5, 138_568.0)
        )

        cases.forEach { (tenure, values) ->
            val (rate, _, expectedRoundedMaturity) = values
            val result = InterestEngine.calculate(
                CalculatorInput(
                    schemeType = SchemeType.TD,
                    amount = 100_000.0,
                    startDate = LocalDate.parse("2026-07-01"),
                    ratePercent = rate,
                    compoundingFrequency = CompoundingFrequency.QUARTERLY,
                    tdTenure = tenure
                )
            )

            assertEquals("Unexpected maturity for ${tenure.label}", expectedRoundedMaturity, kotlin.math.round(result.maturityAmount), 0.001)
        }
    }

    @Test
    fun recurringDepositResultCarriesInputContext() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.RD,
                amount = 500.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 6.7,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                installmentsPaid = 60
            )
        )

        assertEquals(500.0, result.inputSummary.amount, 0.001)
        assertEquals(60, result.inputSummary.installmentsPaid)
        assertEquals(LocalDate.parse("2026-07-01"), result.inputSummary.startDate)
    }

    @Test
    fun recurringDepositMatchesIndiaPostQuarterlyFormula() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.RD,
                amount = 100.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 6.7,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                installmentsPaid = 60
            )
        )

        assertEquals(6_000.0, result.totalDeposited, 0.001)
        assertEquals(7_136.58, result.maturityAmount, 0.01)
        assertEquals(1_136.58, result.interestEarned, 0.01)
    }

    @Test
    fun monthlyIncomeSchemeShowsMonthlyPayoutAndPrincipalAtMaturity() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.MIS,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 7.4,
                compoundingFrequency = CompoundingFrequency.MONTHLY
            )
        )

        assertEquals(100_000.0, result.maturityAmount, 0.001)
        assertEquals(616.67, result.monthlyIncome ?: 0.0, 0.01)
        assertEquals(37_000.0, result.interestEarned, 0.01)
        assertEquals(137_000.0, result.totalReceived, 0.01)
        assertEquals(LocalDate.parse("2031-04-01"), result.maturityDate)
    }

    @Test
    fun seniorCitizenSavingsShowsQuarterlyPayoutAndTotalReceived() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.SCSS,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 8.2,
                compoundingFrequency = CompoundingFrequency.QUARTERLY
            )
        )

        assertEquals(100_000.0, result.maturityAmount, 0.001)
        assertEquals(2_050.0, result.monthlyIncome ?: 0.0, 0.01)
        assertEquals(41_000.0, result.interestEarned, 0.01)
        assertEquals(141_000.0, result.totalReceived, 0.01)
    }

    @Test
    fun customCompoundCalculatorUsesSelectedFrequency() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.COMPOUND_INTEREST,
                amount = 50_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 8.0,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                customType = CustomCalculatorType.Compound,
                customYears = 3.0,
                compoundFrequencyOption = CompoundFrequencyOption.Quarterly
            )
        )

        assertEquals(63_412.09, result.maturityAmount, 0.01)
        assertEquals(LocalDate.parse("2029-04-01"), result.maturityDate)
    }
}
