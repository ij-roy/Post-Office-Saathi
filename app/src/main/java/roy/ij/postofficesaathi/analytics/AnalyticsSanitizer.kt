package roy.ij.postofficesaathi.analytics

object AnalyticsSanitizer {
    fun durationBucket(durationMillis: Long): String =
        when {
            durationMillis < 5_000 -> "0_5s"
            durationMillis < 15_000 -> "5_15s"
            durationMillis < 60_000 -> "15_60s"
            durationMillis < 300_000 -> "1_5m"
            else -> "5m_plus"
        }
}
