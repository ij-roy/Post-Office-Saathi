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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import roy.ij.postofficesaathi.data.pdf.PdfGenerator
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementSnapper
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementSnapper.Guides
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
fun PdfLayoutSelectionScreen(
    onBack: () -> Unit,
    onLayoutSelected: (PdfLayoutType) -> Unit
) {
    SaathiScreen {
        Box(modifier = Modifier.fillMaxSize()) {
            BlurredTemplateBackground()
            Surface(
                onClick = onBack,
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {}

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PagePadding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose the layout",
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TemplateOptionCard(
                                title = "1 card",
                                layoutType = PdfLayoutType.OneDocument,
                                modifier = Modifier.weight(1f),
                                onClick = onLayoutSelected
                            )
                            TemplateOptionCard(
                                title = "2 cards",
                                layoutType = PdfLayoutType.TwoDocuments,
                                modifier = Modifier.weight(1f),
                                onClick = onLayoutSelected
                            )
                            TemplateOptionCard(
                                title = "3 cards",
                                layoutType = PdfLayoutType.ThreeCards,
                                modifier = Modifier.weight(1f),
                                onClick = onLayoutSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlurredTemplateBackground() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .blur(10.dp)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Create PDF",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
        )
        repeat(4) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.44f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {}
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
        ) {}
    }
}

@Composable
private fun TemplateOptionCard(
    title: String,
    layoutType: PdfLayoutType,
    modifier: Modifier = Modifier,
    onClick: (PdfLayoutType) -> Unit
) {
    Surface(
        onClick = { onClick(layoutType) },
        modifier = modifier.heightIn(min = 176.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.76f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TemplatePreview(layoutType)
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TemplatePreview(layoutType: PdfLayoutType) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.055f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.86f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(layoutType.documentLabels.size) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.76f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {}
                if (it != layoutType.documentLabels.lastIndex) {
                    Box(modifier = Modifier.height(7.dp))
                }
            }
        }
    }
}

@Composable
fun DocumentCaptureScreen(
    layoutType: PdfLayoutType,
    onBack: () -> Unit,
    onCaptureComplete: (List<File>) -> Unit
) {
    val context = LocalContext.current
    val hasInitialPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    var hasCameraPermission by remember { mutableStateOf(hasInitialPermission) }
    var permissionRequested by remember { mutableStateOf(false) }
    var currentIndex by remember(layoutType) { mutableStateOf(0) }
    var currentCapture by remember(layoutType) { mutableStateOf<File?>(null) }
    var photoToAdjust by remember(layoutType) { mutableStateOf<File?>(null) }
    var capturedFiles by remember(layoutType) { mutableStateOf<List<File>>(emptyList()) }
    var captureError by remember { mutableStateOf<String?>(null) }
    val currentLabel = layoutType.documentLabels[currentIndex]
    val imageCapture = remember { ImageCapture.Builder().build() }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionRequested = true
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            runCatching {
                copyGalleryImageToCache(context, it, currentLabel)
            }.onSuccess { file ->
                currentCapture = file
                photoToAdjust = file
                captureError = null
            }.onFailure {
                captureError = "Could not open this image. Please try another one."
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequested) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    photoToAdjust?.let { file ->
        ImmediateCornerCorrectionScreen(
            file = file,
            progressText = "${currentIndex + 1}/${layoutType.documentLabels.size}",
            onBack = { photoToAdjust = null },
            onRetake = {
                file.delete()
                currentCapture = null
                photoToAdjust = null
            },
            onAdjusted = { corrected ->
                val nextFiles = capturedFiles + corrected
                if (currentIndex == layoutType.documentLabels.lastIndex) {
                    onCaptureComplete(nextFiles)
                } else {
                    capturedFiles = nextFiles
                    currentCapture = null
                    photoToAdjust = null
                    currentIndex += 1
                }
            }
        )
        return
    }

    if (!hasCameraPermission) {
        SaathiScreen {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PagePadding),
                contentAlignment = Alignment.Center
            ) {
                SaathiCard {
                    Text("Camera permission needed", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Allow camera access to capture card photos for the PDF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SaathiPrimaryButton(
                        text = "Allow Camera",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        if (currentCapture == null) {
            CameraPreview(
                imageCapture = imageCapture,
                modifier = Modifier.fillMaxSize()
            )
            CameraGuideOverlay(
                progressText = "${currentIndex + 1}/${layoutType.documentLabels.size}",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CapturedImageBackground(
                file = currentCapture,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraIconButton(text = "‹", onClick = onBack)
        }

        AnimatedVisibility(
            visible = captureError != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
        ) {
            captureError?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(it, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
        }

        if (currentCapture == null) {
            CaptureButton(
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 34.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 28.dp)
                },
                onClick = {
                    captureError = null
                    capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        label = currentLabel,
                        onSuccess = {
                            currentCapture = it
                            photoToAdjust = it
                        },
                        onError = { captureError = "Could not capture photo. Please try again." }
                    )
                }
            )
            CameraPillButton(
                text = "Gallery",
                onClick = { galleryLauncher.launch("image/*") },
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 34.dp)
                        .width(132.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(start = 18.dp, bottom = 28.dp)
                        .width(118.dp)
                }
            )
        } else {
            Row(
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .width(210.dp)
                        .padding(end = 18.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 24.dp)
                },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CameraPillButton(
                    text = "Retake",
                    onClick = {
                        currentCapture?.delete()
                        currentCapture = null
                        captureError = null
                    },
                    modifier = Modifier.weight(1f)
                )
                CameraPillButton(
                    text = if (currentIndex == layoutType.documentLabels.lastIndex) "Review" else "Use photo",
                    onClick = {
                        photoToAdjust = currentCapture
                    },
                    modifier = Modifier.weight(1f),
                    primary = true
                )
            }
        }
    }
}

@Composable
fun CornerAdjustmentScreen(
    layoutType: PdfLayoutType,
    capturedFiles: List<File>,
    onBack: () -> Unit,
    onAdjusted: (List<File>) -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember(capturedFiles) { mutableStateOf(0) }
    var adjustedFiles by remember(capturedFiles) { mutableStateOf<List<File>>(emptyList()) }
    var corners by remember(currentIndex, capturedFiles) { mutableStateOf(defaultCorners()) }
    var error by remember { mutableStateOf<String?>(null) }
    val currentFile = capturedFiles.getOrNull(currentIndex)
    val progressText = "${currentIndex + 1}/${layoutType.documentLabels.size}"

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentFile != null) {
            CornerAdjustmentCanvas(
                file = currentFile,
                corners = corners,
                onCornersChanged = { corners = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraIconButton(text = "<", onClick = onBack)
            Text(progressText, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.34f),
            contentColor = Color.White
        ) {
            Text(
                "Drag the four points to the card corners",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 126.dp, start = 16.dp, end = 16.dp)
        ) {
            error?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(it, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CameraPillButton("Retake", onBack, Modifier.weight(1f))
            CameraPillButton(
                text = if (currentIndex == capturedFiles.lastIndex) "Apply" else "Next",
                onClick = {
                    val source = currentFile ?: return@CameraPillButton
                    runCatching {
                        createCorrectedCardImage(context, source, corners, currentIndex)
                    }.onSuccess { corrected ->
                        val nextFiles = adjustedFiles + corrected
                        if (currentIndex == capturedFiles.lastIndex) {
                            onAdjusted(nextFiles)
                        } else {
                            adjustedFiles = nextFiles
                            currentIndex += 1
                            error = null
                        }
                    }.onFailure {
                        error = "Could not adjust this photo. Please try again."
                    }
                },
                modifier = Modifier.weight(1f),
                primary = true
            )
        }
    }
}

@Composable
private fun ImmediateCornerCorrectionScreen(
    file: File,
    progressText: String,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onAdjusted: (File) -> Unit
) {
    val context = LocalContext.current
    var workingFile by remember(file.absolutePath) { mutableStateOf(file) }
    var corners by remember(workingFile.absolutePath) { mutableStateOf(detectDocumentCorners(workingFile)) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CornerAdjustmentCanvas(
            file = workingFile,
            corners = corners,
            onCornersChanged = { corners = it },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraIconButton(text = "<", onClick = onBack)
            Text(progressText, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.34f),
            contentColor = Color.White
        ) {
            Text(
                "Adjust corners",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 126.dp, start = 16.dp, end = 16.dp)
        ) {
            error?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(it, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CameraPillButton("Retake", onRetake, Modifier.weight(1.05f))
            CameraPillButton(
                "Left",
                onClick = {
                    runCatching { rotateImageFile(context, workingFile, clockwise = false) }
                        .onSuccess { workingFile = it }
                        .onFailure { error = "Could not rotate image." }
                },
                modifier = Modifier.weight(0.8f)
            )
            CameraPillButton(
                "Right",
                onClick = {
                    runCatching { rotateImageFile(context, workingFile, clockwise = true) }
                        .onSuccess { workingFile = it }
                        .onFailure { error = "Could not rotate image." }
                },
                modifier = Modifier.weight(0.8f)
            )
            CameraPillButton(
                "Apply",
                onClick = {
                    runCatching {
                        createCorrectedCardImage(context, workingFile, corners, 0)
                    }.onSuccess(onAdjusted)
                        .onFailure { error = "Could not adjust this photo. Please try again." }
                },
                modifier = Modifier.weight(1f),
                primary = true
            )
        }
    }
}

@Composable
fun PdfPreviewEditorScreen(
    layoutType: PdfLayoutType,
    capturedFiles: List<File>,
    placements: List<PdfImagePlacement>,
    onBack: () -> Unit,
    onContinue: (List<PdfImagePlacement>) -> Unit
) {
    var currentPlacements by remember(placements) { mutableStateOf(placements) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var undoStack by remember(placements) { mutableStateOf<List<List<PdfImagePlacement>>>(emptyList()) }

    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("PDF Preview", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Adjust images as required.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val a4Ratio = 595f / 842f
                val pageWidth = minOf(maxWidth, maxHeight * a4Ratio)
                val pageHeight = pageWidth / a4Ratio

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.width(pageWidth),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UndoIconButton(
                            enabled = undoStack.isNotEmpty(),
                            onClick = {
                                val previous = undoStack.lastOrNull() ?: return@UndoIconButton
                                undoStack = undoStack.dropLast(1)
                                currentPlacements = previous
                            }
                        )
                        TextButton(
                            onClick = {
                                undoStack = undoStack + listOf(currentPlacements)
                                currentPlacements = PdfPlacementFactory.reset(currentPlacements)
                                selectedIndex = null
                            }
                        ) {
                            Text("Reset", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(pageWidth)
                            .height(pageHeight)
                            .shadow(6.dp)
                            .background(Color.White)
                    ) {
                        A4PlacementEditor(
                            capturedFiles = capturedFiles,
                            placements = currentPlacements,
                            selectedIndex = selectedIndex,
                            onSelected = { selectedIndex = it },
                            onPlacementChanged = { index, placement ->
                                val nextPlacements = currentPlacements.toMutableList().also {
                                    it[index] = placement.clamped()
                                }
                                if (nextPlacements != currentPlacements) {
                                    undoStack = undoStack + listOf(currentPlacements)
                                    currentPlacements = nextPlacements
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            SaathiPrimaryButton("Create PDF", { onContinue(currentPlacements) })
        }
    }
}

@Composable
private fun UndoIconButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val iconColor = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.34f)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = if (enabled) 0.96f else 0.62f),
        border = BorderStroke(1.4.dp, iconColor.copy(alpha = 0.32f)),
        contentColor = iconColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun A4PlacementEditor(
    capturedFiles: List<File>,
    placements: List<PdfImagePlacement>,
    selectedIndex: Int?,
    onSelected: (Int?) -> Unit,
    onPlacementChanged: (Int, PdfImagePlacement) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val pageWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val pageHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        var activeGuides by remember { mutableStateOf(Guides()) }

        if (activeGuides.hasGuides) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                activeGuides.vertical.forEach { x ->
                    drawLine(
                        color = Color(0xFF2F80ED).copy(alpha = 0.34f),
                        start = Offset(x * size.width, 0f),
                        end = Offset(x * size.width, size.height),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }
                activeGuides.horizontal.forEach { y ->
                    drawLine(
                        color = Color(0xFF2F80ED).copy(alpha = 0.28f),
                        start = Offset(0f, y * size.height),
                        end = Offset(size.width, y * size.height),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }
            }
        }

        placements.forEachIndexed { index, placement ->
            val file = capturedFiles.getOrNull(index)
            val bitmap = remember(file?.absolutePath) { file?.let(::decodeBitmapRespectingExif) }
            val isSelected = selectedIndex == index
            key(index, file?.absolutePath) {
                EditablePdfPlacement(
                    bitmap = bitmap,
                    placement = placement,
                    allPlacements = placements,
                    index = index,
                    isSelected = isSelected,
                    pageWidthPx = pageWidthPx,
                    pageHeightPx = pageHeightPx,
                    onSelected = onSelected,
                    onGuidesChanged = { activeGuides = it },
                    onPlacementChanged = { onPlacementChanged(index, it) }
                )
            }
        }
    }
}

@Composable
private fun EditablePdfPlacement(
    bitmap: Bitmap?,
    placement: PdfImagePlacement,
    allPlacements: List<PdfImagePlacement>,
    index: Int,
    isSelected: Boolean,
    pageWidthPx: Float,
    pageHeightPx: Float,
    onSelected: (Int?) -> Unit,
    onGuidesChanged: (Guides) -> Unit,
    onPlacementChanged: (PdfImagePlacement) -> Unit
) {
    val density = LocalDensity.current
    var displayPlacement by remember(placement) { mutableStateOf(placement) }
    var rawDragPlacement by remember(placement) { mutableStateOf(placement) }
    var dragHasActiveSnap by remember { mutableStateOf(false) }
    var activeSnap by remember { mutableStateOf(PdfPlacementSnapper.ActiveSnap()) }
    val latestDisplayPlacement by rememberUpdatedState(displayPlacement)
    val latestRawDragPlacement by rememberUpdatedState(rawDragPlacement)
    val latestDragHasActiveSnap by rememberUpdatedState(dragHasActiveSnap)
    val latestActiveSnap by rememberUpdatedState(activeSnap)
    val latestAllPlacements by rememberUpdatedState(allPlacements)

    LaunchedEffect(placement) {
        displayPlacement = placement
        rawDragPlacement = placement
        dragHasActiveSnap = false
        activeSnap = PdfPlacementSnapper.ActiveSnap()
    }

    val cardLeftPx = displayPlacement.x * pageWidthPx
    val cardTopPx = displayPlacement.y * pageHeightPx
    val cardWidthPx = displayPlacement.width * pageWidthPx
    val cardHeightPx = displayPlacement.height * pageHeightPx

    fun updateDisplay(next: PdfImagePlacement, guides: Guides = Guides()) {
        displayPlacement = next.clamped()
        onGuidesChanged(guides)
    }

    fun commit() {
        val committed = if (latestDragHasActiveSnap) {
            latestDisplayPlacement
        } else {
            latestRawDragPlacement
        }
        onPlacementChanged(committed.clamped())
        dragHasActiveSnap = false
        activeSnap = PdfPlacementSnapper.ActiveSnap()
        onGuidesChanged(Guides())
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(cardLeftPx.roundToInt(), cardTopPx.roundToInt()) }
            .size(
                with(density) { cardWidthPx.toDp() },
                with(density) { cardHeightPx.toDp() }
            )
            .graphicsLayer { rotationZ = displayPlacement.rotationDegrees }
            .pointerInput(index, pageWidthPx, pageHeightPx) {
                var dragStartPlacement = displayPlacement
                var totalDrag = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        onSelected(index)
                        dragStartPlacement = latestDisplayPlacement
                        totalDrag = Offset.Zero
                    },
                    onDragEnd = { commit() },
                    onDragCancel = { commit() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                        val moved = dragStartPlacement.copy(
                            x = dragStartPlacement.x + totalDrag.x / pageWidthPx,
                            y = dragStartPlacement.y + totalDrag.y / pageHeightPx
                        ).clamped()
                        val snap = PdfPlacementSnapper.snapPlacement(
                            placement = moved,
                            placements = latestAllPlacements,
                            activeIndex = index,
                            previousRawPlacement = latestRawDragPlacement,
                            activeSnap = latestActiveSnap
                        )
                        rawDragPlacement = moved
                        dragHasActiveSnap = snap.isSnapped
                        activeSnap = snap.activeSnap
                        updateDisplay(
                            next = if (snap.isSnapped) snap.placement else moved,
                            guides = if (snap.isSnapped) snap.guides else Guides()
                        )
                    }
                )
            }
    ) {
        PdfPlacedImage(
            bitmap = bitmap,
            placement = displayPlacement,
            isSelected = isSelected,
            modifier = Modifier.fillMaxSize()
        )

        if (isSelected) {
            PdfEditHandles(
                placement = displayPlacement,
                onResize = { corner, delta ->
                    val resized = resizePlacement(
                        placement = latestDisplayPlacement,
                        corner = corner,
                        dx = delta.x / pageWidthPx,
                        dy = delta.y / pageHeightPx
                    )
                    val snap = PdfPlacementSnapper.snapPlacement(resized, latestAllPlacements, index)
                    updateDisplay(snap.placement, snap.guides)
                },
                onResizeFinished = { commit() },
                onRotate = { delta ->
                    updateDisplay(
                        latestDisplayPlacement.copy(
                            rotationDegrees = latestDisplayPlacement.rotationDegrees + delta.x * 0.35f
                        )
                    )
                },
                onRotateFinished = { commit() }
            )
        }
    }
}

@Composable
private fun PdfPlacedImage(
    bitmap: Bitmap?,
    placement: PdfImagePlacement,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (bitmap == null) return@Canvas
            val src = Rect(
                (placement.cropLeft * bitmap.width).roundToInt(),
                (placement.cropTop * bitmap.height).roundToInt(),
                (placement.cropRight * bitmap.width).roundToInt(),
                (placement.cropBottom * bitmap.height).roundToInt()
            )
            val dst = RectF(0f, 0f, size.width, size.height)
            drawIntoCanvas {
                it.nativeCanvas.drawBitmap(
                    bitmap,
                    src,
                    dst,
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                )
            }
        }

        if (isSelected) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = Color(0xFFB00010), style = Stroke(width = 2.5f))
            }
        }
    }
}

@Composable
private fun BoxScope.PdfEditHandles(
    placement: PdfImagePlacement,
    onResize: (ResizeCorner, Offset) -> Unit,
    onResizeFinished: () -> Unit,
    onRotate: (Offset) -> Unit,
    onRotateFinished: () -> Unit
) {
    ResizeHandle(ResizeCorner.TopLeft, Modifier.align(Alignment.TopStart), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.TopRight, Modifier.align(Alignment.TopEnd), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.BottomLeft, Modifier.align(Alignment.BottomStart), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.BottomRight, Modifier.align(Alignment.BottomEnd), onResize, onResizeFinished)

    Surface(
        modifier = Modifier
            .offset(x = 28.dp, y = (-30).dp)
            .align(Alignment.TopEnd)
            .size(30.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onRotateFinished,
                    onDragCancel = onRotateFinished,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onRotate(dragAmount)
                    }
                )
            },
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.96f),
        border = BorderStroke(2.dp, Color(0xFFB00010))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("R", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB00010))
        }
    }
}

@Composable
private fun BoxScope.ResizeHandle(
    corner: ResizeCorner,
    modifier: Modifier,
    onResize: (ResizeCorner, Offset) -> Unit,
    onResizeFinished: () -> Unit
) {
    Surface(
        modifier = modifier
            .offset(
                x = when (corner) {
                    ResizeCorner.TopLeft, ResizeCorner.BottomLeft -> (-8).dp
                    ResizeCorner.TopRight, ResizeCorner.BottomRight -> 8.dp
                },
                y = when (corner) {
                    ResizeCorner.TopLeft, ResizeCorner.TopRight -> (-8).dp
                    ResizeCorner.BottomLeft, ResizeCorner.BottomRight -> 8.dp
                }
            )
            .size(18.dp)
            .pointerInput(corner) {
                detectDragGestures(
                    onDragEnd = onResizeFinished,
                    onDragCancel = onResizeFinished,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onResize(corner, dragAmount)
                    }
                )
            },
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(2.dp, Color(0xFFB00010))
    ) {}
}

private enum class ResizeCorner { TopLeft, TopRight, BottomLeft, BottomRight }

private fun resizePlacement(
    placement: PdfImagePlacement,
    corner: ResizeCorner,
    dx: Float,
    dy: Float
): PdfImagePlacement {
    val aspect = 1.585f
    val signedDelta = when (corner) {
        ResizeCorner.BottomRight -> maxOf(dx, dy * aspect)
        ResizeCorner.TopRight -> maxOf(dx, -dy * aspect)
        ResizeCorner.BottomLeft -> maxOf(-dx, dy * aspect)
        ResizeCorner.TopLeft -> maxOf(-dx, -dy * aspect)
    }
    val newWidth = (placement.width + signedDelta).coerceIn(0.08f, 0.9f)
    val newHeight = newWidth / aspect
    val xShift = when (corner) {
        ResizeCorner.TopLeft, ResizeCorner.BottomLeft -> placement.width - newWidth
        else -> 0f
    }
    val yShift = when (corner) {
        ResizeCorner.TopLeft, ResizeCorner.TopRight -> placement.height - newHeight
        else -> 0f
    }
    return placement.copy(
        x = placement.x + xShift,
        y = placement.y + yShift,
        width = newWidth,
        height = newHeight
    ).clamped()
}

@Composable
fun PdfNameInputScreen(
    layoutType: PdfLayoutType,
    capturedFiles: List<File>,
    placements: List<PdfImagePlacement>,
    onBack: () -> Unit,
    onPdfCreated: (File) -> Unit
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    PdfPage(
        title = "File Name",
        subtitle = "Enter the customer name for this PDF.",
        onBack = onBack
    ) {
        SaathiChip("Final step")
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Customer name") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
        SaathiPrimaryButton(
            text = "Save PDF",
            onClick = {
                runCatching {
                    PdfGenerator.createPdf(context, customerName, layoutType, capturedFiles, placements)
                }.onSuccess(onPdfCreated)
                    .onFailure { error = "Could not create PDF. Please try again." }
            }
        )
    }
}

@Composable
fun PdfCreatedSuccessScreen(
    pdfPath: String?,
    onCreateAnother: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val file = pdfPath?.let(::File)

    PdfPage(
        title = "PDF Created",
        subtitle = file?.name ?: "Your PDF is ready.",
        onBack = onHome
    ) {
        SaathiCard {
            SaathiChip("Ready", accent = MaterialTheme.colorScheme.secondary)
            Text("PDF created successfully", style = MaterialTheme.typography.titleLarge)
            Text(
                "Open, share, print from another app, or create another PDF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SaathiSecondaryButton(
                "Open",
                onClick = { file?.let { openPdf(context, it) } },
                modifier = Modifier.weight(1f)
            )
            SaathiSecondaryButton(
                "Share",
                onClick = { file?.let { sharePdf(context, it) } },
                modifier = Modifier.weight(1f)
            )
        }
        SaathiPrimaryButton("Create Another PDF", onCreateAnother)
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
        color = Color.White.copy(alpha = 0.64f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("‹", style = MaterialTheme.typography.headlineMedium)
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

@Composable
private fun CameraIconButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.34f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun CaptureButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(132.dp)
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.46f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("Capture", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CameraPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (primary) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.34f),
        contentColor = if (primary) MaterialTheme.colorScheme.primary else Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (primary) 0.46f else 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLifecycleOwner by rememberUpdatedState(lifecycleOwner)

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture.targetRotation = previewView.display.rotation
                    runCatching {
                        cameraProvider.unbindAll()
                        val useCaseGroup = androidx.camera.core.UseCaseGroup.Builder()
                            .addUseCase(preview)
                            .addUseCase(imageCapture)
                            .setViewPort(
                                ViewPort.Builder(
                                    Rational(previewView.width.coerceAtLeast(1), previewView.height.coerceAtLeast(1)),
                                    previewView.display.rotation
                                ).setScaleType(ViewPort.FILL_CENTER).build()
                            )
                            .build()
                        cameraProvider.bindToLifecycle(currentLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, useCaseGroup)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    )
}

@Composable
private fun CameraGuideOverlay(
    progressText: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val isLandscape = maxWidth > maxHeight
        val guideRatio = 1.585f
        val maxAllowedWidth = if (isLandscape) maxWidth * 0.52f else maxWidth * 0.82f
        val maxAllowedHeight = if (isLandscape) maxHeight * 0.5f else maxHeight * 0.32f
        val guideWidth = minOf(maxAllowedWidth, maxAllowedHeight * guideRatio)
        val guideHeight = guideWidth / guideRatio
        val infiniteTransition = rememberInfiniteTransition(label = "guideHint")
        val hintAlpha by infiniteTransition.animateFloat(
            initialValue = 0.34f,
            targetValue = 0.72f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300),
                repeatMode = RepeatMode.Reverse
            ),
            label = "guideHintAlpha"
        )

        Box(
            modifier = Modifier
                .width(guideWidth)
                .height(guideHeight + 28.dp)
        ) {
            Text(
                text = progressText,
                modifier = Modifier.align(Alignment.TopEnd),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(guideWidth)
                    .height(guideHeight),
                color = Color.Transparent,
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.88f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.graphicsLayer { alpha = hintAlpha },
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                    ) {
                        Text(
                            text = "Try to fit the card inside this rectangle",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapturedImagePreview(
    file: File?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(file?.absolutePath) {
        file?.let(::decodeBitmapRespectingExif)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        if (bitmap == null) {
            Box(contentAlignment = Alignment.Center) {
                Text("Photo unavailable")
            }
        } else {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CapturedImageBackground(
    file: File?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(file?.absolutePath) {
        file?.let(::decodeBitmapRespectingExif)
    }

    if (bitmap == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Photo unavailable", color = Color.White)
        }
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun CornerAdjustmentCanvas(
    file: File,
    corners: List<NormalizedCorner>,
    onCornersChanged: (List<NormalizedCorner>) -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(file.absolutePath) { decodeBitmapRespectingExif(file) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var activeCorner by remember { mutableStateOf<Int?>(null) }
    var localCorners by remember(file.absolutePath) { mutableStateOf(corners) }
    LaunchedEffect(corners, activeCorner) {
        if (activeCorner == null && localCorners != corners) {
            localCorners = corners
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            .pointerInput(containerSize, bitmap) {
                detectDragGestures(
                    onDragStart = { touch ->
                        val rect = bitmap?.let { fittedImageRect(containerSize, it.width, it.height) }
                        activeCorner = rect?.let {
                            localCorners
                                .mapIndexed { index, corner -> index to corner.toScreenOffset(it) }
                                .minByOrNull { (_, offset) -> (offset - touch).getDistance() }
                                ?.takeIf { (_, offset) -> (offset - touch).getDistance() < 96f }
                                ?.first
                        }
                    },
                    onDragEnd = {
                        onCornersChanged(localCorners)
                        activeCorner = null
                    },
                    onDragCancel = {
                        onCornersChanged(localCorners)
                        activeCorner = null
                    },
                    onDrag = { change, dragAmount ->
                        val rect = bitmap?.let { fittedImageRect(containerSize, it.width, it.height) }
                        val index = activeCorner
                        if (rect != null && index != null) {
                            change.consume()
                            val current = localCorners[index].toScreenOffset(rect)
                            val updatedPoint = current + dragAmount
                            localCorners = localCorners.toMutableList().also {
                                it[index] = updatedPoint.toNormalizedCorner(rect)
                            }
                        }
                    }
                )
            }
    ) {
        if (bitmap == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Photo unavailable", color = Color.White)
            }
            return@Box
        }

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val rect = fittedImageRect(containerSize, bitmap.width, bitmap.height)
            val points = localCorners.map { it.toScreenOffset(rect) }
            points.forEachIndexed { index, point ->
                val next = points[(index + 1) % points.size]
                drawLine(
                    color = Color.White,
                    start = point,
                    end = next,
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color(0xFFB00010),
                    start = point,
                    end = next,
                    strokeWidth = 2f
                )
            }
            points.forEachIndexed { index, point ->
                val isActive = index == activeCorner
                drawCircle(color = Color.White, radius = if (isActive) 30f else 24f, center = point)
                drawCircle(
                    color = Color(0xFFB00010),
                    radius = if (isActive) 23f else 18f,
                    center = point,
                    style = Stroke(width = 4f)
                )
            }
        }

        activeCorner?.let { index ->
            CornerLoupe(
                bitmap = bitmap,
                sourcePoint = localCorners[index],
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 88.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun CornerLoupe(
    bitmap: Bitmap,
    sourcePoint: NormalizedCorner,
    modifier: Modifier = Modifier
) {
    val cropSize = 180
    val centerX = (sourcePoint.x * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1)
    val centerY = (sourcePoint.y * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1)
    val left = (centerX - cropSize / 2).coerceIn(0, maxOf(0, bitmap.width - cropSize))
    val top = (centerY - cropSize / 2).coerceIn(0, maxOf(0, bitmap.height - cropSize))
    val actualWidth = minOf(cropSize, bitmap.width - left)
    val actualHeight = minOf(cropSize, bitmap.height - top)
    val loupeBitmap = remember(sourcePoint.x, sourcePoint.y, bitmap) {
        Bitmap.createBitmap(bitmap, left, top, actualWidth, actualHeight)
    }

    Surface(
        modifier = modifier.size(132.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.44f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.72f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                bitmap = loupeBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawLine(Color.White, Offset(center.x - 24f, center.y), Offset(center.x + 24f, center.y), 3f)
                drawLine(Color.White, Offset(center.x, center.y - 24f), Offset(center.x, center.y + 24f), 3f)
                drawCircle(Color(0xFFB00010), 8f, center, style = Stroke(width = 3f))
            }
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    label: String,
    onSuccess: (File) -> Unit,
    onError: (Throwable) -> Unit
) {
    val captureDir = File(context.cacheDir, "pdf-captures").apply { mkdirs() }
    val safeLabel = label.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
    val photoFile = File(captureDir, "${safeLabel}_$timestamp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runCatching { rewriteImageRespectingExif(photoFile) }
                    .onSuccess { onSuccess(it) }
                    .onFailure { onSuccess(photoFile) }
            }

            override fun onError(exception: ImageCaptureException) {
                photoFile.delete()
                onError(exception)
            }
        }
    )
}

private fun copyGalleryImageToCache(context: Context, uri: Uri, label: String): File {
    val captureDir = File(context.cacheDir, "pdf-captures").apply { mkdirs() }
    val safeLabel = label.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "gallery" }
    val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
    }
    val extension = displayName?.substringAfterLast('.', missingDelimiterValue = "jpg") ?: "jpg"
    val outputFile = File(captureDir, "${safeLabel}_${System.currentTimeMillis()}.$extension")
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Could not open image" }
        outputFile.outputStream().use { output -> input.copyTo(output) }
    }
    return outputFile
}

private fun rotateImageFile(context: Context, sourceFile: File, clockwise: Boolean): File {
    val bitmap = decodeBitmapRespectingExif(sourceFile) ?: error("Photo unavailable")
    val matrix = Matrix().apply { postRotate(if (clockwise) 90f else -90f) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val outputDir = File(context.cacheDir, "pdf-captures").apply { mkdirs() }
    val outputFile = File(outputDir, "rotated_${System.currentTimeMillis()}.jpg")
    outputFile.outputStream().use { output ->
        rotated.compress(Bitmap.CompressFormat.JPEG, 94, output)
    }
    if (rotated !== bitmap) bitmap.recycle()
    rotated.recycle()
    return outputFile
}

private fun rewriteImageRespectingExif(sourceFile: File): File {
    val bitmap = decodeBitmapRespectingExif(sourceFile) ?: return sourceFile
    sourceFile.outputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
    }
    bitmap.recycle()
    return sourceFile
}

private fun decodeBitmapRespectingExif(file: File): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
    val rotation = runCatching {
        when (ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }.getOrDefault(0f)

    if (rotation == 0f) return bitmap
    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        if (it !== bitmap) bitmap.recycle()
    }
}

private data class NormalizedCorner(val x: Float, val y: Float)

private fun defaultCorners(): List<NormalizedCorner> = listOf(
    NormalizedCorner(0.10f, 0.34f),
    NormalizedCorner(0.90f, 0.34f),
    NormalizedCorner(0.90f, 0.66f),
    NormalizedCorner(0.10f, 0.66f)
)

private fun detectDocumentCorners(file: File): List<NormalizedCorner> {
    val bitmap = decodeBitmapRespectingExif(file) ?: return defaultCorners()
    val detected = runCatching {
        require(OpenCVLoader.initLocal()) { "OpenCV could not initialize" }
        detectDocumentCornersWithOpenCv(bitmap)
    }.getOrNull()
    bitmap.recycle()
    return detected ?: defaultCorners()
}

private fun detectDocumentCornersWithOpenCv(bitmap: Bitmap): List<NormalizedCorner>? {
    val rgba = Mat()
    val gray = Mat()
    val blurred = Mat()
    val edges = Mat()
    val closed = Mat()
    val contours = mutableListOf<MatOfPoint>()
    val hierarchy = Mat()

    try {
        Utils.bitmapToMat(bitmap, rgba)
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(blurred, edges, 50.0, 150.0)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(7.0, 7.0))
        Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, kernel)
        kernel.release()
        Imgproc.findContours(closed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val imageArea = bitmap.width.toDouble() * bitmap.height.toDouble()
        val bestQuad = contours
            .asSequence()
            .mapNotNull { contour ->
                val area = Imgproc.contourArea(contour)
                if (area < imageArea * 0.02 || area > imageArea * 0.92) return@mapNotNull null
                val curve = MatOfPoint2f(*contour.toArray())
                val perimeter = Imgproc.arcLength(curve, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(curve, approx, perimeter * 0.025, true)
                val points = approx.toArray()
                curve.release()
                approx.release()
                if (points.size == 4 && Imgproc.isContourConvex(MatOfPoint(*points))) {
                    points to area
                } else {
                    null
                }
            }
            .maxByOrNull { it.second }
            ?.first
            ?: return null

        return orderOpenCvPoints(bestQuad).map {
            NormalizedCorner(
                x = (it.x / bitmap.width.toDouble()).toFloat().coerceIn(0f, 1f),
                y = (it.y / bitmap.height.toDouble()).toFloat().coerceIn(0f, 1f)
            )
        }
    } finally {
        rgba.release()
        gray.release()
        blurred.release()
        edges.release()
        closed.release()
        hierarchy.release()
        contours.forEach { it.release() }
    }
}

private fun orderOpenCvPoints(points: Array<Point>): List<Point> {
    val topLeft = points.minBy { it.x + it.y }
    val bottomRight = points.maxBy { it.x + it.y }
    val topRight = points.maxBy { it.x - it.y }
    val bottomLeft = points.minBy { it.x - it.y }
    return listOf(topLeft, topRight, bottomRight, bottomLeft)
}

private fun fittedImageRect(size: IntSize, imageWidth: Int, imageHeight: Int): RectF {
    if (size.width == 0 || size.height == 0 || imageWidth == 0 || imageHeight == 0) {
        return RectF()
    }
    val scale = min(size.width.toFloat() / imageWidth.toFloat(), size.height.toFloat() / imageHeight.toFloat())
    val width = imageWidth * scale
    val height = imageHeight * scale
    val left = (size.width - width) / 2f
    val top = (size.height - height) / 2f
    return RectF(left, top, left + width, top + height)
}

private fun NormalizedCorner.toScreenOffset(imageRect: RectF): Offset =
    Offset(
        x = imageRect.left + x * imageRect.width(),
        y = imageRect.top + y * imageRect.height()
    )

private fun Offset.toNormalizedCorner(imageRect: RectF): NormalizedCorner =
    NormalizedCorner(
        x = ((x - imageRect.left) / imageRect.width()).coerceIn(0f, 1f),
        y = ((y - imageRect.top) / imageRect.height()).coerceIn(0f, 1f)
    )

private fun createCorrectedCardImage(
    context: Context,
    sourceFile: File,
    corners: List<NormalizedCorner>,
    index: Int
): File {
    val sourceBitmap = decodeBitmapRespectingExif(sourceFile) ?: error("Photo unavailable")
    val outputWidth = 1600
    val outputHeight = (outputWidth / 1.585f).roundToInt()
    val outputBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(outputBitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val src = floatArrayOf(
        corners[0].x * sourceBitmap.width, corners[0].y * sourceBitmap.height,
        corners[1].x * sourceBitmap.width, corners[1].y * sourceBitmap.height,
        corners[2].x * sourceBitmap.width, corners[2].y * sourceBitmap.height,
        corners[3].x * sourceBitmap.width, corners[3].y * sourceBitmap.height
    )
    val dst = floatArrayOf(
        0f, 0f,
        outputWidth.toFloat(), 0f,
        outputWidth.toFloat(), outputHeight.toFloat(),
        0f, outputHeight.toFloat()
    )
    val matrix = Matrix().apply {
        setPolyToPoly(src, 0, dst, 0, 4)
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(sourceBitmap, matrix, paint)

    val outputDir = File(context.cacheDir, "corrected-cards").apply { mkdirs() }
    val outputFile = File(outputDir, "card_${index + 1}_${System.currentTimeMillis()}.jpg")
    outputFile.outputStream().use { output ->
        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
    }
    sourceBitmap.recycle()
    outputBitmap.recycle()
    return outputFile
}

private fun openPdf(context: Context, file: File) {
    val uri = file.contentUri(context)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}

private fun sharePdf(context: Context, file: File) {
    val uri = file.contentUri(context)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share PDF"))
}

private fun File.contentUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.files", this)
