package roy.ij.postofficesaathi.ui.pdf

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.pdf.PdfCreationUseCase
import roy.ij.postofficesaathi.data.pdf.PdfImageProcessor
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import roy.ij.postofficesaathi.ui.pdf.state.NormalizedCorner
import roy.ij.postofficesaathi.ui.pdf.state.PdfFlowSavedStateCodec
import java.io.File

data class PdfFlowUiState(
    val selectedLayout: PdfLayoutType = PdfLayoutType.OneDocument,
    val capturedFilePaths: List<String> = emptyList(),
    val placements: List<PdfImagePlacement> = emptyList(),
    val createdPdfPath: String? = null,
    val createdPdfName: String? = null,
    val customerName: String = "",
    val workingImagePath: String? = null,
    val corners: List<NormalizedCorner> = PdfImageProcessor.defaultCorners(),
    val isProcessing: Boolean = false,
    val processingMessage: String? = null,
    val error: String? = null
) {
    val capturedFiles: List<File> get() = capturedFilePaths.map(::File)
}

sealed interface PdfFlowExternalAction {
    data class PdfCreated(val document: PublicDocumentRef) : PdfFlowExternalAction
}

class PdfFlowViewModel(
    private val context: Context,
    private val analytics: SaathiAnalytics,
    private val savedStateHandle: SavedStateHandle,
    private val imageProcessor: PdfImageProcessor = PdfImageProcessor(context),
    private val pdfCreationUseCase: PdfCreationUseCase = PdfCreationUseCase(context)
) : ViewModel() {
    private val _uiState = MutableStateFlow(readState())
    val uiState: StateFlow<PdfFlowUiState> = _uiState.asStateFlow()

    private val _externalActions = MutableSharedFlow<PdfFlowExternalAction>()
    val externalActions: SharedFlow<PdfFlowExternalAction> = _externalActions.asSharedFlow()

    fun selectLayout(layoutType: PdfLayoutType) {
        analytics.logEvent(
            AnalyticsEvent.PdfLayoutSelected,
            mapOf(
                AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                AnalyticsParam.LayoutType to layoutType.analyticsName()
            )
        )
        analytics.setContext(AnalyticsParam.LayoutType, layoutType.analyticsName())
        updateState(
            _uiState.value.copy(
                selectedLayout = layoutType,
                capturedFilePaths = emptyList(),
                placements = emptyList(),
                createdPdfPath = null,
                createdPdfName = null,
                customerName = "",
                workingImagePath = null,
                corners = PdfImageProcessor.defaultCorners(),
                error = null
            )
        )
    }

    fun setCapturedFiles(files: List<File>) {
        val paths = files.map { it.absolutePath }
        val placements = PdfPlacementFactory.defaultPlacements(paths.size, paths)
        updateState(_uiState.value.copy(capturedFilePaths = paths, placements = placements, error = null))
    }

    fun setPlacements(placements: List<PdfImagePlacement>) {
        updateState(_uiState.value.copy(placements = placements, error = null))
    }

    fun updateCustomerName(customerName: String) {
        updateState(_uiState.value.copy(customerName = customerName, error = null))
    }

    fun importGalleryImage(uri: Uri, label: String, onImported: (File) -> Unit) {
        viewModelScope.launch {
            setProcessing("Importing image...")
            runCatching {
                withContext(Dispatchers.IO) { imageProcessor.copyGalleryImageToCache(uri, label) }
            }.onSuccess { file ->
                clearProcessing()
                onImported(file)
            }.onFailure { error ->
                setError("Could not open this image. Please try another one.")
                analytics.recordError("gallery_import", error, pdfParams(_uiState.value.selectedLayout))
            }
        }
    }

    fun prepareCapturedPhoto(file: File, onPrepared: (File) -> Unit) {
        viewModelScope.launch {
            setProcessing("Preparing photo...")
            runCatching {
                withContext(Dispatchers.Default) { imageProcessor.rewriteImageRespectingExif(file) }
            }.onSuccess { prepared ->
                clearProcessing()
                onPrepared(prepared)
            }.onFailure { error ->
                clearProcessing()
                analytics.recordError("capture_prepare", error, pdfParams(_uiState.value.selectedLayout))
                onPrepared(file)
            }
        }
    }

    fun loadCorners(file: File) {
        updateState(
            _uiState.value.copy(
                workingImagePath = file.absolutePath,
                corners = PdfImageProcessor.defaultCorners(),
                isProcessing = true,
                processingMessage = "Detecting corners...",
                error = null
            )
        )
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.Default) { imageProcessor.detectDocumentCorners(file) }
            }
            result.onSuccess { corners ->
                analytics.logEvent(
                    AnalyticsEvent.CornerDetectionResult,
                    pdfParams(_uiState.value.selectedLayout) + mapOf(AnalyticsParam.UsedFallback to (corners == PdfImageProcessor.defaultCorners()))
                )
                updateState(_uiState.value.copy(corners = corners, isProcessing = false, processingMessage = null))
            }.onFailure { error ->
                analytics.recordError("corner_detection", error, pdfParams(_uiState.value.selectedLayout))
                updateState(_uiState.value.copy(corners = PdfImageProcessor.defaultCorners(), isProcessing = false, processingMessage = null))
            }
        }
    }

    fun updateCorners(corners: List<NormalizedCorner>) {
        updateState(_uiState.value.copy(corners = corners))
    }

    fun rotateWorkingImage(clockwise: Boolean) {
        val path = _uiState.value.workingImagePath ?: return
        viewModelScope.launch {
            analytics.logButtonTap(if (clockwise) "image_rotate_right" else "image_rotate_left", AnalyticsScreen.Capture)
            setProcessing("Rotating image...")
            runCatching {
                withContext(Dispatchers.Default) { imageProcessor.rotateImageFile(File(path), clockwise) }
            }.onSuccess { rotated ->
                clearProcessing()
                loadCorners(rotated)
            }.onFailure { error ->
                setError("Could not rotate image.")
                analytics.recordError("image_rotate", error, pdfParams(_uiState.value.selectedLayout))
            }
        }
    }

    fun createCorrectedImage(index: Int, onAdjusted: (File) -> Unit) {
        val path = _uiState.value.workingImagePath ?: return
        val corners = _uiState.value.corners
        viewModelScope.launch {
            setProcessing("Adjusting image...")
            runCatching {
                withContext(Dispatchers.Default) { imageProcessor.createCorrectedCardImage(File(path), corners, index) }
            }.onSuccess { corrected ->
                clearProcessing()
                analytics.logEvent(AnalyticsEvent.ImageAdjusted, pdfParams(_uiState.value.selectedLayout))
                onAdjusted(corrected)
            }.onFailure { error ->
                setError("Could not adjust this photo. Please try again.")
                analytics.recordError("image_adjust", error, pdfParams(_uiState.value.selectedLayout))
            }
        }
    }

    fun createPdf() {
        val state = _uiState.value
        viewModelScope.launch {
            analytics.logButtonTap("pdf_save", AnalyticsScreen.Name)
            analytics.logEvent(
                AnalyticsEvent.PdfCreateStarted,
                pdfParams(state.selectedLayout) + mapOf(AnalyticsParam.ImageCount to state.capturedFilePaths.size)
            )
            setProcessing("Creating PDF...")
            runCatching {
                withContext(Dispatchers.IO) {
                    pdfCreationUseCase.createPdf(
                        customerName = state.customerName,
                        layoutType = state.selectedLayout,
                        imagePaths = state.capturedFilePaths,
                        placements = state.placements
                    )
                }
            }.onSuccess { document ->
                analytics.logEvent(
                    AnalyticsEvent.PdfCreateSucceeded,
                    pdfParams(state.selectedLayout) + mapOf(AnalyticsParam.ImageCount to state.capturedFilePaths.size)
                )
                updateState(
                    _uiState.value.copy(
                        createdPdfPath = document.uriString,
                        createdPdfName = document.displayName,
                        isProcessing = false,
                        processingMessage = null
                    )
                )
                _externalActions.emit(PdfFlowExternalAction.PdfCreated(document))
            }.onFailure { error ->
                setError("Could not create PDF. Please try again.")
                analytics.logEvent(AnalyticsEvent.PdfCreateFailed, pdfParams(state.selectedLayout, error))
                analytics.recordError("pdf_create", error, pdfParams(state.selectedLayout))
            }
        }
    }

    private fun setProcessing(message: String) {
        updateState(_uiState.value.copy(isProcessing = true, processingMessage = message, error = null))
    }

    private fun clearProcessing() {
        updateState(_uiState.value.copy(isProcessing = false, processingMessage = null))
    }

    private fun setError(message: String) {
        updateState(_uiState.value.copy(isProcessing = false, processingMessage = null, error = message))
    }

    private fun updateState(state: PdfFlowUiState) {
        _uiState.value = state
        savedStateHandle[KeyLayout] = state.selectedLayout.name
        savedStateHandle[KeyCapturedPaths] = ArrayList(state.capturedFilePaths)
        savedStateHandle[KeyPlacements] = PdfFlowSavedStateCodec.encodePlacements(state.placements)
        savedStateHandle[KeyCreatedPdfPath] = state.createdPdfPath
        savedStateHandle[KeyCreatedPdfName] = state.createdPdfName
        savedStateHandle[KeyCustomerName] = state.customerName
        savedStateHandle[KeyWorkingImagePath] = state.workingImagePath
    }

    private fun readState(): PdfFlowUiState {
        val layout = savedStateHandle.get<String>(KeyLayout)
            ?.let { runCatching { PdfLayoutType.valueOf(it) }.getOrNull() }
            ?: PdfLayoutType.OneDocument
        val capturedPaths = savedStateHandle.get<ArrayList<String>>(KeyCapturedPaths).orEmpty()
        val placements = savedStateHandle.get<ArrayList<String>>(KeyPlacements)
            ?.let(PdfFlowSavedStateCodec::decodePlacements)
            .orEmpty()
        return PdfFlowUiState(
            selectedLayout = layout,
            capturedFilePaths = capturedPaths,
            placements = placements,
            createdPdfPath = savedStateHandle[KeyCreatedPdfPath],
            createdPdfName = savedStateHandle[KeyCreatedPdfName],
            customerName = savedStateHandle[KeyCustomerName] ?: "",
            workingImagePath = savedStateHandle[KeyWorkingImagePath]
        )
    }

    class Factory(
        private val context: Context,
        private val analytics: SaathiAnalytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PdfFlowViewModel(
                context = context.applicationContext,
                analytics = analytics,
                savedStateHandle = extras.createSavedStateHandle()
            ) as T
        }
    }

    companion object {
        private const val KeyLayout = "layout"
        private const val KeyCapturedPaths = "captured_paths"
        private const val KeyPlacements = "placements"
        private const val KeyCreatedPdfPath = "created_pdf_path"
        private const val KeyCreatedPdfName = "created_pdf_name"
        private const val KeyCustomerName = "customer_name"
        private const val KeyWorkingImagePath = "working_image_path"
    }
}

fun PdfLayoutType.analyticsName(): String =
    when (this) {
        PdfLayoutType.OneDocument -> "one_document"
        PdfLayoutType.TwoDocuments -> "two_documents"
        PdfLayoutType.ThreeCards -> "three_cards"
    }

fun pdfParams(layoutType: PdfLayoutType, throwable: Throwable? = null): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Pdf,
        AnalyticsParam.LayoutType to layoutType.analyticsName(),
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )
