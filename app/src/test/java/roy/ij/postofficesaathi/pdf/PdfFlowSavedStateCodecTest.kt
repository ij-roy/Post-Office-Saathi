package roy.ij.postofficesaathi.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.ui.pdf.state.PdfFlowSavedStateCodec

class PdfFlowSavedStateCodecTest {
    @Test
    fun placementCodecStoresOnlyPrimitiveAndStringValues() {
        val placements = listOf(
            PdfImagePlacement(
                imagePath = "/cache/card_1.jpg",
                x = 0.2f,
                y = 0.3f,
                width = 0.4f,
                height = 0.5f,
                cropLeft = 0.1f,
                cropTop = 0.2f,
                cropRight = 0.9f,
                cropBottom = 0.8f,
                rotationDegrees = 45f
            )
        )

        val encoded = PdfFlowSavedStateCodec.encodePlacements(placements)

        assertFalse(encoded.any { it.contains("Bitmap", ignoreCase = true) })
        assertEquals(placements, PdfFlowSavedStateCodec.decodePlacements(encoded))
    }
}
