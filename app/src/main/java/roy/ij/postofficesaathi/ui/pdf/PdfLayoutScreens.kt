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
import roy.ij.postofficesaathi.ui.components.PdfTemplateCardIllustration
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
    analytics: SaathiAnalytics,
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
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
                                onClick = { layout ->
                                    onLayoutSelected(layout)
                                }
                            )
                            TemplateOptionCard(
                                title = "2 cards",
                                layoutType = PdfLayoutType.TwoDocuments,
                                modifier = Modifier.weight(1f),
                                onClick = { layout ->
                                    onLayoutSelected(layout)
                                }
                            )
                            TemplateOptionCard(
                                title = "3 cards",
                                layoutType = PdfLayoutType.ThreeCards,
                                modifier = Modifier.weight(1f),
                                onClick = { layout ->
                                    onLayoutSelected(layout)
                                }
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
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.62f)
        )
        repeat(4) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
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
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.78f)
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
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
    PdfTemplateCardIllustration(
        cardCount = layoutType.documentLabels.size,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
    )
}
