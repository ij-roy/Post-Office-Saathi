package roy.ij.postofficesaathi.analytics

object AnalyticsSanitizer {
    private val whitespace = Regex("\\s+")
    private val unsafeNumber = Regex(".*\\d(?:\\D*\\d){9,}.*")
    private val unsafePan = Regex(".*[A-Z]{5}\\d{4}[A-Z].*")
    private const val MaxSearchLength = 40

    fun safeSearchText(query: String): String? {
        val normalized = query
            .trim()
            .lowercase()
            .replace(whitespace, " ")
            .replace(Regex("[^a-z0-9\\s._-]"), "")

        if (normalized.isBlank()) return null
        val upper = normalized.uppercase()
        if (unsafeNumber.matches(normalized) || unsafePan.matches(upper)) return null

        return normalized.truncateAtWordBoundary(MaxSearchLength).ifBlank { null }
    }

    fun queryLengthBucket(query: String): String =
        when (query.trim().length) {
            0 -> "empty"
            in 1..3 -> "1_3"
            in 4..10 -> "4_10"
            in 11..25 -> "11_25"
            else -> "26_plus"
        }

    fun countBucket(count: Int): String =
        when (count) {
            0 -> "0"
            1 -> "1"
            in 2..5 -> "2_5"
            in 6..10 -> "6_10"
            else -> "11_plus"
        }

    fun durationBucket(durationMillis: Long): String =
        when {
            durationMillis < 5_000 -> "0_5s"
            durationMillis < 15_000 -> "5_15s"
            durationMillis < 60_000 -> "15_60s"
            durationMillis < 300_000 -> "1_5m"
            else -> "5m_plus"
        }

    fun amountBucket(amount: Double): String =
        when {
            amount < 1_000 -> "under_1k"
            amount < 10_000 -> "1k_10k"
            amount < 100_000 -> "10k_1l"
            amount < 500_000 -> "1l_5l"
            else -> "5l_plus"
        }

    fun tenureBucket(years: Double): String =
        when {
            years < 1.0 -> "under_1y"
            years < 3.0 -> "1_3y"
            years < 5.0 -> "3_5y"
            years < 10.0 -> "5_10y"
            else -> "10y_plus"
        }

    fun pincodePrefix(pincode: String): String {
        val digits = pincode.filter(Char::isDigit).take(3)
        return digits.padEnd(3, 'X') + "XXX"
    }

    private fun String.truncateAtWordBoundary(maxLength: Int): String {
        if (length <= maxLength) return this
        val candidate = take(maxLength).trim()
        val lastSpace = candidate.lastIndexOf(' ')
        return if (lastSpace > 0) candidate.take(lastSpace) else candidate
    }
}
