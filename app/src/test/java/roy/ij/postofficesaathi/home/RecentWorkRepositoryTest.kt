package roy.ij.postofficesaathi.home

import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.data.recent.RecentWorkItem
import roy.ij.postofficesaathi.data.recent.RecentWorkType
import roy.ij.postofficesaathi.data.recent.sortRecentWorkItems

class RecentWorkRepositoryTest {
    @Test
    fun sortRecentWorkItemsReturnsNewestFirst() {
        val older = RecentWorkItem(
            title = "Older Form.pdf",
            uri = "content://forms/older",
            type = RecentWorkType.Form,
            modifiedAtMillis = 1000L
        )
        val newer = RecentWorkItem(
            title = "Newer PDF.pdf",
            uri = "content://pdfs/newer",
            type = RecentWorkType.CreatedPdf,
            modifiedAtMillis = 2000L
        )

        val sorted = sortRecentWorkItems(listOf(older, newer))

        assertEquals(listOf(newer, older), sorted)
    }
}
