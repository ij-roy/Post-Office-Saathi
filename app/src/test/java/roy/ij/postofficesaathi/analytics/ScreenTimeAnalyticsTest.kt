package roy.ij.postofficesaathi.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTimeAnalyticsTest {
    @Test
    fun durationBucketGroupsElapsedTimeForScreenTime() {
        assertEquals("0_5s", AnalyticsSanitizer.durationBucket(4_999))
        assertEquals("5_15s", AnalyticsSanitizer.durationBucket(8_000))
        assertEquals("15_60s", AnalyticsSanitizer.durationBucket(20_000))
        assertEquals("1_5m", AnalyticsSanitizer.durationBucket(120_000))
        assertEquals("5m_plus", AnalyticsSanitizer.durationBucket(360_000))
    }
}
