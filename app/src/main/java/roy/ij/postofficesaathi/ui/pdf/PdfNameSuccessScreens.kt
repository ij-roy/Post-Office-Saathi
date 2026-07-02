package roy.ij.postofficesaathi.ui.pdf

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Rational
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.key
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.pdf.PdfImageProcessor
import roy.ij.postofficesaathi.data.pdf.PdfGenerator
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementSnapper
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementSnapper.Guides
import roy.ij.postofficesaathi.ui.pdf.state.NormalizedCorner
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiChip
import roy.ij.postofficesaathi.ui.components.SaathiPrimaryButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import roy.ij.postofficesaathi.ui.components.SaathiSecondaryButton
import roy.ij.postofficesaathi.ui.components.ScreenHeader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt


@Composable
fun PdfNameInputScreen(
    analytics: SaathiAnalytics,
    state: PdfFlowUiState,
    viewModel: PdfFlowViewModel,
    onBack: () -> Unit,
    onPdfCreated: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.externalActions.collect { action ->
            if (action is PdfFlowExternalAction.PdfCreated) {
                onPdfCreated()
            }
        }
    }

    PdfPage(
        title = "File Name",
        subtitle = "Name and save the PDF.",
        onBack = onBack
    ) {
        FinalPdfSummaryCard(
            layoutType = state.selectedLayout,
            pageCount = state.capturedFilePaths.size
        )
        SaathiCard {
            SaathiChip("Final step", accent = MaterialTheme.colorScheme.secondary)
            Text("Customer details", style = MaterialTheme.typography.titleLarge)
            Text(
                "This name will be used to create the saved PDF file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = state.customerName,
                onValueChange = viewModel::updateCustomerName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Customer name") },
                placeholder = { Text("Example: Sita Devi") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
                )
            )
            FileNamePreview(customerName = state.customerName, layoutType = state.selectedLayout)
            AnimatedVisibility(
                visible = state.error != null || state.isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val message = state.processingMessage ?: state.error
                message?.let {
                    val isError = state.error != null
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.10f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        contentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        border = BorderStroke(
                            1.dp,
                            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                        )
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        SaathiPrimaryButton(
            text = "Save PDF",
            onClick = {
                viewModel.createPdf()
            },
            enabled = !state.isProcessing
        )
    }
}

@Composable
fun PdfCreatedSuccessScreen(
    analytics: SaathiAnalytics,
    pdfPath: String?,
    pdfName: String?,
    onCreateAnother: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current

    PdfPage(
        title = "PDF Created",
        subtitle = "Your document is ready.",
        onBack = onHome
    ) {
        PdfSuccessHero(fileName = pdfName ?: pdfPath?.substringAfterLast('/'))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SaathiSecondaryButton(
                "Open",
                onClick = {
                    pdfPath?.let {
                        runCatching { openPdf(context, it) }
                            .onSuccess {
                                analytics.logEvent(
                                    AnalyticsEvent.PdfOpened,
                                    mapOf(
                                        AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                                        AnalyticsParam.PdfFilename to pdfName
                                    )
                                )
                            }
                            .onFailure { error ->
                                val params = mapOf(
                                    AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                                    AnalyticsParam.PdfFilename to pdfName,
                                    AnalyticsParam.ErrorArea to "pdf_open",
                                    AnalyticsParam.ErrorType to error.javaClass.simpleName
                                )
                                analytics.logEvent(AnalyticsEvent.PdfOpenFailed, params)
                                analytics.recordError("pdf_open", error, params)
                            }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            SaathiSecondaryButton(
                "Share",
                onClick = {
                    pdfPath?.let {
                        runCatching { sharePdf(context, it) }
                            .onSuccess {
                                analytics.logEvent(
                                    AnalyticsEvent.PdfShared,
                                    mapOf(
                                        AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                                        AnalyticsParam.PdfFilename to pdfName
                                    )
                                )
                            }
                            .onFailure { error ->
                                val params = mapOf(
                                    AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                                    AnalyticsParam.PdfFilename to pdfName,
                                    AnalyticsParam.ErrorArea to "pdf_share",
                                    AnalyticsParam.ErrorType to error.javaClass.simpleName
                                )
                                analytics.logEvent(AnalyticsEvent.PdfShareFailed, params)
                                analytics.recordError("pdf_share", error, params)
                            }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        SaathiPrimaryButton("Create Another PDF", onCreateAnother)
        SaathiSecondaryButton(
            text = "Back to Home",
            onClick = onHome,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FinalPdfSummaryCard(
    layoutType: PdfLayoutType,
    pageCount: Int
) {
    SaathiCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PdfStackPreview(
                layoutType = layoutType,
                modifier = Modifier.size(width = 92.dp, height = 112.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SaathiChip("Ready to save")
                Text("PDF package", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${layoutType.documentLabels.size} card layout prepared from $pageCount photo${if (pageCount == 1) "" else "s"}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PdfStackPreview(
    layoutType: PdfLayoutType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(58.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(layoutType.documentLabels.size) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(5.dp),
                            color = if (it % 2 == 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                            }
                        ) {}
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PDF", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun FileNamePreview(
    customerName: String,
    layoutType: PdfLayoutType
) {
    val previewName = customerName
        .trim()
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "Customer" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "File preview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "${previewName}_${layoutType.fileLabel}_date.pdf",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PdfSuccessHero(fileName: String?) {
    SaathiCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SuccessMark(modifier = Modifier.size(76.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                SaathiChip("Saved", accent = MaterialTheme.colorScheme.secondary)
                Text("PDF created successfully", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Open it now, share it with the customer, or start another PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Saved file",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    fileName ?: "PDF file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SuccessMark(modifier: Modifier = Modifier) {
    val secondary = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = secondary.copy(alpha = 0.14f),
        border = BorderStroke(1.5.dp, secondary.copy(alpha = 0.24f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            val strokeWidth = 7f
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.18f, size.height * 0.54f),
                end = Offset(size.width * 0.42f, size.height * 0.78f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.42f, size.height * 0.78f),
                end = Offset(size.width * 0.84f, size.height * 0.22f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = secondary,
                start = Offset(size.width * 0.18f, size.height * 0.54f),
                end = Offset(size.width * 0.42f, size.height * 0.78f),
                strokeWidth = 4f
            )
            drawLine(
                color = secondary,
                start = Offset(size.width * 0.42f, size.height * 0.78f),
                end = Offset(size.width * 0.84f, size.height * 0.22f),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
private fun PdfPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(
                title = title,
                subtitle = subtitle,
                showChip = false,
                action = { PdfBackIconButton(onBack) }
            )
            content()
        }
    }
}

@Composable
private fun PdfBackIconButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun LayoutPreview(layoutType: PdfLayoutType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        layoutType.documentLabels.forEach { label ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.64f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun openPdf(context: Context, uriString: String) {
    val uri = uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}

private fun sharePdf(context: Context, uriString: String) {
    val uri = uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share PDF"))
}

internal fun String.toDocumentUri(context: Context): Uri {
    val uri = Uri.parse(this)
    if (uri.scheme == "content") return uri
    val file = if (uri.scheme == "file") File(uri.path.orEmpty()) else File(this)
    return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
}

