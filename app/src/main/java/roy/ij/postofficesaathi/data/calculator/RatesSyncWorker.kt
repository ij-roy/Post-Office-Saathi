package roy.ij.postofficesaathi.data.calculator

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsProvider

class RatesSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val analytics = AnalyticsProvider.create(applicationContext)
        return when (val result = GitHubRatesRepository(applicationContext).syncRates()) {
            is RatesSyncResult.Updated -> {
                analytics.logEvent(
                    AnalyticsEvent.RatesSyncCompleted,
                    mapOf(
                        AnalyticsParam.RatesVersion to result.version,
                        AnalyticsParam.UsedFallback to false
                    )
                )
                Result.success()
            }
            is RatesSyncResult.Unchanged -> {
                analytics.logEvent(
                    AnalyticsEvent.RatesSyncCompleted,
                    mapOf(
                        AnalyticsParam.RatesVersion to result.version,
                        AnalyticsParam.UsedFallback to true
                    )
                )
                Result.success()
            }
            is RatesSyncResult.Failed -> {
                analytics.logEvent(
                    AnalyticsEvent.RatesSyncFailed,
                    mapOf(AnalyticsParam.ErrorType to result.throwable.javaClass.simpleName)
                )
                if (runAttemptCount < 2) Result.retry() else Result.failure()
            }
        }
    }
}

