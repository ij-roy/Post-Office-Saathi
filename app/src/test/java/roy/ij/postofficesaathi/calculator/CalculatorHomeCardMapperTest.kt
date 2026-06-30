package roy.ij.postofficesaathi.calculator

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.RateHistory
import roy.ij.postofficesaathi.domain.calculator.SchemeRate
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.ui.calculator.CalculatorHomeCardMapper

class CalculatorHomeCardMapperTest {
    @Test
    fun `scheme cards use short name full description then plain rate`() {
        val history = RateHistory(
            version = "test",
            rates = listOf(
                SchemeRate(
                    schemeType = SchemeType.RD,
                    effectiveFrom = LocalDate.parse("2024-01-01"),
                    effectiveTo = null,
                    rate = 6.7,
                    compoundingFrequency = CompoundingFrequency.QUARTERLY
                )
            )
        )

        val card = CalculatorHomeCardMapper.cardFor(SchemeType.RD, history)

        assertEquals("RD", card.title)
        assertEquals("Recurring Deposit", card.description)
        assertEquals("6.7% p.a.", card.rateLabel)
        assertFalse(card.isDiscontinued)
    }
}
