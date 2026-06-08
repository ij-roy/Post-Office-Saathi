package roy.ij.postofficesaathi.ui.pdf.state

import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement

object PdfFlowSavedStateCodec {
    fun encodePlacements(placements: List<PdfImagePlacement>): ArrayList<String> =
        ArrayList(placements.map(::encodePlacement))

    fun decodePlacements(encoded: List<String>): List<PdfImagePlacement> =
        encoded.mapNotNull(::decodePlacement)

    private fun encodePlacement(placement: PdfImagePlacement): String =
        listOf(
            placement.imagePath,
            placement.x,
            placement.y,
            placement.width,
            placement.height,
            placement.cropLeft,
            placement.cropTop,
            placement.cropRight,
            placement.cropBottom,
            placement.rotationDegrees
        ).joinToString(separator = "|") { value ->
            value.toString().replace("\\", "\\\\").replace("|", "\\p")
        }

    private fun decodePlacement(value: String): PdfImagePlacement? {
        val parts = splitEscaped(value)
        if (parts.size != 10) return null
        return runCatching {
            PdfImagePlacement(
                imagePath = parts[0],
                x = parts[1].toFloat(),
                y = parts[2].toFloat(),
                width = parts[3].toFloat(),
                height = parts[4].toFloat(),
                cropLeft = parts[5].toFloat(),
                cropTop = parts[6].toFloat(),
                cropRight = parts[7].toFloat(),
                cropBottom = parts[8].toFloat(),
                rotationDegrees = parts[9].toFloat()
            )
        }.getOrNull()
    }

    private fun splitEscaped(value: String): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        value.forEach { char ->
            when {
                escaped -> {
                    current.append(if (char == 'p') '|' else char)
                    escaped = false
                }
                char == '\\' -> escaped = true
                char == '|' -> {
                    parts += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        if (escaped) current.append('\\')
        parts += current.toString()
        return parts
    }
}
