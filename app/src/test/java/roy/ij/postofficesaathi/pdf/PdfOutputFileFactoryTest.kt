package roy.ij.postofficesaathi.pdf

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule
import roy.ij.postofficesaathi.data.pdf.PdfOutputFileFactory

class PdfOutputFileFactoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun nextAvailableFileReturnsBaseNameWhenItDoesNotExist() {
        val outputDir = temporaryFolder.newFolder("pdfs")

        val file = PdfOutputFileFactory.nextAvailableFile(outputDir, "Sita_Document_2026-06-08.pdf")

        assertEquals("Sita_Document_2026-06-08.pdf", file.name)
    }

    @Test
    fun nextAvailableFileAppendsIncrementingSuffixWhenBaseExists() {
        val outputDir = temporaryFolder.newFolder("pdfs")
        outputDir.resolve("Sita_Document_2026-06-08.pdf").writeText("existing")
        outputDir.resolve("Sita_Document_2026-06-08-2.pdf").writeText("existing")

        val file = PdfOutputFileFactory.nextAvailableFile(outputDir, "Sita_Document_2026-06-08.pdf")

        assertEquals("Sita_Document_2026-06-08-3.pdf", file.name)
    }
}
