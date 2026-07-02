package roy.ij.postofficesaathi.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseSaathiAnalytics(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : SaathiAnalytics {
    override fun logEvent(name: String, params: Map<String, Any?>) {
        firebaseAnalytics.logEvent(name, params.toBundle())
    }

    override fun logButtonTap(buttonId: String, screen: String) {
        logEvent(
            AnalyticsEvent.ButtonTapped,
            mapOf(
                AnalyticsParam.ButtonId to buttonId,
                AnalyticsParam.Screen to screen
            )
        )
    }

    override fun logScreenViewed(screen: String) {
        setContext(AnalyticsParam.Screen, screen)
        logEvent(AnalyticsEvent.ScreenViewed, mapOf(AnalyticsParam.Screen to screen))
    }

    override fun logScreenTime(screen: String, durationMillis: Long) {
        logEvent(
            AnalyticsEvent.ScreenTime,
            mapOf(
                AnalyticsParam.Screen to screen,
                AnalyticsParam.DurationBucket to AnalyticsSanitizer.durationBucket(durationMillis)
            )
        )
    }

    override fun setContext(key: String, value: String?) {
        if (value == null) {
            crashlytics.setCustomKey(key, "")
        } else {
            crashlytics.setCustomKey(key, value.take(100))
        }
    }

    override fun recordError(area: String, throwable: Throwable, params: Map<String, Any?>) {
        crashlytics.setCustomKey(AnalyticsParam.ErrorArea, area)
        crashlytics.setCustomKey(AnalyticsParam.ErrorType, throwable.javaClass.simpleName)
        params.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value?.toString()?.take(100) ?: "")
        }
        crashlytics.recordException(throwable)
    }

    private fun Map<String, Any?>.toBundle(): Bundle =
        Bundle().also { bundle ->
            forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putLong(key, value.toLong())
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Float -> bundle.putDouble(key, value.toDouble())
                    is Boolean -> bundle.putString(key, value.toString())
                    else -> bundle.putString(key, value.toString())
                }
            }
        }
}
