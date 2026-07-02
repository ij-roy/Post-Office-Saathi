package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure
import roy.ij.postofficesaathi.ui.calculator.scheme.SchemeCalculatorPresentation
import roy.ij.postofficesaathi.ui.calculator.scheme.SchemeCalculatorUiState
import roy.ij.postofficesaathi.ui.calculator.scheme.calculatorDisplayToDate

class SchemeCalculatorPresentationTest {
    @Test
    fun `rd uses direct amount date and installment labels`() {
        val presentation = SchemeCalculatorPresentation.forScheme(SchemeType.RD)

        assertEquals("RD Calculator", presentation.title)
        assertEquals(null, presentation.subtitle)
        assertEquals("Monthly Amount", presentation.amountLabel)
        assertEquals("Enter monthly amount (e.g. 5000)", presentation.amountPlaceholder)
        assertEquals("From", presentation.fromDateLabel)
        assertEquals("To", presentation.toDateLabel)
        assertTrue(presentation.showDateRange)
        assertEquals("Installments", presentation.installmentsLabel)
    }

    @Test
    fun `all calculator presentations remove header subtitle`() {
        SchemeType.entries.forEach { schemeType ->
            val presentation = SchemeCalculatorPresentation.forScheme(schemeType)

            assertEquals("Unexpected subtitle for $schemeType", null, presentation.subtitle)
        }
    }

    @Test
    fun `all calculator presentations provide an amount placeholder`() {
        SchemeType.entries.forEach { schemeType ->
            val presentation = SchemeCalculatorPresentation.forScheme(schemeType)

            assertNotNull("Missing placeholder for $schemeType", presentation.amountPlaceholder)
        }
    }

    @Test
    fun `calculator presentations use scheme specific placeholders`() {
        val expectations = mapOf(
            SchemeType.RD to "Enter monthly amount (e.g. 5000)",
            SchemeType.TD to "Enter investment amount (e.g. 100000)",
            SchemeType.MIS to "Enter investment amount (e.g. 100000)",
            SchemeType.NSC to "Enter investment amount (e.g. 100000)",
            SchemeType.KVP to "Enter investment amount (e.g. 100000)",
            SchemeType.SCSS to "Enter investment amount (e.g. 100000)",
            SchemeType.MSSC to "Enter investment amount (e.g. 100000)",
            SchemeType.PPF to "Enter yearly deposit (e.g. 50000)",
            SchemeType.SSY to "Enter yearly deposit (e.g. 50000)",
            SchemeType.SB to "Enter balance amount (e.g. 25000)",
            SchemeType.SIMPLE_INTEREST to "Enter principal amount (e.g. 100000)",
            SchemeType.COMPOUND_INTEREST to "Enter principal amount (e.g. 100000)"
        )

        expectations.forEach { (schemeType, placeholder) ->
            assertEquals(placeholder, SchemeCalculatorPresentation.forScheme(schemeType).amountPlaceholder)
        }
    }

    @Test
    fun `official schemes show from and to date range`() {
        val customSchemes = setOf(SchemeType.SIMPLE_INTEREST, SchemeType.COMPOUND_INTEREST)

        SchemeType.entries
            .filter { it.onHome && it !in customSchemes }
            .forEach { schemeType ->
                val presentation = SchemeCalculatorPresentation.forScheme(schemeType)

                assertEquals("From", presentation.fromDateLabel)
                assertEquals("To", presentation.toDateLabel)
                assertTrue("Expected date range for $schemeType", presentation.showDateRange)
            }
    }

    @Test
    fun `custom calculators do not show date fields`() {
        listOf(SchemeType.SIMPLE_INTEREST, SchemeType.COMPOUND_INTEREST).forEach { schemeType ->
            val presentation = SchemeCalculatorPresentation.forScheme(schemeType)

            assertEquals("Principal Amount", presentation.amountLabel)
            assertEquals("Enter principal amount (e.g. 100000)", presentation.amountPlaceholder)
            assertFalse("Custom calculator should not show date range", presentation.showDateRange)
        }
    }

    @Test
    fun `td display to date follows selected tenure`() {
        val state = SchemeCalculatorUiState(
            schemeType = SchemeType.TD,
            startDate = LocalDate.parse("2026-07-01"),
            tdTenure = TDTenure.ThreeYears
        )

        assertEquals(LocalDate.parse("2029-07-01"), calculatorDisplayToDate(state))
    }

    @Test
    fun `sb display to date stays user selected`() {
        val state = SchemeCalculatorUiState(
            schemeType = SchemeType.SB,
            startDate = LocalDate.parse("2026-07-01"),
            toDate = LocalDate.parse("2026-12-15")
        )

        assertEquals(LocalDate.parse("2026-12-15"), calculatorDisplayToDate(state))
    }

    @Test
    fun `rd display to date follows installment count`() {
        val state = SchemeCalculatorUiState(
            schemeType = SchemeType.RD,
            startDate = LocalDate.parse("2026-07-01"),
            installmentsPaid = "36"
        )

        assertEquals(LocalDate.parse("2029-07-01"), calculatorDisplayToDate(state))
    }

    @Test
    fun `custom calculator can still derive maturity date internally`() {
        val state = SchemeCalculatorUiState(
            schemeType = SchemeType.COMPOUND_INTEREST,
            startDate = LocalDate.parse("2026-07-01"),
            customYears = "2.5"
        )

        assertEquals(LocalDate.parse("2029-01-01"), calculatorDisplayToDate(state))
    }
}
