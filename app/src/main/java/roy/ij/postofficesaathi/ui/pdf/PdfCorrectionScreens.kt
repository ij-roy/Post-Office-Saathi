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
internal fun ImmediateCornerCorrectionScreen(
    analytics: SaathiAnalytics,
    layoutType: PdfLayoutType,
    file: File,
    progressText: String,
    corners: List<NormalizedCorner>,
    isProcessing: Boolean,
    processingMessage: String?,
    errorMessage: String?,
    onCornersChanged: (List<NormalizedCorner>) -> Unit,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onApply: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CornerAdjustmentCanvas(
            file = file,
            corners = corners,
            onCornersChanged = onCornersChanged,
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
            visible = errorMessage != null || isProcessing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 126.dp, start = 16.dp, end = 16.dp)
        ) {
            val message = processingMessage ?: errorMessage
            message?.let {
                val isError = errorMessage != null
                Surface(
                    color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.88f) else Color.Black.copy(alpha = 0.52f),
                    contentColor = if (isError) MaterialTheme.colorScheme.onError else Color.White,
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
            CameraPillButton(
                "Retake",
                onClick = {
                    analytics.logButtonTap("corner_retake", AnalyticsScreen.Capture)
                    onRetake()
                },
                modifier = Modifier.weight(1.05f)
            )
            CameraPillButton(
                "Left",
                onClick = {
                    analytics.logButtonTap("image_rotate_left", AnalyticsScreen.Capture)
                    onRotateLeft()
                },
                modifier = Modifier.weight(0.8f)
            )
            CameraPillButton(
                "Right",
                onClick = {
                    analytics.logButtonTap("image_rotate_right", AnalyticsScreen.Capture)
                    onRotateRight()
                },
                modifier = Modifier.weight(0.8f)
            )
            CameraPillButton(
                "Apply",
                onClick = {
                    analytics.logButtonTap("image_adjust_apply", AnalyticsScreen.Capture)
                    onApply()
                },
                modifier = Modifier.weight(1f),
                primary = true
            )
        }
    }
}

@Composable
private fun CornerAdjustmentCanvas(
    file: File,
    corners: List<NormalizedCorner>,
    onCornersChanged: (List<NormalizedCorner>) -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberPreviewBitmap(file, maxDimension = 1800)
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
    val displaySize = 132.dp
    val displaySizePx = with(LocalDensity.current) { displaySize.toPx() }
    val centerX = (sourcePoint.x * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1)
    val centerY = (sourcePoint.y * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1)
    val layout = remember(sourcePoint.x, sourcePoint.y, bitmap, displaySizePx) {
        CornerLoupeLayout.calculate(
            bitmapWidth = bitmap.width,
            bitmapHeight = bitmap.height,
            centerX = centerX,
            centerY = centerY,
            cropSize = cropSize,
            displaySize = displaySizePx
        )
    }
    val loupeBitmap = remember(layout, bitmap) {
        Bitmap.createBitmap(bitmap, layout.left, layout.top, layout.width, layout.height)
    }
    DisposableEffect(loupeBitmap) {
        onDispose { loupeBitmap.recycle() }
    }

    Surface(
        modifier = modifier.size(displaySize),
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
                val center = Offset(
                    x = layout.reticleX.coerceIn(0f, size.width),
                    y = layout.reticleY.coerceIn(0f, size.height)
                )
                drawLine(Color.White, Offset(center.x - 24f, center.y), Offset(center.x + 24f, center.y), 3f)
                drawLine(Color.White, Offset(center.x, center.y - 24f), Offset(center.x, center.y + 24f), 3f)
                drawCircle(Color(0xFFB00010), 8f, center, style = Stroke(width = 3f))
            }
        }
    }
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

