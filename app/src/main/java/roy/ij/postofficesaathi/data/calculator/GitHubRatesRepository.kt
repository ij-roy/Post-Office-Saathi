package roy.ij.postofficesaathi.data.calculator

import android.content.Context
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubRatesRepository(
    private val context: Context,
    private val ratesUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/rates.json"
) : RatesRepository {
    private val appContext = context.applicationContext
    private val ratesDir = File(appContext.filesDir, "rates").apply { mkdirs() }
    private val cacheFile = File(ratesDir, "rates.json")
    private val preferences = appContext.getSharedPreferences("rates_preferences", Context.MODE_PRIVATE)

    override suspend fun loadRates(): RatesLoadResult = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            runCatching {
                return@withContext RatesLoadResult(
                    history = RateHistoryParser.parse(cacheFile.readText()),
                    isFromCache = true
                )
            }
        }
        RatesLoadResult(
            history = RateHistoryParser.parse(appContext.assets.open(AssetRatesFile).bufferedReader().use { it.readText() }),
            isFromCache = false
        )
    }

    override suspend fun syncRates(): RatesSyncResult = withContext(Dispatchers.IO) {
        runCatching {
            val freshJson = URL(ratesUrl).readText()
            val freshHistory = RateHistoryParser.parse(freshJson)
            val previousVersion = preferences.getString(KeyRatesVersion, null)
            if (previousVersion == freshHistory.version && cacheFile.exists()) {
                RatesSyncResult.Unchanged(freshHistory.version)
            } else {
                cacheFile.writeText(freshJson)
                preferences.edit()
                    .putString(KeyRatesVersion, freshHistory.version)
                    .putString(KeyPendingToast, "Rates updated as of ${freshHistory.version.toFriendlyDate()}")
                    .apply()
                RatesSyncResult.Updated(freshHistory.version)
            }
        }.getOrElse { RatesSyncResult.Failed(it) }
    }

    override fun lastSyncedVersion(): String? = preferences.getString(KeyRatesVersion, null)

    override fun pendingRateUpdateToast(): String? = preferences.getString(KeyPendingToast, null)

    override fun clearPendingRateUpdateToast() {
        preferences.edit().remove(KeyPendingToast).apply()
    }

    private fun String.toFriendlyDate(): String =
        runCatching {
            LocalDate.parse(this).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
        }.getOrDefault(this)

    companion object {
        private const val AssetRatesFile = "rates.json"
        private const val KeyRatesVersion = "rates_version"
        private const val KeyPendingToast = "pending_rate_update_toast"
    }
}

