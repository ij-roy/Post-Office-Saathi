package roy.ij.postofficesaathi.domain.calculator

import java.time.LocalDate

object SchemeRateResolver {
    fun resolve(
        history: RateHistory,
        schemeType: SchemeType,
        date: LocalDate,
        tdTenure: TDTenure = TDTenure.FiveYears
    ): RateLookupResult {
        val exact = rateFor(schemeType, date, history.rates)
        val selected = exact ?: currentRate(schemeType, history.rates)
            ?: throw IllegalStateException("No rate data available for ${schemeType.name}")
        val ratePercent = selected.rateFor(tdTenure)
            ?: throw IllegalStateException("No ${tdTenure.jsonKey} TD rate data available")
        return RateLookupResult(
            schemeType = schemeType,
            requestedDate = date,
            ratePercent = ratePercent,
            compoundingFrequency = selected.compoundingFrequency,
            effectiveFrom = selected.effectiveFrom,
            effectiveTo = selected.effectiveTo,
            usedFallback = exact == null,
            rateDatasetVersion = history.version
        )
    }

    fun rateFor(scheme: SchemeType, date: LocalDate, rates: List<SchemeRate>): SchemeRate? =
        rates.firstOrNull {
            it.schemeType == scheme &&
                !date.isBefore(it.effectiveFrom) &&
                (it.effectiveTo == null || !date.isAfter(it.effectiveTo))
        }

    fun tdRateFor(tenure: TDTenure, date: LocalDate, rates: List<SchemeRate>): Double? =
        rateFor(SchemeType.TD, date, rates)?.tdTenureRates?.get(tenure.jsonKey)
            ?: currentRate(SchemeType.TD, rates)?.tdTenureRates?.get(tenure.jsonKey)

    fun currentRate(scheme: SchemeType, rates: List<SchemeRate>): SchemeRate? =
        rates
            .filter { it.schemeType == scheme }
            .maxWithOrNull(compareBy<SchemeRate> { it.effectiveTo == null }.thenBy { it.effectiveFrom })

    fun allRatesFor(scheme: SchemeType, rates: List<SchemeRate>): List<SchemeRate> =
        rates.filter { it.schemeType == scheme }.sortedByDescending { it.effectiveFrom }

    private fun SchemeRate.rateFor(tdTenure: TDTenure): Double? =
        if (schemeType == SchemeType.TD) tdTenureRates[tdTenure.jsonKey] else rate
}

