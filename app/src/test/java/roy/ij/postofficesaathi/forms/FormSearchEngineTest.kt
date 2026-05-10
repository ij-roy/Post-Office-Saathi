package roy.ij.postofficesaathi.forms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.domain.forms.FormSearchEngine

class FormSearchEngineTest {
    private val forms = listOf(
        FormItem(
            id = "aadhaar-update",
            title = "Aadhaar Update Form",
            category = "Aadhaar",
            description = "Form used for Aadhaar address and mobile update.",
            keywords = listOf("aadhaar", "adhar", "aadhar", "uidai", "address change"),
            file = "forms/aadhaar-update.pdf",
            language = "English"
        ),
        FormItem(
            id = "rd-opening",
            title = "Recurring Deposit Account Opening",
            category = "Savings",
            description = "RD account opening form.",
            keywords = listOf("rd", "recurring deposit", "account opening"),
            file = "forms/rd-opening.pdf",
            language = "English"
        )
    )

    @Test
    fun searchMatchesCommonAadhaarMisspellingFromKeywords() {
        val results = FormSearchEngine.search(forms, "adhar")

        assertEquals("aadhaar-update", results.single().id)
    }

    @Test
    fun searchMatchesMultipleWordsAcrossDescriptionAndTitle() {
        val results = FormSearchEngine.search(forms, "account opening")

        assertEquals("rd-opening", results.single().id)
    }

    @Test
    fun blankSearchReturnsAllForms() {
        val results = FormSearchEngine.search(forms, "   ")

        assertEquals(forms.map { it.id }, results.map { it.id })
    }

    @Test
    fun noResultsCanBeDetectedForFeedbackLogging() {
        val results = FormSearchEngine.search(forms, "passport renewal")

        assertTrue(results.isEmpty())
    }
}
