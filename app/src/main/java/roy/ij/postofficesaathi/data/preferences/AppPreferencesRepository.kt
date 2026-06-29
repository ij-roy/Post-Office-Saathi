package roy.ij.postofficesaathi.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import roy.ij.postofficesaathi.data.review.ReviewPromptPolicy

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

enum class ThemeMode(val storedValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStoredValue(value: String?): ThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: System
    }
}

data class AppPreferences(
    val hasSeenOnboarding: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val completedActionCount: Int = 0,
    val lastReviewPromptVersion: Long? = null
) {
    fun markOnboardingSeen(): AppPreferences = copy(hasSeenOnboarding = true)
}

class AppPreferencesRepository(private val context: Context) {
    val preferences: Flow<AppPreferences> = context.appPreferencesDataStore.data.map { data ->
        AppPreferences(
            hasSeenOnboarding = data[KeyHasSeenOnboarding] ?: false,
            themeMode = ThemeMode.fromStoredValue(data[KeyThemeMode]),
            completedActionCount = data[KeyCompletedActionCount] ?: 0,
            lastReviewPromptVersion = data[KeyLastReviewPromptVersion]
        )
    }

    suspend fun markOnboardingSeen() {
        context.appPreferencesDataStore.edit { data ->
            data[KeyHasSeenOnboarding] = true
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.appPreferencesDataStore.edit { data ->
            data[KeyThemeMode] = themeMode.storedValue
        }
    }

    suspend fun recordMeaningfulAction(currentVersionCode: Long): Boolean {
        var shouldRequestReview = false
        context.appPreferencesDataStore.edit { data ->
            val result = ReviewPromptPolicy.onMeaningfulActionCompleted(
                completedActionCount = data[KeyCompletedActionCount] ?: 0,
                lastReviewPromptVersion = data[KeyLastReviewPromptVersion],
                currentVersionCode = currentVersionCode
            )
            data[KeyCompletedActionCount] = result.completedActionCount
            if (result.shouldRequestReview) {
                data[KeyLastReviewPromptVersion] = currentVersionCode
                shouldRequestReview = true
            }
        }
        return shouldRequestReview
    }

    private companion object {
        val KeyHasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val KeyThemeMode = stringPreferencesKey("theme_mode")
        val KeyCompletedActionCount = intPreferencesKey("completed_action_count")
        val KeyLastReviewPromptVersion = longPreferencesKey("last_review_prompt_version")
    }
}
