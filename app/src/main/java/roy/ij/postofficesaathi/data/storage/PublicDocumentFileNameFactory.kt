package roy.ij.postofficesaathi.data.storage

object PublicDocumentFileNameFactory {
    fun nextAvailableName(baseFileName: String, existingNames: Set<String>): String {
        if (baseFileName !in existingNames) return baseFileName

        val extensionIndex = baseFileName.lastIndexOf('.')
        val stem = if (extensionIndex > 0) baseFileName.substring(0, extensionIndex) else baseFileName
        val extension = if (extensionIndex > 0) baseFileName.substring(extensionIndex) else ""

        var suffix = 2
        while (true) {
            val candidate = "$stem-$suffix$extension"
            if (candidate !in existingNames) return candidate
            suffix += 1
        }
    }
}
