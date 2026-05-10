package roy.ij.postofficesaathi.domain.pdf

import android.graphics.BitmapFactory

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
    const val DefaultCardWidth = 0.50f // Updated to 50% width
    private const val DefaultImageAspect = 1.585f
    private const val A4AspectCompensation = 842f / 595f
    const val DefaultCardHeight = DefaultCardWidth / (DefaultImageAspect * A4AspectCompensation)
    private const val CardGap = 0.06f

    fun defaultPlacements(count: Int, imagePaths: List<String> = emptyList()): List<PdfImagePlacement> {
        // Phase 1: Measure all real-world asset proportions
        val dynamicHeights = mutableListOf<Float>()
        for (index in 0 until count) {
            val path = imagePaths.getOrElse(index) { "" }
            var aspect = DefaultImageAspect // Generic ID fallback

            if (path.isNotEmpty()) {
                try {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(path, opts)
                    if (opts.outWidth > 0 && opts.outHeight > 0) {
                        aspect = opts.outWidth.toFloat() / opts.outHeight.toFloat()
                    }
                } catch (e: Exception) { }
            }
            val relativeAspect = aspect * A4AspectCompensation
            dynamicHeights.add(DefaultCardWidth / relativeAspect)
        }

        // Phase 2: Accumulate final group footprints to mathematically derive central starting Y anchor
        val totalContentHeight = dynamicHeights.sum()
        val totalSpacersHeight = if (count > 1) (count - 1) * CardGap else 0f
        val groupBlockHeight = totalContentHeight + totalSpacersHeight

        // Center the whole group Block vertically on page (1.0f representing total height)
        var currentY = ((1f - groupBlockHeight) / 2f).coerceAtLeast(0.05f)

        // Phase 3: Compile final absolute placement definitions
        val placements = mutableListOf<PdfImagePlacement>()
        for (index in 0 until count) {
            val h = dynamicHeights[index]
            placements.add(
                PdfImagePlacement(
                    imagePath = imagePaths.getOrElse(index) { "" },
                    x = (1f - DefaultCardWidth) / 2f, // Centered Horizontally
                    y = currentY, // Dynamic Floating Center Vertically
                    width = DefaultCardWidth,
                    height = h
                ).clamped()
            )
            currentY += h + CardGap
        }
        
        return placements
    }

    fun reset(placements: List<PdfImagePlacement>): List<PdfImagePlacement> =
        defaultPlacements(placements.size, placements.map { it.imagePath })
}
