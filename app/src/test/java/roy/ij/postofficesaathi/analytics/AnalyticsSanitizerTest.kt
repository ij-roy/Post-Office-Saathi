package roy.ij.postofficesaathi.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsSanitizerTest {
    @Test
    fun safeSearchTextKeepsUsefulShortSearches() {
        assertEquals("rd account", AnalyticsSanitizer.safeSearchText("  RD Account  "))
    }

    @Test
    fun safeSearchTextDropsLikelyPhoneOrAadhaarNumbers() {
        assertNull(AnalyticsSanitizer.safeSearchText("9876543210"))
        assertNull(AnalyticsSanitizer.safeSearchText("1234 5678 9012"))
    }

    @Test
    fun safeSearchTextDropsLikelyPanValues() {
        assertNull(AnalyticsSanitizer.safeSearchText("ABCDE1234F"))
    }

    @Test
    fun safeSearchTextTruncatesLongPastedText() {
        val text = "recurring deposit account opening form with a lot of pasted words"

        assertEquals("recurring deposit account opening form", AnalyticsSanitizer.safeSearchText(text))
    }

    @Test
    fun queryLengthBucketGroupsSearchLengths() {
        assertEquals("empty", AnalyticsSanitizer.queryLengthBucket(""))
        assertEquals("1_3", AnalyticsSanitizer.queryLengthBucket("rd"))
        assertEquals("4_10", AnalyticsSanitizer.queryLengthBucket("aadhaar"))
        assertEquals("11_25", AnalyticsSanitizer.queryLengthBucket("account opening"))
        assertEquals("26_plus", AnalyticsSanitizer.queryLengthBucket("recurring deposit account opening"))
    }

    @Test
    fun countBucketGroupsResultCounts() {
        assertEquals("0", AnalyticsSanitizer.countBucket(0))
        assertEquals("1", AnalyticsSanitizer.countBucket(1))
        assertEquals("2_5", AnalyticsSanitizer.countBucket(3))
        assertEquals("6_10", AnalyticsSanitizer.countBucket(7))
        assertEquals("11_plus", AnalyticsSanitizer.countBucket(14))
    }

    @Test
    fun durationBucketGroupsElapsedTime() {
        assertEquals("0_5s", AnalyticsSanitizer.durationBucket(4_999))
        assertEquals("5_15s", AnalyticsSanitizer.durationBucket(8_000))
        assertEquals("15_60s", AnalyticsSanitizer.durationBucket(20_000))
        assertEquals("1_5m", AnalyticsSanitizer.durationBucket(120_000))
        assertEquals("5m_plus", AnalyticsSanitizer.durationBucket(360_000))
    }
}
