package roy.ij.postofficesaathi.preferences

import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.data.preferences.AppPreferences

class OnboardingStateTest {
    @Test
    fun markOnboardingSeenSetsFlag() {
        val state = AppPreferences(hasSeenOnboarding = false)

        val updated = state.markOnboardingSeen()

        assertTrue(updated.hasSeenOnboarding)
    }
}
