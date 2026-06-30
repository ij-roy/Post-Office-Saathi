package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.data.calculator.RateHistoryParser
import roy.ij.postofficesaathi.domain.calculator.SchemeRateResolver
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure

class SchemeRateResolverTest {
    @Test
    fun resolvesExactHistoricalRateForSchemeDate() {
        val history = RateHistoryParser.parse(publicRatesJson())

        val result = SchemeRateResolver.resolve(
            history = history,
            schemeType = SchemeType.RD,
            date = LocalDate.parse("2023-05-01")
        )

        assertFalse(result.usedFallback)
        assertEquals(6.2, result.ratePercent, 0.001)
        assertEquals(LocalDate.parse("2023-04-01"), result.effectiveFrom)
    }

    @Test
    fun resolvesTdTenureRate() {
        val history = RateHistoryParser.parse(publicRatesJson())

        val result = SchemeRateResolver.resolve(
            history = history,
            schemeType = SchemeType.TD,
            date = LocalDate.parse("2024-02-01"),
            tdTenure = TDTenure.FiveYears
        )

        assertFalse(result.usedFallback)
        assertEquals(7.5, result.ratePercent, 0.001)
    }

    @Test
    fun fallsBackToCurrentRateWhenDateHasNoMatchingPeriod() {
        val history = RateHistoryParser.parse(publicRatesJson())

        val result = SchemeRateResolver.resolve(
            history = history,
            schemeType = SchemeType.KVP,
            date = LocalDate.parse("2012-01-01")
        )

        assertTrue(result.usedFallback)
        assertEquals(7.5, result.ratePercent, 0.001)
        assertEquals("2026-04-01", result.rateDatasetVersion)
        assertEquals(LocalDate.parse("2023-04-01"), result.effectiveFrom)
    }
}

