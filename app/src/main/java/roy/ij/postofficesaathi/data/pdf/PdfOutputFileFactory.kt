package roy.ij.postofficesaathi.data.pdf

import java.io.File

object PdfOutputFileFactory {
    fun nextAvailableFile(outputDir: File, baseFileName: String): File {
        val baseFile = File(outputDir, baseFileName)
        if (!baseFile.exists()) return baseFile

        val extensionIndex = baseFileName.lastIndexOf('.')
        val stem = if (extensionIndex > 0) baseFileName.substring(0, extensionIndex) else baseFileName
        val extension = if (extensionIndex > 0) baseFileName.substring(extensionIndex) else ""

        var suffix = 2
        while (true) {
            val candidate = File(outputDir, "$stem-$suffix$extension")
            if (!candidate.exists()) return candidate
            suffix += 1
        }
    }
}
