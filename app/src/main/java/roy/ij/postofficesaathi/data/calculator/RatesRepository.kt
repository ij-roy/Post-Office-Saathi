package roy.ij.postofficesaathi.data.calculator

import roy.ij.postofficesaathi.domain.calculator.RateHistory

interface RatesRepository {
    suspend fun loadRates(): RatesLoadResult
    suspend fun syncRates(): RatesSyncResult
    fun lastSyncedVersion(): String?
    fun pendingRateUpdateToast(): String?
    fun clearPendingRateUpdateToast()
}

data class RatesLoadResult(
    val history: RateHistory,
    val isFromCache: Boolean
)

sealed interface RatesSyncResult {
    data class Updated(val version: String) : RatesSyncResult
    data class Unchanged(val version: String) : RatesSyncResult
    data class Failed(val throwable: Throwable) : RatesSyncResult
}

