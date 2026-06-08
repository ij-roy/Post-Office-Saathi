package roy.ij.postofficesaathi.ui.forms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsSanitizer
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.forms.FormsLoadResult
import roy.ij.postofficesaathi.data.forms.FormsRepository
import roy.ij.postofficesaathi.data.forms.GitHubFormsRepository
import roy.ij.postofficesaathi.data.forms.OfflineFormsException
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.domain.forms.FormSearchEngine

data class FormsUiState(
    val loadResult: FormsLoadResult = FormsLoadResult(emptyList(), isFromCache = true),
    val query: String = "",
    val visibleForms: List<FormItem> = emptyList(),
    val isLoading: Boolean = true,
    val activeMessage: String? = null,
    val activeMessageId: Long = 0L,
    val activeFormId: String? = null,
    val activeFormAction: FormActionKind? = null
)

enum class FormActionKind {
    Open,
    Share
}

sealed interface FormsExternalAction {
    data class OpenPdf(val document: PublicDocumentRef, val form: FormItem, val query: String) : FormsExternalAction
    data class SharePdf(val document: PublicDocumentRef, val form: FormItem, val query: String) : FormsExternalAction
}

class FormsViewModel(
    private val repository: FormsRepository,
    private val analytics: SaathiAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow(FormsUiState())
    val uiState: StateFlow<FormsUiState> = _uiState.asStateFlow()

    private val _externalActions = MutableSharedFlow<FormsExternalAction>()
    val externalActions: SharedFlow<FormsExternalAction> = _externalActions.asSharedFlow()

    init {
        loadForms()
    }

    fun loadForms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.loadForms()
            analytics.logEvent(
                "forms_index_loaded",
                mapOf(
                    AnalyticsParam.Flow to AnalyticsFlow.Forms,
                    AnalyticsParam.FromCache to result.isFromCache,
                    AnalyticsParam.ResultCountBucket to AnalyticsSanitizer.countBucket(result.forms.size)
                )
            )
            _uiState.update { state ->
                state.copy(
                    loadResult = result,
                    visibleForms = FormSearchEngine.search(result.forms, state.query),
                    activeMessage = result.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateQuery(query: String) {
        val previousQuery = _uiState.value.query
        if (previousQuery.isBlank() && query.isNotBlank()) {
            analytics.logButtonTap("forms_search_field", AnalyticsScreen.Forms)
        }
        _uiState.update { state ->
            val visibleForms = FormSearchEngine.search(state.loadResult.forms, query)
            state.copy(query = query, visibleForms = visibleForms)
        }
        logSearch(query, _uiState.value.visibleForms.size)
    }

    fun openForm(form: FormItem) {
        if (_uiState.value.activeFormId != null) return
        viewModelScope.launch {
            val query = _uiState.value.query
            setActiveAction(form.id, FormActionKind.Open)
            analytics.logButtonTap("form_open_or_download", AnalyticsScreen.Forms)
            analytics.logEvent(AnalyticsEvent.FormDownloadStarted, form.analyticsParams(query))
            runCatching { repository.downloadForm(form) }
                .onSuccess { document ->
                    analytics.logEvent(AnalyticsEvent.FormDownloadSucceeded, form.analyticsParams(query))
                    markFormDownloaded(form.id)
                    if (document.newlySaved) {
                        showMessage("Form saved to Documents/PostOfficeSaathi/Forms.")
                    }
                    _externalActions.emit(FormsExternalAction.OpenPdf(document, form, query))
                }
                .onFailure { error ->
                    showMessage(error.downloadFailureMessage())
                    analytics.logEvent(AnalyticsEvent.FormDownloadFailed, form.analyticsParams(query, error))
                    analytics.recordError("form_download", error, form.analyticsParams(query))
                }
            clearActiveAction()
        }
    }

    fun shareForm(form: FormItem) {
        if (_uiState.value.activeFormId != null) return
        viewModelScope.launch {
            val query = _uiState.value.query
            setActiveAction(form.id, FormActionKind.Share)
            analytics.logButtonTap("form_share", AnalyticsScreen.Forms)
            analytics.logEvent(AnalyticsEvent.FormDownloadStarted, form.analyticsParams(query))
            runCatching { repository.downloadForm(form) }
                .onSuccess { document ->
                    analytics.logEvent(AnalyticsEvent.FormDownloadSucceeded, form.analyticsParams(query))
                    markFormDownloaded(form.id)
                    if (document.newlySaved) {
                        showMessage("Form saved to Documents/PostOfficeSaathi/Forms.")
                    }
                    _externalActions.emit(FormsExternalAction.SharePdf(document, form, query))
                }
                .onFailure { error ->
                    showMessage(error.downloadFailureMessage())
                    analytics.logEvent(AnalyticsEvent.FormDownloadFailed, form.analyticsParams(query, error))
                    analytics.recordError("form_download", error, form.analyticsParams(query))
                }
            clearActiveAction()
        }
    }

    fun onFormOpened(form: FormItem, query: String) {
        analytics.logEvent(AnalyticsEvent.FormOpened, form.analyticsParams(query))
    }

    fun onFormShared(form: FormItem, query: String) {
        analytics.logEvent(AnalyticsEvent.FormShared, form.analyticsParams(query))
    }

    fun onExternalActionFailed(area: String, error: Throwable, form: FormItem, query: String) {
        showMessage(
            when (area) {
                "form_open" -> "Could not open this form. Please try again."
                else -> "Could not share this form. Please try again."
            }
        )
        analytics.recordError(area, error, form.analyticsParams(query))
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(activeMessage = message, activeMessageId = it.activeMessageId + 1L) }
    }

    private fun setActiveAction(formId: String, action: FormActionKind) {
        _uiState.update { it.copy(activeFormId = formId, activeFormAction = action, activeMessage = null) }
    }

    private fun clearActiveAction() {
        _uiState.update { it.copy(activeFormId = null, activeFormAction = null) }
    }

    private fun markFormDownloaded(formId: String) {
        _uiState.update { state ->
            val loadForms = state.loadResult.forms.markDownloaded(formId)
            val visibleForms = state.visibleForms.markDownloaded(formId)
            state.copy(
                loadResult = state.loadResult.copy(forms = loadForms),
                visibleForms = visibleForms
            )
        }
    }

    private fun logSearch(query: String, resultCount: Int) {
        if (query.isBlank()) return
        val params = mapOf(
            AnalyticsParam.Flow to AnalyticsFlow.Forms,
            AnalyticsParam.QueryTextSafe to AnalyticsSanitizer.safeSearchText(query),
            AnalyticsParam.QueryLengthBucket to AnalyticsSanitizer.queryLengthBucket(query),
            AnalyticsParam.ResultCountBucket to AnalyticsSanitizer.countBucket(resultCount)
        )
        analytics.logEvent(AnalyticsEvent.FormSearch, params)
        if (resultCount == 0) {
            analytics.logEvent(AnalyticsEvent.FormSearchEmpty, params)
        }
    }

    class Factory(
        private val context: Context,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormsViewModel(
                repository = GitHubFormsRepository(context.applicationContext),
                analytics = analytics
            ) as T
        }
    }
}

private fun List<FormItem>.markDownloaded(formId: String): List<FormItem> =
    map { if (it.id == formId) it.copy(isDownloaded = true) else it }

private fun Throwable.downloadFailureMessage(): String =
    if (this is OfflineFormsException) {
        "No internet connection. Please try again when you're online."
    } else {
        "Could not download this form. Please try again."
    }

private fun FormItem.analyticsParams(query: String, throwable: Throwable? = null): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Forms,
        AnalyticsParam.FormId to id,
        AnalyticsParam.FormCategory to category,
        AnalyticsParam.FormLanguage to language,
        AnalyticsParam.QueryTextSafe to AnalyticsSanitizer.safeSearchText(query),
        AnalyticsParam.QueryLengthBucket to AnalyticsSanitizer.queryLengthBucket(query),
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )
