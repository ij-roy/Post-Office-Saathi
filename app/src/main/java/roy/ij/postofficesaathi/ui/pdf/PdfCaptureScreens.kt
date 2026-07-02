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
import androidx.compose.ui.text.style.TextAlign
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
fun DocumentCaptureScreen(
    analytics: SaathiAnalytics,
    layoutType: PdfLayoutType,
    pdfFlowViewModel: PdfFlowViewModel,
    onBack: () -> Unit,
    onCaptureComplete: (List<File>) -> Unit
) {
    val context = LocalContext.current
    val pdfState by pdfFlowViewModel.uiState.collectAsStateWithLifecycle()
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
        analytics.logEvent(
            AnalyticsEvent.CameraPermissionResult,
            pdfParams(layoutType) + mapOf(AnalyticsParam.Granted to granted)
        )
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pdfFlowViewModel.importGalleryImage(it, currentLabel) { file ->
                analytics.logEvent(
                    AnalyticsEvent.GalleryImportSucceeded,
                    pdfParams(layoutType) + mapOf(AnalyticsParam.CaptureSource to "gallery")
                )
                analytics.setContext(AnalyticsParam.CaptureSource, "gallery")
                currentCapture = file
                photoToAdjust = file
                captureError = null
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequested) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    photoToAdjust?.let { file ->
        val workingFile = pdfState.workingImagePath?.let(::File) ?: file
        LaunchedEffect(file.absolutePath) {
            pdfFlowViewModel.loadCorners(file)
        }
        ImmediateCornerCorrectionScreen(
            analytics = analytics,
            layoutType = layoutType,
            file = workingFile,
            progressText = "${currentIndex + 1}/${layoutType.documentLabels.size}",
            corners = pdfState.corners,
            isProcessing = pdfState.isProcessing,
            processingMessage = pdfState.processingMessage,
            errorMessage = pdfState.error,
            onCornersChanged = pdfFlowViewModel::updateCorners,
            onBack = { photoToAdjust = null },
            onRetake = {
                analytics.logButtonTap("capture_retake_after_adjust", AnalyticsScreen.Capture)
                file.delete()
                currentCapture = null
                photoToAdjust = null
            },
            onRotateLeft = { pdfFlowViewModel.rotateWorkingImage(clockwise = false) },
            onRotateRight = { pdfFlowViewModel.rotateWorkingImage(clockwise = true) },
            onApply = {
                pdfFlowViewModel.createCorrectedImage(currentIndex) { corrected ->
                    val nextFiles = capturedFiles + corrected
                    if (currentIndex == layoutType.documentLabels.lastIndex) {
                        analytics.logEvent(
                            AnalyticsEvent.CaptureSucceeded,
                            pdfParams(layoutType) + mapOf(AnalyticsParam.ImageCount to nextFiles.size)
                        )
                        onCaptureComplete(nextFiles)
                    } else {
                        capturedFiles = nextFiles
                        currentCapture = null
                        photoToAdjust = null
                        currentIndex += 1
                    }
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
                        onClick = {
                            analytics.logButtonTap("camera_permission_request", AnalyticsScreen.Capture)
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
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
            CameraCaptureControls(
                isLandscape = isLandscape,
                onCapture = {
                    analytics.logEvent(
                        AnalyticsEvent.CaptureStarted,
                        pdfParams(layoutType) + mapOf(AnalyticsParam.CaptureSource to "camera")
                    )
                    analytics.setContext(AnalyticsParam.CaptureSource, "camera")
                    captureError = null
                    capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        label = currentLabel,
                        onSuccess = {
                            pdfFlowViewModel.prepareCapturedPhoto(it) { prepared ->
                                currentCapture = prepared
                                photoToAdjust = prepared
                                captureError = null
                            }
                        },
                        onError = { error ->
                            captureError = "Could not capture photo. Please try again."
                            val params = pdfParams(layoutType, error) + mapOf(
                                AnalyticsParam.CaptureSource to "camera",
                                AnalyticsParam.ErrorArea to "capture"
                            )
                            analytics.logEvent(AnalyticsEvent.CaptureFailed, params)
                            analytics.recordError("capture", error, params)
                        }
                    )
                },
                onGallery = {
                    analytics.logButtonTap("gallery_pick", AnalyticsScreen.Capture)
                    galleryLauncher.launch("image/*")
                },
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 26.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, bottom = 22.dp)
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
                        analytics.logButtonTap("capture_retake", AnalyticsScreen.Capture)
                        currentCapture?.delete()
                        currentCapture = null
                        captureError = null
                    },
                    modifier = Modifier.weight(1f)
                )
                CameraPillButton(
                    text = if (currentIndex == layoutType.documentLabels.lastIndex) "Review" else "Use photo",
                    onClick = {
                        analytics.logButtonTap("capture_use_photo", AnalyticsScreen.Capture)
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
internal fun CameraIconButton(
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
private fun CameraCaptureControls(
    isLandscape: Boolean,
    onCapture: () -> Unit,
    onGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isLandscape) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CaptureButton(onClick = onCapture)
            GalleryButton(onClick = onGallery, compact = true)
        }
    } else {
        Box(
            modifier = modifier.height(116.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            GalleryButton(
                onClick = onGallery,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 14.dp)
            )
            CaptureButton(
                onClick = onCapture,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CaptureButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier.size(88.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        contentColor = primary,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.56f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = primary.copy(alpha = 0.10f),
                border = BorderStroke(2.dp, primary.copy(alpha = 0.22f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Capture photo",
                        modifier = Modifier.size(31.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(if (compact) 82.dp else 104.dp)
            .height(if (compact) 64.dp else 56.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.36f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = "Upload from gallery",
                modifier = Modifier.size(if (compact) 22.dp else 20.dp)
            )
            if (!compact) {
                Text(
                    "Gallery",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun CameraPillButton(
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
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val preview = remember { Preview.Builder().build() }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )

    DisposableEffect(lifecycleOwner, previewView, imageCapture) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                boundProvider = cameraProvider
                preview.setSurfaceProvider(previewView.surfaceProvider)
                imageCapture.targetRotation = previewView.display.rotation
                runCatching {
                    cameraProvider.unbind(preview, imageCapture)
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
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, useCaseGroup)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
        onDispose {
            boundProvider?.unbind(preview, imageCapture)
        }
    }
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
private fun CapturedImageBackground(
    file: File?,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberPreviewBitmap(file, maxDimension = 1600)

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
                onSuccess(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                photoFile.delete()
                onError(exception)
            }
        }
    )
}


