package roy.ij.postofficesaathi.forms

import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.data.forms.FormsIndexParser

class FormsIndexParserTest {
    @Test
    fun parsesFormsIndexJsonIntoFormItems() {
        val json = """
            [
              {
                "id": "aadhaar-update-form",
                "title": "Aadhaar Update Form",
                "category": "Aadhaar",
                "description": "Form used for Aadhaar details update.",
                "keywords": ["aadhaar", "adhar", "mobile update"],
                "file": "forms/aadhaar-update-form.pdf",
                "language": "English"
              }
            ]
        """.trimIndent()

        val forms = FormsIndexParser.parse(json)

        assertEquals(1, forms.size)
        assertEquals("aadhaar-update-form", forms[0].id)
        assertEquals(listOf("aadhaar", "adhar", "mobile update"), forms[0].keywords)
        assertEquals("forms/aadhaar-update-form.pdf", forms[0].file)
    }
}
