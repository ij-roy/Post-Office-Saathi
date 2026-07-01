package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.InterestEngine
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure
import roy.ij.postofficesaathi.ui.calculator.toShareText

class CalculatorResultShareTextTest {
    @Test
    fun shareTextUsesCleanResultSummaryWithoutRepeatedProjectionWords() {
        val result = CalculatorResult(
            schemeType = SchemeType.RD,
            title = "Recurring Deposit",
            ratePercent = 6.7,
            rateLabel = "6.7%",
            totalDeposited = 30000.0,
            interestEarned = 5718.18,
            maturityAmount = 35718.18,
            maturityDate = LocalDate.of(2031, 7, 1)
        )

        val text = result.toShareText()

        assertTrue(text.contains("Recurring Deposit (RD)"))
        assertTrue(text.contains("Total payable:"))
        assertTrue(text.contains("Deposited:"))
        assertTrue(text.contains("Interest earned:"))
        assertTrue(text.contains("Maturity date:"))
        assertTrue(text.contains("https://ij-roy.github.io/postofficesaathi/"))
        assertFalse(text.contains("with deposits"))
        assertFalse(text.contains("Total deposited"))
    }

    @Test
    fun recurringDepositShareTextIncludesMonthlyAmountAndInstallments() {
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

        val text = result.toShareText()

        assertTrue(text.contains("Monthly amount:"))
        assertTrue(text.contains("Installments: 60"))
        assertTrue(text.contains("Opening date: 01 Jul 2026"))
    }

    @Test
    fun monthlyIncomeShareTextExplainsPayoutAndTotalInterest() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.MIS,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 7.4,
                compoundingFrequency = CompoundingFrequency.MONTHLY
            )
        )

        val text = result.toShareText()

        assertTrue(text.contains("Monthly payout:"))
        assertTrue(text.contains("Total interest:"))
        assertTrue(text.contains("Term: 5 years"))
    }

    @Test
    fun timeDepositShareTextIncludesSelectedTenure() {
        val result = InterestEngine.calculate(
            CalculatorInput(
                schemeType = SchemeType.TD,
                amount = 100_000.0,
                startDate = LocalDate.parse("2026-04-01"),
                ratePercent = 7.5,
                compoundingFrequency = CompoundingFrequency.QUARTERLY,
                tdTenure = TDTenure.ThreeYears
            )
        )

        val text = result.toShareText()

        assertTrue(text.contains("Tenure: 3 Years"))
        assertTrue(text.contains("Investment:"))
    }
}
