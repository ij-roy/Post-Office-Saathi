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
    fun timeDepositCompoundsQuarterlyForSelectedTenure() {
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
        assertTrue(result.maturityAmount > 144_000.0)
        assertEquals(LocalDate.parse("2031-04-01"), result.maturityDate)
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
        assertEquals(LocalDate.parse("2031-04-01"), result.maturityDate)
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

