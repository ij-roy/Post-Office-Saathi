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
fun PdfPreviewEditorScreen(
    analytics: SaathiAnalytics,
    layoutType: PdfLayoutType,
    capturedFiles: List<File>,
    placements: List<PdfImagePlacement>,
    onBack: () -> Unit,
    onContinue: (List<PdfImagePlacement>) -> Unit
) {
    var currentPlacements by remember(placements) { mutableStateOf(placements) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var undoStack by remember(placements) { mutableStateOf<List<List<PdfImagePlacement>>>(emptyList()) }
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Layout?") },
            text = { Text("Are you sure you want to discard all changes and restore the original positions?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        analytics.logButtonTap("pdf_preview_reset_confirm", AnalyticsScreen.Preview)
                        analytics.logEvent(AnalyticsEvent.PdfPreviewReset, pdfParams(layoutType))
                        undoStack = undoStack + listOf(currentPlacements)
                        currentPlacements = PdfPlacementFactory.reset(currentPlacements)
                        selectedIndex = null
                        showResetConfirm = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                                analytics.logButtonTap("pdf_preview_undo", AnalyticsScreen.Preview)
                                val previous = undoStack.lastOrNull() ?: return@UndoIconButton
                                undoStack = undoStack.dropLast(1)
                                currentPlacements = previous
                            }
                        )
                        TextButton(
                            onClick = { showResetConfirm = true }
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedIndex = null }
                            )
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
        color = Color.Transparent,
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
            val bitmap = rememberPreviewBitmap(file, maxDimension = 1200)
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
    val interactionSource = remember { MutableInteractionSource() }
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onSelected(index) }
            )
            .pointerInput(index, pageWidthPx, pageHeightPx, placement) {
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
                onRotate = { snappedAngle ->
                    updateDisplay(
                        latestDisplayPlacement.copy(
                            rotationDegrees = snappedAngle
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
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
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
                    paint
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
    onRotate: (Float) -> Unit,
    onRotateFinished: () -> Unit
) {
    val currentPlacement by rememberUpdatedState(placement)
    val currentOnRotate by rememberUpdatedState(onRotate)
    val currentOnRotateFinished by rememberUpdatedState(onRotateFinished)

    ResizeHandle(ResizeCorner.TopLeft, Modifier.align(Alignment.TopStart), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.TopRight, Modifier.align(Alignment.TopEnd), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.BottomLeft, Modifier.align(Alignment.BottomStart), onResize, onResizeFinished)
    ResizeHandle(ResizeCorner.BottomRight, Modifier.align(Alignment.BottomEnd), onResize, onResizeFinished)

    Surface(
        modifier = Modifier
            .offset(x = 36.dp)
            .align(Alignment.CenterEnd)
            .size(26.dp)
            .pointerInput(Unit) {
                var startAngle = 0f
                var totalDelta = 0f
                detectDragGestures(
                    onDragStart = {
                        startAngle = currentPlacement.rotationDegrees
                        totalDelta = 0f
                    },
                    onDragEnd = currentOnRotateFinished,
                    onDragCancel = currentOnRotateFinished,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDelta += dragAmount.x * 0.35f
                        val snapped = snapRotation(startAngle + totalDelta)
                        currentOnRotate(snapped)
                    }
                )
            },
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.96f),
        border = BorderStroke(2.dp, Color(0xFFB00010))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.RotateRight,
                contentDescription = "Rotate",
                tint = Color(0xFFB00010),
                modifier = Modifier.size(16.dp)
            )
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
    val currentOnResize by rememberUpdatedState(onResize)
    val currentOnResizeFinished by rememberUpdatedState(onResizeFinished)

    Surface(
        modifier = modifier
            .offset(
                x = when (corner) {
                    ResizeCorner.TopLeft, ResizeCorner.BottomLeft -> (-14).dp
                    ResizeCorner.TopRight, ResizeCorner.BottomRight -> 14.dp
                },
                y = when (corner) {
                    ResizeCorner.TopLeft, ResizeCorner.TopRight -> (-14).dp
                    ResizeCorner.BottomLeft, ResizeCorner.BottomRight -> 14.dp
                }
            )
            .size(28.dp)
            .pointerInput(corner) {
                detectDragGestures(
                    onDragEnd = currentOnResizeFinished,
                    onDragCancel = currentOnResizeFinished,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnResize(corner, dragAmount)
                    }
                )
            },
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = Color(0xFFB00010)
            ) {}
        }
    }
}

private enum class ResizeCorner { TopLeft, TopRight, BottomLeft, BottomRight }

private fun resizePlacement(
    placement: PdfImagePlacement,
    corner: ResizeCorner,
    dx: Float,
    dy: Float
): PdfImagePlacement {
    // 1. Extract absolute physical ratio from file headers
    var imageAspect = 1.585f
    if (!placement.imagePath.isNullOrEmpty()) {
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(placement.imagePath, opts)
            if (opts.outWidth > 0 && opts.outHeight > 0) {
                imageAspect = opts.outWidth.toFloat() / opts.outHeight.toFloat()
            }
        } catch (e: Exception) { /* Safe default fallback */ }
    }
    // 2. Calibrate image aspect into layout-unit ratio (compensating for A4 height)
    val aspect = imageAspect * (842f / 595f)
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
internal fun rememberPreviewBitmap(file: File?, maxDimension: Int): Bitmap? {
    var bitmap by remember(file?.absolutePath, maxDimension) { mutableStateOf<Bitmap?>(null) }
    DisposableEffect(file?.absolutePath, maxDimension) {
        bitmap = file?.let { PdfImageProcessor.decodeBitmapRespectingExif(it, maxDimension) }
        onDispose {
            bitmap?.recycle()
            bitmap = null
        }
    }
    return bitmap
}

private fun snapRotation(degrees: Float): Float {
    val threshold = 5f
    val multiple = 45f
    val closest = (degrees / multiple).roundToInt() * multiple
    return if (kotlin.math.abs(degrees - closest) <= threshold) closest else degrees
}

