package roy.ij.postofficesaathi.ui.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.preferences.AppPreferences
import roy.ij.postofficesaathi.data.preferences.AppPreferencesRepository
import roy.ij.postofficesaathi.data.preferences.ThemeMode

data class AppSettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val isLoading: Boolean = true
)

sealed interface AppSettingsExternalAction {
    data object RequestReview : AppSettingsExternalAction
}

class AppSettingsViewModel(
    private val repository: AppPreferencesRepository,
    private val analytics: SaathiAnalytics,
    private val currentVersionCode: Long
) : ViewModel() {
    val uiState = repository.preferences
        .map { AppSettingsUiState(preferences = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettingsUiState())

    private val _externalActions = MutableSharedFlow<AppSettingsExternalAction>()
    val externalActions: SharedFlow<AppSettingsExternalAction> = _externalActions.asSharedFlow()

    fun completeOnboarding(skipped: Boolean) {
        viewModelScope.launch {
            analytics.logEvent(if (skipped) AnalyticsEvent.OnboardingSkipped else AnalyticsEvent.OnboardingCompleted)
            repository.markOnboardingSeen()
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            analytics.logEvent(
                AnalyticsEvent.ThemeChanged,
                mapOf(AnalyticsParam.ThemeMode to themeMode.storedValue)
            )
            repository.setThemeMode(themeMode)
        }
    }

    fun recordMeaningfulAction() {
        viewModelScope.launch {
            if (repository.recordMeaningfulAction(currentVersionCode)) {
                analytics.logEvent(AnalyticsEvent.ReviewPromptRequested)
                _externalActions.emit(AppSettingsExternalAction.RequestReview)
            }
        }
    }

    fun logSettingsOpened() {
        analytics.logEvent(AnalyticsEvent.SettingsOpened)
    }

    fun logHelpOpened() {
        analytics.logEvent(AnalyticsEvent.HelpOpened)
    }

    fun logPrivacyOpened() {
        analytics.logEvent(AnalyticsEvent.PrivacyOpened)
    }

    fun logFeedbackEmailTapped() {
        analytics.logEvent(AnalyticsEvent.FeedbackEmailTapped)
    }

    fun logRateAppTapped() {
        analytics.logEvent(AnalyticsEvent.RateAppTapped)
    }

    class Factory(
        private val context: Context,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            return AppSettingsViewModel(
                repository = AppPreferencesRepository(context.applicationContext),
                analytics = analytics,
                currentVersionCode = versionCode
            ) as T
        }
    }
}
