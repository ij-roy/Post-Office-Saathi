package roy.ij.postofficesaathi.storage

import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.data.storage.PublicDocumentFileNameFactory

class PublicDocumentFileNameFactoryTest {
    @Test
    fun nextAvailableNameReturnsBaseNameWhenItDoesNotExist() {
        val name = PublicDocumentFileNameFactory.nextAvailableName(
            baseFileName = "Sita_Document_2026-06-08.pdf",
            existingNames = emptySet()
        )

        assertEquals("Sita_Document_2026-06-08.pdf", name)
    }

    @Test
    fun nextAvailableNameAppendsIncrementingSuffixWhenBaseExists() {
        val name = PublicDocumentFileNameFactory.nextAvailableName(
            baseFileName = "Sita_Document_2026-06-08.pdf",
            existingNames = setOf(
                "Sita_Document_2026-06-08.pdf",
                "Sita_Document_2026-06-08-2.pdf"
            )
        )

        assertEquals("Sita_Document_2026-06-08-3.pdf", name)
    }
}
