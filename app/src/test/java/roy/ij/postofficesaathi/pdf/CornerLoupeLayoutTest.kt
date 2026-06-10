package roy.ij.postofficesaathi.pdf

import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.ui.pdf.CornerLoupeLayout

class CornerLoupeLayoutTest {
    @Test
    fun reticleTracksCornerWhenCropIsClampedAtLeftEdge() {
        val layout = CornerLoupeLayout.calculate(
            bitmapWidth = 1000,
            bitmapHeight = 800,
            centerX = 20,
            centerY = 400,
            cropSize = 180,
            displaySize = 132f
        )

        assertEquals(0, layout.left)
        assertEquals(14.666667f, layout.reticleX, 0.0001f)
        assertEquals(66f, layout.reticleY, 0.0001f)
    }

    @Test
    fun reticleTracksCornerWhenCropIsCentered() {
        val layout = CornerLoupeLayout.calculate(
            bitmapWidth = 1000,
            bitmapHeight = 800,
            centerX = 500,
            centerY = 400,
            cropSize = 180,
            displaySize = 132f
        )

        assertEquals(410, layout.left)
        assertEquals(66f, layout.reticleX, 0.0001f)
        assertEquals(66f, layout.reticleY, 0.0001f)
    }
}
