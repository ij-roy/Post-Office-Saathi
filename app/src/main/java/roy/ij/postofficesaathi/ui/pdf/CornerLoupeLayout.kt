package roy.ij.postofficesaathi.ui.pdf

data class CornerLoupeLayout(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val reticleX: Float,
    val reticleY: Float
) {
    companion object {
        fun calculate(
            bitmapWidth: Int,
            bitmapHeight: Int,
            centerX: Int,
            centerY: Int,
            cropSize: Int,
            displaySize: Float
        ): CornerLoupeLayout {
            val left = (centerX - cropSize / 2).coerceIn(0, maxOf(0, bitmapWidth - cropSize))
            val top = (centerY - cropSize / 2).coerceIn(0, maxOf(0, bitmapHeight - cropSize))
            val actualWidth = minOf(cropSize, bitmapWidth - left)
            val actualHeight = minOf(cropSize, bitmapHeight - top)
            return CornerLoupeLayout(
                left = left,
                top = top,
                width = actualWidth,
                height = actualHeight,
                reticleX = ((centerX - left).toFloat() / actualWidth.toFloat()) * displaySize,
                reticleY = ((centerY - top).toFloat() / actualHeight.toFloat()) * displaySize
            )
        }
    }
}
