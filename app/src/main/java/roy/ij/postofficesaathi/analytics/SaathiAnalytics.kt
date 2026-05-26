package roy.ij.postofficesaathi.analytics

interface SaathiAnalytics {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun logButtonTap(buttonId: String, screen: String)
    fun logScreenViewed(screen: String)
    fun logScreenTime(screen: String, durationMillis: Long)
    fun setContext(key: String, value: String?)
    fun recordError(area: String, throwable: Throwable, params: Map<String, Any?> = emptyMap())
}
