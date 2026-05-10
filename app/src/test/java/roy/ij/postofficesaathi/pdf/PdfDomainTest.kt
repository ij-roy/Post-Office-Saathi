package roy.ij.postofficesaathi.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.domain.pdf.PdfFileNameFactory
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementSnapper
import java.time.LocalDate

class PdfDomainTest {
    @Test
    fun layoutsUseGenericCardLabels() {
        assertEquals(
            listOf("Card 1"),
            PdfLayoutType.OneDocument.documentLabels
        )
        assertEquals(
            listOf("Card 1", "Card 2"),
            PdfLayoutType.TwoDocuments.documentLabels
        )
        assertEquals(
            listOf("Card 1", "Card 2", "Card 3"),
            PdfLayoutType.ThreeCards.documentLabels
        )
    }

    @Test
    fun pdfFileNameRemovesUnsafeCharactersAndAddsDate() {
        val name = PdfFileNameFactory.create(
            customerName = " Sita/Devi ji ",
            layoutType = PdfLayoutType.ThreeCards,
            date = LocalDate.of(2026, 5, 10)
        )

        assertEquals("Sita_Devi_ji_Three_Cards_2026-05-10.pdf", name)
    }

    @Test
    fun blankCustomerNameFallsBackToDocumentName() {
        val name = PdfFileNameFactory.create(
            customerName = " ",
            layoutType = PdfLayoutType.OneDocument,
            date = LocalDate.of(2026, 5, 10)
        )

        assertEquals("Customer_Document_2026-05-10.pdf", name)
    }

    @Test
    fun defaultPlacementsKeepSameCardSizeForAllLayouts() {
        val one = PdfPlacementFactory.defaultPlacements(1)
        val two = PdfPlacementFactory.defaultPlacements(2)
        val three = PdfPlacementFactory.defaultPlacements(3)

        // All layouts must use the exact same default card width
        assertEquals(one.first().width, two.first().width, 0.0001f)
        assertEquals(two.first().width, three.first().width, 0.0001f)

        // All layouts must use the exact same default card height
        assertEquals(one.first().height, two.first().height, 0.0001f)
        assertEquals(two.first().height, three.first().height, 0.0001f)
    }

    @Test
    fun defaultPlacementsMatchFactoryConstants() {
        val one = PdfPlacementFactory.defaultPlacements(1)
        assertEquals(PdfPlacementFactory.DefaultCardWidth, one.first().width, 0.0001f)
        assertEquals(PdfPlacementFactory.DefaultCardHeight, one.first().height, 0.0001f)
    }

    @Test
    fun defaultPlacementsAreCenteredHorizontally() {
        val three = PdfPlacementFactory.defaultPlacements(3)
        val expectedX = (1f - PdfPlacementFactory.DefaultCardWidth) / 2f
        three.forEach { placement ->
            assertEquals(expectedX, placement.x, 0.0001f)
        }
    }

    @Test
    fun defaultVerticalPositionsForOneCard() {
        val one = PdfPlacementFactory.defaultPlacements(1)
        val expectedY = (1f - one[0].height) / 2f

        // A single card should be centered vertically on the A4 page.
        assertEquals(expectedY, one[0].y, 0.0001f)
        assertTrue("First card should not be at the very top", one[0].y > 0.05f)
    }

    @Test
    fun defaultVerticalPositionsForTwoCards() {
        val two = PdfPlacementFactory.defaultPlacements(2)

        // Both cards should be within the page
        assertTrue(two[0].y >= 0f)
        assertTrue(two[1].y + two[1].height <= 1f)

        // Second card should be below the first
        assertTrue(two[1].y > two[0].y + two[0].height)
    }

    @Test
    fun defaultVerticalPositionsForThreeCards() {
        val three = PdfPlacementFactory.defaultPlacements(3)

        // All cards within page bounds
        three.forEach { p ->
            assertTrue(p.y >= 0f)
            assertTrue(p.y + p.height <= 1f)
        }

        // Cards should be stacked vertically in order
        assertTrue(three[1].y > three[0].y + three[0].height)
        assertTrue(three[2].y > three[1].y + three[1].height)

        // Consistent spacing between cards
        val gap1 = three[1].y - (three[0].y + three[0].height)
        val gap2 = three[2].y - (three[1].y + three[1].height)
        assertEquals(gap1, gap2, 0.0001f)
    }

    @Test
    fun resetRestoresDefaultPlacements() {
        val edited = PdfPlacementFactory.defaultPlacements(2).mapIndexed { index, placement ->
            placement.copy(x = placement.x + index * 0.1f, width = placement.width * 1.4f)
        }

        assertEquals(PdfPlacementFactory.defaultPlacements(2), PdfPlacementFactory.reset(edited))
    }

    @Test
    fun resetPreservesImagePaths() {
        val paths = listOf("a.jpg", "b.jpg")
        val edited = PdfPlacementFactory.defaultPlacements(2, paths).map {
            it.copy(x = 0.5f, y = 0.5f, width = 0.8f)
        }
        val reset = PdfPlacementFactory.reset(edited)
        assertEquals(paths, reset.map { it.imagePath })
    }

    @Test
    fun placementClampKeepsImageInsideA4Page() {
        val placement = PdfImagePlacement(
            imagePath = "x.jpg",
            x = 0.95f,
            y = -0.2f,
            width = 0.2f,
            height = 0.2f
        ).clamped()

        assertEquals(0.8f, placement.x, 0.0001f)
        assertEquals(0f, placement.y, 0.0001f)
    }

    @Test
    fun placementClampEnforcesMinimumSize() {
        val placement = PdfImagePlacement(
            imagePath = "x.jpg",
            x = 0.5f,
            y = 0.5f,
            width = 0.01f,
            height = 0.01f
        ).clamped()

        assertTrue(placement.width >= 0.08f)
        assertTrue(placement.height >= 0.04f)
    }

    @Test
    fun placementClampEnforcesCropBounds() {
        val placement = PdfImagePlacement(
            imagePath = "x.jpg",
            x = 0.0f,
            y = 0.0f,
            width = 0.5f,
            height = 0.5f,
            cropLeft = 0.8f,
            cropTop = 0.9f,
            cropRight = 0.7f,
            cropBottom = 0.85f
        ).clamped()

        // cropRight must be > cropLeft
        assertTrue(placement.cropRight > placement.cropLeft)
        // cropBottom must be > cropTop
        assertTrue(placement.cropBottom > placement.cropTop)
    }

    @Test
    fun editedPlacementPositionIsPreserved() {
        val defaults = PdfPlacementFactory.defaultPlacements(1, listOf("test.jpg"))
        val edited = defaults[0].copy(x = 0.1f, y = 0.3f)
        val clamped = edited.clamped()

        assertEquals(0.1f, clamped.x, 0.0001f)
        assertEquals(0.3f, clamped.y, 0.0001f)
        assertEquals(defaults[0].width, clamped.width, 0.0001f)
        assertEquals(defaults[0].height, clamped.height, 0.0001f)
    }

    @Test
    fun editedPlacementSizeIsPreserved() {
        val defaults = PdfPlacementFactory.defaultPlacements(1, listOf("test.jpg"))
        val edited = defaults[0].copy(width = 0.6f, height = 0.4f)
        val clamped = edited.clamped()

        assertEquals(0.6f, clamped.width, 0.0001f)
        assertEquals(0.4f, clamped.height, 0.0001f)
    }

    @Test
    fun noAutoScaleForSingleCard() {
        val one = PdfPlacementFactory.defaultPlacements(1)
        // Card should NOT fill the page — it should be the same small fixed size
        assertTrue("Single card should not fill A4 width", one[0].width < 0.6f)
        assertTrue("Single card should not fill A4 height", one[0].height < 0.5f)
    }

    @Test
    fun defaultPlacementsReturnCorrectCount() {
        assertEquals(1, PdfPlacementFactory.defaultPlacements(1).size)
        assertEquals(2, PdfPlacementFactory.defaultPlacements(2).size)
        assertEquals(3, PdfPlacementFactory.defaultPlacements(3).size)
    }

    @Test
    fun defaultPlacementsImagePathsArePreserved() {
        val paths = listOf("img1.jpg", "img2.jpg", "img3.jpg")
        val placements = PdfPlacementFactory.defaultPlacements(3, paths)
        assertEquals(paths, placements.map { it.imagePath })
    }

    @Test
    fun defaultCardAspectRatioIsIdCard() {
        // Placement units are normalized to A4; physical output should still be approximately 1.585:1.
        val a4Width = 595f
        val a4Height = 842f
        val ratio = (PdfPlacementFactory.DefaultCardWidth * a4Width) /
            (PdfPlacementFactory.DefaultCardHeight * a4Height)
        assertEquals(1.585f, ratio, 0.01f)
    }

    @Test
    fun activeSnapReleasesWhenDraggedAwayFromGuide() {
        val rawNearCenter = PdfImagePlacement(
            imagePath = "x.jpg",
            x = 0.334f,
            y = 0.2f,
            width = 0.32f,
            height = 0.2f
        )
        val initialSnap = PdfPlacementSnapper.snapPlacement(
            placement = rawNearCenter,
            placements = listOf(rawNearCenter),
            activeIndex = 0
        )
        assertTrue(initialSnap.isSnapped)

        val rawDraggedAwayButStillInsideSnapThreshold = rawNearCenter.copy(x = 0.332f)
        val released = PdfPlacementSnapper.snapPlacement(
            placement = rawDraggedAwayButStillInsideSnapThreshold,
            placements = listOf(rawDraggedAwayButStillInsideSnapThreshold),
            activeIndex = 0,
            previousRawPlacement = rawNearCenter,
            activeSnap = initialSnap.activeSnap
        )

        assertEquals(rawDraggedAwayButStillInsideSnapThreshold.x, released.placement.x, 0.0001f)
        assertTrue(!released.isSnapped)

        val rawDraggedFartherAwayButStillInsideSnapThreshold = rawNearCenter.copy(x = 0.3305f)
        val stillReleased = PdfPlacementSnapper.snapPlacement(
            placement = rawDraggedFartherAwayButStillInsideSnapThreshold,
            placements = listOf(rawDraggedFartherAwayButStillInsideSnapThreshold),
            activeIndex = 0,
            previousRawPlacement = rawDraggedAwayButStillInsideSnapThreshold,
            activeSnap = released.activeSnap
        )

        assertEquals(rawDraggedFartherAwayButStillInsideSnapThreshold.x, stillReleased.placement.x, 0.0001f)
        assertTrue(!stillReleased.isSnapped)
    }
}
