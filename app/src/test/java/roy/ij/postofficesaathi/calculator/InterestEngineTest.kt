package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun recurringDepositYearWiseBreakdownUsesQuarterlyAccrualNotEqualSplit() {
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

        assertEquals(
            listOf("2026-27", "2027-28", "2028-29", "2029-30", "2030-31", "2031-32"),
            result.fyWiseBreakdown.map { it.financialYear }
        )
        assertMoneyRows(
            listOf(25.36, 107.78, 197.62, 293.64, 396.26, 115.92),
            result.fyWiseBreakdown.map { it.interestAccrued }
        )
        assertTrue(result.fyWiseBreakdown.map { it.interestAccrued }.distinct().size > 1)
        assertBreakdownSumsToInterest(result)
    }

    @Test
    fun timeDepositYearWiseBreakdownUsesAnnualPayoutEvents() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.TD,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 7.5,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                tdTenure = TDTenure.FiveYears
            )
        )

        assertEquals(
            listOf("2027-28", "2028-29", "2029-30", "2030-31", "2031-32"),
            result.fyWiseBreakdown.map { it.financialYear }
        )
        assertMoneyRows(
            listOf(7_713.59, 7_713.59, 7_713.59, 7_713.59, 7_713.57),
            result.fyWiseBreakdown.map { it.interestAccrued }
        )
        assertBreakdownSumsToInterest(result)
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
    fun monthlyIncomeYearWiseBreakdownUsesMonthlyPayoutEvents() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.MIS,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 7.4,
                compoundingFrequency = CompoundingFrequency.MONTHLY
            )
        )

        assertEquals(
            listOf("2026-27", "2027-28", "2028-29", "2029-30", "2030-31", "2031-32"),
            result.fyWiseBreakdown.map { it.financialYear }
        )
        assertMoneyRows(
            listOf(4_933.33, 7_400.00, 7_400.00, 7_400.00, 7_400.00, 2_466.67),
            result.fyWiseBreakdown.map { it.interestAccrued }
        )
        assertBreakdownSumsToInterest(result)
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
    fun seniorCitizenSavingsYearWiseBreakdownUsesQuarterlyPayoutEvents() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.SCSS,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 8.2,
                compoundingFrequency = CompoundingFrequency.QUARTERLY
            )
        )

        assertEquals(
            listOf("2026-27", "2027-28", "2028-29", "2029-30", "2030-31", "2031-32"),
            result.fyWiseBreakdown.map { it.financialYear }
        )
        assertMoneyRows(
            listOf(4_100.00, 8_200.00, 8_200.00, 8_200.00, 8_200.00, 4_100.00),
            result.fyWiseBreakdown.map { it.interestAccrued }
        )
        assertBreakdownSumsToInterest(result)
    }

    @Test
    fun nationalSavingsCertificateYearWiseBreakdownUsesAnnualCompounding() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.NSC,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-07-01"),
                ratePercent = 7.7,
                compoundingFrequency = CompoundingFrequency.ANNUAL
            )
        )

        assertMoneyRows(
            listOf(7_700.00, 8_292.90, 8_931.45),
            result.fyWiseBreakdown.take(3).map { it.interestAccrued }
        )
        assertTrue(result.fyWiseBreakdown[0].interestAccrued < result.fyWiseBreakdown[1].interestAccrued)
        assertBreakdownSumsToInterest(result)
    }

    @Test
    fun schemesWithBreakdownRowsSumToInterestEarned() {
        val inputs = listOf(
            CalculatorInput(SchemeType.MSSC, 100_000.0, LocalDate.parse("2026-07-01"), 7.5, CompoundingFrequency.QUARTERLY),
            CalculatorInput(SchemeType.SB, 25_000.0, LocalDate.parse("2026-07-01"), 4.0, CompoundingFrequency.SIMPLE, toDate = LocalDate.parse("2027-07-01")),
            CalculatorInput(SchemeType.SIMPLE_INTEREST, 100_000.0, LocalDate.parse("2026-07-01"), 6.0, CompoundingFrequency.SIMPLE, customYears = 2.0),
            CalculatorInput(
                SchemeType.COMPOUND_INTEREST,
                100_000.0,
                LocalDate.parse("2026-07-01"),
                8.0,
                CompoundingFrequency.QUARTERLY,
                customType = CustomCalculatorType.Compound,
                customYears = 2.0,
                compoundFrequencyOption = CompoundFrequencyOption.Quarterly
            )
        )

        inputs.map(InterestEngine::calculate).forEach { result ->
            assertTrue("Expected breakdown rows for ${result.schemeType}", result.fyWiseBreakdown.isNotEmpty())
            assertBreakdownSumsToInterest(result)
        }
    }

    @Test
    fun ppfAndSsyHideYearWiseBreakdownUntilOfficialDepositTimingInputsExist() {
        listOf(SchemeType.PPF, SchemeType.SSY).forEach { schemeType ->
            val result = InterestEngine.calculate(
                CalculatorInput(
                    schemeType = schemeType,
                    amount = 50_000.0,
                    startDate = LocalDate.parse("2026-07-01"),
                    ratePercent = 7.1,
                    compoundingFrequency = CompoundingFrequency.ANNUAL
                )
            )

            assertTrue(result.fyWiseBreakdown.isEmpty())
        }
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

    private fun assertBreakdownSumsToInterest(result: roy.ij.postofficesaathi.domain.calculator.CalculatorResult) {
        assertEquals(
            "Breakdown should sum to interest for ${result.schemeType}",
            result.interestEarned,
            result.fyWiseBreakdown.sumOf { it.interestAccrued },
            0.01
        )
    }

    private fun assertMoneyRows(expected: List<Double>, actual: List<Double>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEachIndexed { index, (expectedValue, actualValue) ->
            assertEquals("Unexpected value at index $index", expectedValue, actualValue, 0.01)
        }
    }
}
