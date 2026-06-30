package roy.ij.postofficesaathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit
import roy.ij.postofficesaathi.analytics.AnalyticsProvider
import roy.ij.postofficesaathi.data.calculator.RatesSyncWorker
import roy.ij.postofficesaathi.ui.PostOfficeSaathiApp
import roy.ij.postofficesaathi.ui.settings.AppSettingsExternalAction
import roy.ij.postofficesaathi.ui.settings.AppSettingsViewModel
import roy.ij.postofficesaathi.ui.splash.StartupSplashOverlay
import roy.ij.postofficesaathi.ui.theme.PostOfficeSaathiTheme

class MainActivity : ComponentActivity() {
    private val analytics by lazy { AnalyticsProvider.create(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enqueueRateSync()
        setContent {
            val settingsFactory = remember(applicationContext, analytics) {
                AppSettingsViewModel.Factory(applicationContext, analytics)
            }
            val settingsViewModel: AppSettingsViewModel = viewModel(factory = settingsFactory)
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(settingsViewModel) {
                settingsViewModel.externalActions.collect { action ->
                    if (action is AppSettingsExternalAction.RequestReview) {
                        val reviewManager = ReviewManagerFactory.create(this@MainActivity)
                        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
                            if (request.isSuccessful) {
                                reviewManager.launchReviewFlow(this@MainActivity, request.result)
                            }
                        }
                    }
                }
            }

            PostOfficeSaathiTheme(themeMode = settingsState.preferences.themeMode) {
                Box {
                    PostOfficeSaathiApp(
                        analytics = analytics,
                        settingsState = settingsState,
                        settingsViewModel = settingsViewModel
                    )
                    StartupSplashOverlay()
                }
            }
        }
    }

    private fun enqueueRateSync() {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "rates_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RatesSyncWorker>(6, TimeUnit.HOURS).build()
        )
        workManager.enqueueUniqueWork(
            "rates_sync_startup",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<RatesSyncWorker>().build()
        )
    }
}
