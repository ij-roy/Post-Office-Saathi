package roy.ij.postofficesaathi.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import roy.ij.postofficesaathi.data.recent.RecentWorkItem
import roy.ij.postofficesaathi.data.recent.RecentWorkRepository

data class HomeUiState(
    val recentItems: List<RecentWorkItem> = emptyList(),
    val message: String? = null,
    val messageId: Long = 0L
)

sealed interface HomeExternalAction {
    data class Open(val item: RecentWorkItem) : HomeExternalAction
    data class Share(val item: RecentWorkItem) : HomeExternalAction
}

class HomeViewModel(
    private val repository: RecentWorkRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _externalActions = MutableSharedFlow<HomeExternalAction>()
    val externalActions: SharedFlow<HomeExternalAction> = _externalActions.asSharedFlow()

    fun refreshRecentWork() {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { repository.loadRecentWork() }
            _uiState.value = _uiState.value.copy(recentItems = items)
        }
    }

    fun openRecent(item: RecentWorkItem) {
        viewModelScope.launch { _externalActions.emit(HomeExternalAction.Open(item)) }
    }

    fun shareRecent(item: RecentWorkItem) {
        viewModelScope.launch { _externalActions.emit(HomeExternalAction.Share(item)) }
    }

    fun onExternalActionFailed() {
        _uiState.value = _uiState.value.copy(
            message = "Could not open this file. Please try from file manager.",
            messageId = _uiState.value.messageId + 1L
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(RecentWorkRepository(context.applicationContext)) as T
        }
    }
}
