package roy.ij.postofficesaathi.preferences

import org.junit.Assert.assertEquals
import org.junit.Test
import roy.ij.postofficesaathi.data.preferences.ThemeMode

class ThemeModeTest {
    @Test
    fun fromStoredValueDefaultsInvalidValueToSystem() {
        assertEquals(ThemeMode.System, ThemeMode.fromStoredValue("unexpected"))
    }

    @Test
    fun fromStoredValueParsesKnownValues() {
        assertEquals(ThemeMode.System, ThemeMode.fromStoredValue("system"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStoredValue("light"))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStoredValue("dark"))
    }
}
