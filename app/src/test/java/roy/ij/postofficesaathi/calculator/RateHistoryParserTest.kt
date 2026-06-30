package roy.ij.postofficesaathi.calculator

import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.data.calculator.RateHistoryParser
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.SchemeType

class RateHistoryParserTest {
    @Test
    fun parsesRealPublicRatesJson() {
        val history = RateHistoryParser.parse(publicRatesJson())

        assertEquals("2026-04-01", history.version)
        assertTrue(history.rates.any { it.schemeType == SchemeType.RD })
        assertTrue(history.rates.any { it.schemeType == SchemeType.TD && it.tdTenureRates.containsKey("5Y") })
        assertTrue(history.rates.any { it.schemeType == SchemeType.MSSC })
    }

    @Test
    fun parsesFlatAndTenureRates() {
        val history = RateHistoryParser.parse(publicRatesJson())

        val rdCurrent = history.rates.first {
            it.schemeType == SchemeType.RD && it.effectiveTo == null
        }
        val tdCurrent = history.rates.first {
            it.schemeType == SchemeType.TD && it.effectiveTo == null
        }

        assertEquals(6.7, rdCurrent.rate, 0.001)
        assertEquals(CompoundingFrequency.QUARTERLY, rdCurrent.compoundingFrequency)
        assertEquals(7.5, tdCurrent.tdTenureRates.getValue("5Y"), 0.001)
        assertEquals(0.0, tdCurrent.rate, 0.001)
    }

    @Test
    fun exposesEarliestDatePerScheme() {
        val history = RateHistoryParser.parse(publicRatesJson())

        assertEquals(LocalDate.parse("1981-04-01"), history.earliestDateFor(SchemeType.RD))
        assertEquals(LocalDate.parse("2023-04-01"), history.earliestDateFor(SchemeType.MSSC))
        assertNotNull(history.earliestDateFor(SchemeType.SSY))
    }
}

