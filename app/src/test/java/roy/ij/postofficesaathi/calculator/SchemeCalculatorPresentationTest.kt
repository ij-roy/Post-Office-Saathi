package roy.ij.postofficesaathi.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.ui.calculator.scheme.SchemeCalculatorPresentation

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
        assertTrue(presentation.showToDate)
        assertEquals("Installments", presentation.installmentsLabel)
    }
}
