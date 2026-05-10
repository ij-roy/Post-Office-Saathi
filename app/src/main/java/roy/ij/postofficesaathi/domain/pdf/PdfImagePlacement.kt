package roy.ij.postofficesaathi.domain.pdf

data class PdfImagePlacement(
    val imagePath: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val rotationDegrees: Float = 0f
) {
    fun clamped(): PdfImagePlacement {
        val safeWidth = width.coerceIn(0.08f, 1f)
        val safeHeight = height.coerceIn(0.04f, 1f)
        val safeCropLeft = cropLeft.coerceIn(0f, 0.95f)
        val safeCropTop = cropTop.coerceIn(0f, 0.95f)
        val safeCropRight = cropRight.coerceIn(safeCropLeft + 0.05f, 1f)
        val safeCropBottom = cropBottom.coerceIn(safeCropTop + 0.05f, 1f)

        return copy(
            x = x.coerceIn(0f, 1f - safeWidth),
            y = y.coerceIn(0f, 1f - safeHeight),
            width = safeWidth,
            height = safeHeight,
            cropLeft = safeCropLeft,
            cropTop = safeCropTop,
            cropRight = safeCropRight,
            cropBottom = safeCropBottom,
            rotationDegrees = rotationDegrees
        )
    }
}

object PdfPlacementFactory {
    const val DefaultCardWidth = 0.32f
    const val DefaultCardHeight = DefaultCardWidth / 1.585f
    private const val FirstCardTop = 0.095f
    private const val CardGap = 0.06f

    fun defaultPlacements(count: Int, imagePaths: List<String> = emptyList()): List<PdfImagePlacement> =
        List(count) { index ->
            PdfImagePlacement(
                imagePath = imagePaths.getOrElse(index) { "" },
                x = (1f - DefaultCardWidth) / 2f,
                y = FirstCardTop + index * (DefaultCardHeight + CardGap),
                width = DefaultCardWidth,
                height = DefaultCardHeight
            ).clamped()
        }

    fun reset(placements: List<PdfImagePlacement>): List<PdfImagePlacement> =
        defaultPlacements(placements.size, placements.map { it.imagePath })
}
