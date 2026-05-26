package roy.ij.postofficesaathi.analytics

import android.content.Context
import android.os.Build
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AnalyticsProvider {
    fun create(context: Context): SaathiAnalytics {
        val analytics = FirebaseSaathiAnalytics(
            firebaseAnalytics = FirebaseAnalytics.getInstance(context),
            crashlytics = FirebaseCrashlytics.getInstance()
        )
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        analytics.setContext("app_version", "${packageInfo.versionName} ($versionCode)")
        return analytics
    }
}
