package roy.ij.postofficesaathi.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ──────────────────────────────────────────────────────────────────────────────
// ONBOARDING ILLUSTRATIONS
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Onboarding page 1 — "Download Forms"
 * A filing folder with documents peeking out, and a hovering magnifying glass.
 */
@Composable
fun OnboardingFormsIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "obForms")
    val floatY by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obFormsFloat"
    )
    val magGlassBob by transition.animateFloat(
        initialValue = -4f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obFormsMag"
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // === Folder back panel ===
        val folderW = w * 0.62f
        val folderH = h * 0.52f
        val folderX = cx - folderW / 2f
        val folderY = cy - folderH / 2f + floatY * 0.3f + 8f

        // Folder tab
        val tabPath = Path().apply {
            moveTo(folderX + 12f, folderY)
            lineTo(folderX + 12f, folderY - folderH * 0.14f)
            lineTo(folderX + folderW * 0.38f, folderY - folderH * 0.14f)
            lineTo(folderX + folderW * 0.44f, folderY)
            close()
        }
        drawPath(tabPath, primary.copy(alpha = 0.22f))
        drawPath(tabPath, primary.copy(alpha = 0.35f), style = Stroke(width = 1.5f))

        // Folder body
        drawRoundRect(
            color = primaryContainer.copy(alpha = 0.38f),
            topLeft = Offset(folderX, folderY),
            size = Size(folderW, folderH),
            cornerRadius = CornerRadius(14f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.20f),
            topLeft = Offset(folderX, folderY),
            size = Size(folderW, folderH),
            cornerRadius = CornerRadius(14f),
            style = Stroke(width = 1.8f)
        )

        // === Documents peeking out of folder ===
        val docW = folderW * 0.52f
        val docH = folderH * 0.72f
        for (i in 0..2) {
            val offsetX = (i - 1) * folderW * 0.12f
            val offsetY = -docH * 0.15f - i * 6f + floatY * (i + 1) * 0.18f
            val docLeft = cx - docW / 2f + offsetX
            val docTop = folderY + offsetY

            drawRoundRect(
                color = surface.copy(alpha = 0.88f - i * 0.08f),
                topLeft = Offset(docLeft, docTop),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(8f)
            )
            drawRoundRect(
                color = outline.copy(alpha = 0.20f),
                topLeft = Offset(docLeft, docTop),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(8f),
                style = Stroke(width = 1.2f)
            )

            // Lines on document
            val lineStartX = docLeft + docW * 0.16f
            val lineY1 = docTop + docH * 0.26f
            val lineY2 = docTop + docH * 0.44f
            val lineY3 = docTop + docH * 0.62f
            drawLine(onSurface.copy(alpha = 0.12f), Offset(lineStartX, lineY1), Offset(lineStartX + docW * 0.68f, lineY1), strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(onSurface.copy(alpha = 0.10f), Offset(lineStartX, lineY2), Offset(lineStartX + docW * 0.48f, lineY2), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(onSurface.copy(alpha = 0.08f), Offset(lineStartX, lineY3), Offset(lineStartX + docW * 0.58f, lineY3), strokeWidth = 3f, cap = StrokeCap.Round)
        }

        // === Magnifying glass ===
        val magCx = cx + w * 0.22f
        val magCy = cy - h * 0.22f + magGlassBob
        val magR = w * 0.072f

        // Glass circle
        drawCircle(secondary.copy(alpha = 0.12f), radius = magR, center = Offset(magCx, magCy))
        drawCircle(secondary.copy(alpha = 0.55f), radius = magR, center = Offset(magCx, magCy), style = Stroke(width = 3f))

        // Handle
        val handleStart = Offset(magCx + magR * 0.7f, magCy + magR * 0.7f)
        val handleEnd = Offset(magCx + magR * 1.6f, magCy + magR * 1.6f)
        drawLine(secondary.copy(alpha = 0.50f), handleStart, handleEnd, strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

@Composable
fun HomeCalculatorIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "homeCalculator")
    val barGrow by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeCalcBarGrow"
    )
    val percentPulse by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeCalcPercentPulse"
    )
    val arrowBob by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeCalcArrowBob"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // === Bar chart ===
        val barCount = 10
        val barHeights = listOf(0.20f, 0.32f, 0.28f, 0.45f, 0.38f, 0.58f, 0.50f, 0.72f, 0.65f, 0.85f)
        val totalBarAreaW = w * 0.75f
        val barGap = totalBarAreaW * 0.035f
        val barW = (totalBarAreaW - (barCount - 1) * barGap) / barCount
        val chartBaseY = cy + h * 0.25f
        val chartStartX = cx - totalBarAreaW / 2f

        // Bar colors — gradient from secondary to primary
        val barColors = List(barCount) { i ->
            val fraction = i.toFloat() / (barCount - 1)
            val r = secondary.red + (primary.red - secondary.red) * fraction
            val g = secondary.green + (primary.green - secondary.green) * fraction
            val b = secondary.blue + (primary.blue - secondary.blue) * fraction
            Color(r, g, b).copy(alpha = 0.25f + fraction * 0.20f)
        }
        val barBorderColors = List(barCount) { i ->
            val fraction = i.toFloat() / (barCount - 1)
            val r = secondary.red + (primary.red - secondary.red) * fraction
            val g = secondary.green + (primary.green - secondary.green) * fraction
            val b = secondary.blue + (primary.blue - secondary.blue) * fraction
            Color(r, g, b).copy(alpha = 0.45f + fraction * 0.20f)
        }

        // Baseline
        drawLine(
            color = onSurface.copy(alpha = 0.10f),
            start = Offset(chartStartX - barW * 0.6f, chartBaseY),
            end = Offset(chartStartX + totalBarAreaW + barW * 0.6f, chartBaseY),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Draw bars + collect top positions for trend line
        val barTops = mutableListOf<Offset>()
        val maxBarH = h * 0.55f
        for (i in 0 until barCount) {
            val x = chartStartX + i * (barW + barGap)
            val animatedHeight = maxBarH * barHeights[i] * barGrow
            val barTop = chartBaseY - animatedHeight

            drawRoundRect(
                color = barColors[i],
                topLeft = Offset(x, barTop),
                size = Size(barW, animatedHeight),
                cornerRadius = CornerRadius(barW * 0.30f, barW * 0.30f)
            )
            drawRoundRect(
                color = barBorderColors[i],
                topLeft = Offset(x, barTop),
                size = Size(barW, animatedHeight),
                cornerRadius = CornerRadius(barW * 0.30f, barW * 0.30f),
                style = Stroke(width = 1.5f)
            )

            barTops.add(Offset(x + barW / 2f, barTop))
        }

        // === Trend line connecting bar tops ===
        for (i in 0 until barTops.size - 1) {
            drawLine(
                color = primary.copy(alpha = 0.40f),
                start = barTops[i],
                end = barTops[i + 1],
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        // Dots at each bar top
        barTops.forEach { pt ->
            drawCircle(primary.copy(alpha = 0.50f), radius = 3f, center = pt)
        }

        // === Upward arrow at the end of the trend ===
        val lastTop = barTops.last()
        val arrowTipX = lastTop.x + barW * 1.4f
        val arrowTipY = lastTop.y - h * 0.12f + arrowBob
        val arrowBaseX = lastTop.x
        val arrowBaseY = lastTop.y + arrowBob * 0.5f

        drawLine(primary.copy(alpha = 0.48f), Offset(arrowBaseX, arrowBaseY), Offset(arrowTipX, arrowTipY), strokeWidth = 2.5f, cap = StrokeCap.Round)
        val ahs = h * 0.045f
        drawLine(primary.copy(alpha = 0.48f), Offset(arrowTipX, arrowTipY), Offset(arrowTipX - ahs, arrowTipY + ahs * 1.2f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(primary.copy(alpha = 0.48f), Offset(arrowTipX, arrowTipY), Offset(arrowTipX - ahs * 1.3f, arrowTipY + ahs * 0.1f), strokeWidth = 2.5f, cap = StrokeCap.Round)

        // === Percentage badge (top-left of chart) ===
        val badgeCx = cx - w * 0.22f
        val badgeCy = cy - h * 0.22f
        val badgeR = h * 0.14f * percentPulse

        drawCircle(primary.copy(alpha = 0.12f), radius = badgeR, center = Offset(badgeCx, badgeCy))
        drawCircle(primary.copy(alpha = 0.42f), radius = badgeR, center = Offset(badgeCx, badgeCy), style = Stroke(width = 2f))

        // Percent sign inside badge
        val pSize = badgeR * 0.44f
        drawLine(primary.copy(alpha = 0.55f), Offset(badgeCx - pSize, badgeCy + pSize * 0.8f), Offset(badgeCx + pSize, badgeCy - pSize * 0.8f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawCircle(primary.copy(alpha = 0.50f), radius = pSize * 0.30f, center = Offset(badgeCx - pSize * 0.55f, badgeCy - pSize * 0.45f))
        drawCircle(primary.copy(alpha = 0.50f), radius = pSize * 0.30f, center = Offset(badgeCx + pSize * 0.55f, badgeCy + pSize * 0.45f))

        // === Small decorative coin ===
        val coinCx = cx + w * 0.12f
        val coinCy = cy - h * 0.20f + arrowBob * 0.4f
        val coinR = h * 0.08f
        drawCircle(tertiary.copy(alpha = 0.14f), radius = coinR, center = Offset(coinCx, coinCy))
        drawCircle(tertiary.copy(alpha = 0.36f), radius = coinR, center = Offset(coinCx, coinCy), style = Stroke(width = 1.5f))
        drawLine(tertiary.copy(alpha = 0.30f), Offset(coinCx, coinCy - coinR * 0.35f), Offset(coinCx, coinCy + coinR * 0.35f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(tertiary.copy(alpha = 0.24f), Offset(coinCx - coinR * 0.28f, coinCy - coinR * 0.12f), Offset(coinCx + coinR * 0.28f, coinCy - coinR * 0.12f), strokeWidth = 1.2f, cap = StrokeCap.Round)
    }
}

/**
 * Onboarding page 2 — "Create PDFs"
 * A camera viewfinder framing a document, with corner marks and a PDF badge.
 */
@Composable
fun OnboardingPdfIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "obPdf")
    val pulse by transition.animateFloat(
        initialValue = 0.92f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obPdfPulse"
    )
    val cornerShift by transition.animateFloat(
        initialValue = 0f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obPdfCorner"
    )
    val badgeSlide by transition.animateFloat(
        initialValue = 8f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obPdfBadge"
    )

    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // === Document card ===
        val cardW = w * 0.44f
        val cardH = h * 0.55f
        val cardX = cx - cardW / 2f
        val cardY = cy - cardH / 2f - 4f

        drawRoundRect(
            color = surface.copy(alpha = 0.90f),
            topLeft = Offset(cardX, cardY),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(10f)
        )
        drawRoundRect(
            color = outline.copy(alpha = 0.22f),
            topLeft = Offset(cardX, cardY),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(10f),
            style = Stroke(width = 1.4f)
        )

        // Lines on card
        val lineX = cardX + cardW * 0.14f
        for (i in 0..3) {
            val ly = cardY + cardH * (0.18f + i * 0.17f)
            val lw = cardW * (0.72f - i * 0.08f)
            drawLine(onSurface.copy(alpha = 0.12f), Offset(lineX, ly), Offset(lineX + lw, ly), strokeWidth = 3f, cap = StrokeCap.Round)
        }

        // Small image placeholder on card
        val imgX = cardX + cardW * 0.14f
        val imgY = cardY + cardH * 0.62f
        val imgW = cardW * 0.34f
        val imgH = cardH * 0.22f
        drawRoundRect(
            color = primaryContainer.copy(alpha = 0.28f),
            topLeft = Offset(imgX, imgY),
            size = Size(imgW, imgH),
            cornerRadius = CornerRadius(5f)
        )

        // === Viewfinder corners ===
        val vfPad = 14f - cornerShift
        val cornerLen = 18f + cornerShift
        val vfLeft = cardX - vfPad
        val vfTop = cardY - vfPad
        val vfRight = cardX + cardW + vfPad
        val vfBottom = cardY + cardH + vfPad
        val cornerColor = primary.copy(alpha = 0.65f)
        val cStroke = 3f

        // Top-left
        drawLine(cornerColor, Offset(vfLeft, vfTop + cornerLen), Offset(vfLeft, vfTop), strokeWidth = cStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(vfLeft, vfTop), Offset(vfLeft + cornerLen, vfTop), strokeWidth = cStroke, cap = StrokeCap.Round)
        // Top-right
        drawLine(cornerColor, Offset(vfRight - cornerLen, vfTop), Offset(vfRight, vfTop), strokeWidth = cStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(vfRight, vfTop), Offset(vfRight, vfTop + cornerLen), strokeWidth = cStroke, cap = StrokeCap.Round)
        // Bottom-left
        drawLine(cornerColor, Offset(vfLeft, vfBottom - cornerLen), Offset(vfLeft, vfBottom), strokeWidth = cStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(vfLeft, vfBottom), Offset(vfLeft + cornerLen, vfBottom), strokeWidth = cStroke, cap = StrokeCap.Round)
        // Bottom-right
        drawLine(cornerColor, Offset(vfRight - cornerLen, vfBottom), Offset(vfRight, vfBottom), strokeWidth = cStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(vfRight, vfBottom - cornerLen), Offset(vfRight, vfBottom), strokeWidth = cStroke, cap = StrokeCap.Round)

        // === Corner adjustment dots ===
        val dotR = 4f * pulse
        drawCircle(primary.copy(alpha = 0.50f), radius = dotR, center = Offset(vfLeft, vfTop))
        drawCircle(primary.copy(alpha = 0.50f), radius = dotR, center = Offset(vfRight, vfTop))
        drawCircle(primary.copy(alpha = 0.50f), radius = dotR, center = Offset(vfLeft, vfBottom))
        drawCircle(primary.copy(alpha = 0.50f), radius = dotR, center = Offset(vfRight, vfBottom))

        // === PDF Badge ===
        val badgeW = w * 0.18f
        val badgeH = h * 0.10f
        val badgeX = cx + w * 0.14f + badgeSlide
        val badgeY = cy + h * 0.28f

        drawRoundRect(
            color = primary.copy(alpha = 0.18f),
            topLeft = Offset(badgeX, badgeY),
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(6f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.40f),
            topLeft = Offset(badgeX, badgeY),
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.5f)
        )
        // "PDF" text approximation — 3 small horizontal bars
        val pdfLineY = badgeY + badgeH * 0.5f
        drawLine(primary.copy(alpha = 0.45f), Offset(badgeX + badgeW * 0.2f, pdfLineY - 3f), Offset(badgeX + badgeW * 0.8f, pdfLineY - 3f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(primary.copy(alpha = 0.35f), Offset(badgeX + badgeW * 0.25f, pdfLineY + 3f), Offset(badgeX + badgeW * 0.7f, pdfLineY + 3f), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

/**
 * Onboarding page 3 — "Find Recent Work"
 * A clock face with document thumbnails orbiting around it.
 */
@Composable
fun OnboardingRecentIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "obRecent")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "obRecentOrbit"
    )
    val minuteHand by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "obRecentMinute"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.90f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obRecentPulse"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val clockR = w.coerceAtMost(h) * 0.22f

        // === Clock face ===
        drawCircle(secondary.copy(alpha = 0.12f), radius = clockR, center = Offset(cx, cy))
        drawCircle(secondary.copy(alpha = 0.40f), radius = clockR, center = Offset(cx, cy), style = Stroke(width = 2.5f))

        // Tick marks
        for (i in 0 until 12) {
            val angle = Math.toRadians(i * 30.0)
            val innerR = clockR * 0.82f
            val outerR = clockR * 0.94f
            val startX = cx + innerR * sin(angle).toFloat()
            val startY = cy - innerR * cos(angle).toFloat()
            val endX = cx + outerR * sin(angle).toFloat()
            val endY = cy - outerR * cos(angle).toFloat()
            val tickWidth = if (i % 3 == 0) 2.5f else 1.5f
            drawLine(secondary.copy(alpha = 0.35f), Offset(startX, startY), Offset(endX, endY), strokeWidth = tickWidth, cap = StrokeCap.Round)
        }

        // Hour hand (fixed at ~10:10 position for aesthetics)
        val hourAngle = Math.toRadians(300.0)
        val hourLen = clockR * 0.48f
        drawLine(
            onSurface.copy(alpha = 0.50f),
            Offset(cx, cy),
            Offset(cx + hourLen * sin(hourAngle).toFloat(), cy - hourLen * cos(hourAngle).toFloat()),
            strokeWidth = 3.5f, cap = StrokeCap.Round
        )

        // Minute hand (animated)
        val minuteAngle = Math.toRadians(minuteHand.toDouble())
        val minuteLen = clockR * 0.66f
        drawLine(
            secondary.copy(alpha = 0.60f),
            Offset(cx, cy),
            Offset(cx + minuteLen * sin(minuteAngle).toFloat(), cy - minuteLen * cos(minuteAngle).toFloat()),
            strokeWidth = 2.5f, cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(secondary.copy(alpha = 0.55f), radius = 4f, center = Offset(cx, cy))

        // === Orbiting document thumbnails ===
        val orbitR = clockR * 1.8f
        val thumbW = w * 0.13f
        val thumbH = h * 0.16f

        for (i in 0..2) {
            val angle = Math.toRadians(rotation.toDouble() + i * 120.0)
            val tx = cx + orbitR * cos(angle).toFloat() - thumbW / 2f
            val ty = cy + orbitR * sin(angle).toFloat() * 0.6f - thumbH / 2f // Elliptical orbit
            val thumbAlpha = 0.75f + 0.15f * sin(angle).toFloat() // Fade near back

            drawRoundRect(
                color = surface.copy(alpha = thumbAlpha * pulse),
                topLeft = Offset(tx, ty),
                size = Size(thumbW, thumbH),
                cornerRadius = CornerRadius(6f)
            )
            drawRoundRect(
                color = outline.copy(alpha = 0.24f),
                topLeft = Offset(tx, ty),
                size = Size(thumbW, thumbH),
                cornerRadius = CornerRadius(6f),
                style = Stroke(width = 1.2f)
            )

            // Mini lines
            val colors = listOf(primary, secondary, tertiary)
            val lineColor = colors[i % 3]
            drawLine(lineColor.copy(alpha = 0.22f), Offset(tx + 5f, ty + thumbH * 0.28f), Offset(tx + thumbW - 5f, ty + thumbH * 0.28f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(onSurface.copy(alpha = 0.10f), Offset(tx + 5f, ty + thumbH * 0.50f), Offset(tx + thumbW * 0.7f, ty + thumbH * 0.50f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(onSurface.copy(alpha = 0.08f), Offset(tx + 5f, ty + thumbH * 0.70f), Offset(tx + thumbW * 0.55f, ty + thumbH * 0.70f), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// HOME SCREEN ILLUSTRATIONS
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Home "Download Forms" card illustration.
 * An envelope with a form letter partially sliding out + bouncing download arrow + stamp.
 */
@Composable
fun HomeFormsIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "homeForms")
    val letterSlide by transition.animateFloat(
        initialValue = -4f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeFormsSlide"
    )
    val arrowBounce by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeFormsArrow"
    )
    val stampPulse by transition.animateFloat(
        initialValue = 0.88f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homeFormsStamp"
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // === Letter (slides out of envelope) ===
        val letterW = w * 0.50f
        val letterH = h * 0.52f
        val letterX = cx - letterW / 2f
        val letterY = cy - letterH * 0.65f - letterSlide

        drawRoundRect(
            color = surface.copy(alpha = 0.92f),
            topLeft = Offset(letterX, letterY),
            size = Size(letterW, letterH),
            cornerRadius = CornerRadius(7f)
        )
        drawRoundRect(
            color = outline.copy(alpha = 0.20f),
            topLeft = Offset(letterX, letterY),
            size = Size(letterW, letterH),
            cornerRadius = CornerRadius(7f),
            style = Stroke(width = 1.2f)
        )

        // Letter lines
        val llx = letterX + letterW * 0.14f
        drawLine(onSurface.copy(alpha = 0.14f), Offset(llx, letterY + letterH * 0.20f), Offset(llx + letterW * 0.72f, letterY + letterH * 0.20f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(onSurface.copy(alpha = 0.11f), Offset(llx, letterY + letterH * 0.36f), Offset(llx + letterW * 0.52f, letterY + letterH * 0.36f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(onSurface.copy(alpha = 0.09f), Offset(llx, letterY + letterH * 0.50f), Offset(llx + letterW * 0.62f, letterY + letterH * 0.50f), strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Header bar on letter
        drawRoundRect(
            color = primary.copy(alpha = 0.14f),
            topLeft = Offset(letterX + 6f, letterY + 6f),
            size = Size(letterW - 12f, letterH * 0.10f),
            cornerRadius = CornerRadius(4f)
        )

        // === Envelope body (overlaps letter bottom) ===
        val envW = w * 0.60f
        val envH = h * 0.36f
        val envX = cx - envW / 2f
        val envY = cy + h * 0.02f

        drawRoundRect(
            color = primaryContainer.copy(alpha = 0.40f),
            topLeft = Offset(envX, envY),
            size = Size(envW, envH),
            cornerRadius = CornerRadius(10f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.22f),
            topLeft = Offset(envX, envY),
            size = Size(envW, envH),
            cornerRadius = CornerRadius(10f),
            style = Stroke(width = 1.5f)
        )

        // Envelope flap (triangle)
        val flapPath = Path().apply {
            moveTo(envX + 4f, envY + 4f)
            lineTo(cx, envY + envH * 0.42f)
            lineTo(envX + envW - 4f, envY + 4f)
        }
        drawPath(flapPath, primary.copy(alpha = 0.15f))
        drawPath(flapPath, primary.copy(alpha = 0.25f), style = Stroke(width = 1.2f))

        // === Download arrow ===
        val arrowCx = cx
        val arrowCy = cy - h * 0.32f + arrowBounce
        val arrowSize = w * 0.05f

        drawLine(primary.copy(alpha = 0.50f), Offset(arrowCx, arrowCy - arrowSize), Offset(arrowCx, arrowCy + arrowSize), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(primary.copy(alpha = 0.50f), Offset(arrowCx - arrowSize * 0.7f, arrowCy + arrowSize * 0.3f), Offset(arrowCx, arrowCy + arrowSize), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(primary.copy(alpha = 0.50f), Offset(arrowCx + arrowSize * 0.7f, arrowCy + arrowSize * 0.3f), Offset(arrowCx, arrowCy + arrowSize), strokeWidth = 3f, cap = StrokeCap.Round)

        // === Stamp ===
        val stampR = w * 0.065f * stampPulse
        val stampCx = cx + w * 0.22f
        val stampCy = cy - h * 0.12f

        drawCircle(tertiary.copy(alpha = 0.22f), radius = stampR, center = Offset(stampCx, stampCy))
        drawCircle(tertiary.copy(alpha = 0.45f), radius = stampR, center = Offset(stampCx, stampCy), style = Stroke(width = 1.8f))
        // Inner circle
        drawCircle(tertiary.copy(alpha = 0.15f), radius = stampR * 0.55f, center = Offset(stampCx, stampCy))
    }
}

/**
 * Home "Create PDF" card illustration.
 * A phone frame capturing a card image, with shutter ring and PDF badge.
 */
@Composable
fun HomePdfIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "homePdf")
    val shutterRing by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homePdfShutter"
    )
    val floatY by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homePdfFloat"
    )
    val badgePulse by transition.animateFloat(
        initialValue = 0.90f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "homePdfBadge"
    )

    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // === Phone frame ===
        val phoneW = w * 0.50f
        val phoneH = h * 0.72f
        val phoneX = cx - phoneW / 2f
        val phoneY = cy - phoneH / 2f + floatY * 0.4f

        // Phone body
        drawRoundRect(
            color = onSurface.copy(alpha = 0.08f),
            topLeft = Offset(phoneX, phoneY),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(14f)
        )
        drawRoundRect(
            color = onSurface.copy(alpha = 0.22f),
            topLeft = Offset(phoneX, phoneY),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(14f),
            style = Stroke(width = 2.5f)
        )

        // Phone screen area
        val screenPad = 6f
        val screenX = phoneX + screenPad
        val screenY = phoneY + screenPad + 8f
        val screenW = phoneW - screenPad * 2
        val screenH = phoneH - screenPad * 2 - 16f

        drawRoundRect(
            color = surface.copy(alpha = 0.65f),
            topLeft = Offset(screenX, screenY),
            size = Size(screenW, screenH),
            cornerRadius = CornerRadius(8f)
        )

        // === Card being captured (inside phone screen) ===
        val cardW = screenW * 0.78f
        val cardH = screenH * 0.38f
        val cardX = screenX + (screenW - cardW) / 2f
        val cardY = screenY + (screenH - cardH) / 2f - 4f + floatY * 0.2f

        drawRoundRect(
            color = primaryContainer.copy(alpha = 0.35f),
            topLeft = Offset(cardX, cardY),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(6f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.25f),
            topLeft = Offset(cardX, cardY),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.2f)
        )

        // Card content lines
        drawLine(onSurface.copy(alpha = 0.14f), Offset(cardX + 8f, cardY + cardH * 0.3f), Offset(cardX + cardW - 8f, cardY + cardH * 0.3f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(onSurface.copy(alpha = 0.10f), Offset(cardX + 8f, cardY + cardH * 0.55f), Offset(cardX + cardW * 0.6f, cardY + cardH * 0.55f), strokeWidth = 2f, cap = StrokeCap.Round)
        drawLine(onSurface.copy(alpha = 0.08f), Offset(cardX + 8f, cardY + cardH * 0.78f), Offset(cardX + cardW * 0.7f, cardY + cardH * 0.78f), strokeWidth = 2f, cap = StrokeCap.Round)

        // === Shutter ring (animated capture flash) ===
        val ringR = (screenW * 0.12f) + (shutterRing * screenW * 0.06f)
        val ringAlpha = (1f - shutterRing) * 0.30f
        drawCircle(primary.copy(alpha = ringAlpha), radius = ringR, center = Offset(screenX + screenW / 2f, screenY + screenH / 2f), style = Stroke(width = 2.5f))

        // === PDF badge ===
        val badgeSize = w * 0.10f * badgePulse
        val badgeCx = cx + w * 0.20f
        val badgeCy = cy + h * 0.28f + floatY * 0.3f

        drawRoundRect(
            color = primary.copy(alpha = 0.20f),
            topLeft = Offset(badgeCx - badgeSize, badgeCy - badgeSize * 0.6f),
            size = Size(badgeSize * 2, badgeSize * 1.2f),
            cornerRadius = CornerRadius(5f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.42f),
            topLeft = Offset(badgeCx - badgeSize, badgeCy - badgeSize * 0.6f),
            size = Size(badgeSize * 2, badgeSize * 1.2f),
            cornerRadius = CornerRadius(5f),
            style = Stroke(width = 1.5f)
        )
        // PDF text approximation
        drawLine(primary.copy(alpha = 0.40f), Offset(badgeCx - badgeSize * 0.55f, badgeCy), Offset(badgeCx + badgeSize * 0.55f, badgeCy), strokeWidth = 2.5f, cap = StrokeCap.Round)
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// PDF LAYOUT SELECTION ILLUSTRATIONS
// ──────────────────────────────────────────────────────────────────────────────

/**
 * PDF template card illustration showing a fan of ID cards.
 * @param cardCount Number of cards to show (1, 2, or 3)
 */
@Composable
fun PdfTemplateCardIllustration(
    cardCount: Int,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pdfTemplate$cardCount")
    val floatY by transition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1600 + cardCount * 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pdfTemplateFloat$cardCount"
    )
    val fanSpread by transition.animateFloat(
        initialValue = 0.92f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pdfTemplateFan$cardCount"
    )

    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val secondary = MaterialTheme.colorScheme.secondary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val idCardW = w * 0.62f
        val idCardH = h * 0.38f

        // Fan angle per card from center
        val maxAngle = when (cardCount) {
            1 -> 0f
            2 -> 8f
            else -> 12f
        } * fanSpread

        for (i in 0 until cardCount) {
            val angleOffset = when (cardCount) {
                1 -> 0f
                2 -> (i - 0.5f) * maxAngle
                else -> (i - 1f) * maxAngle
            }
            val yOff = when (cardCount) {
                1 -> 0f
                2 -> (i - 0.5f).let { it * it } * 6f
                else -> (i - 1f).let { it * it } * 8f
            }
            val cardAlpha = 0.65f + (i.toFloat() / cardCount) * 0.30f

            rotate(degrees = angleOffset, pivot = Offset(cx, cy + idCardH * 0.7f)) {
                val cardX = cx - idCardW / 2f
                val cardY = cy - idCardH / 2f + yOff + floatY

                // Card body
                drawRoundRect(
                    color = surface.copy(alpha = cardAlpha),
                    topLeft = Offset(cardX, cardY),
                    size = Size(idCardW, idCardH),
                    cornerRadius = CornerRadius(8f)
                )
                drawRoundRect(
                    color = outline.copy(alpha = 0.22f),
                    topLeft = Offset(cardX, cardY),
                    size = Size(idCardW, idCardH),
                    cornerRadius = CornerRadius(8f),
                    style = Stroke(width = 1.2f)
                )

                // Header band
                drawRoundRect(
                    color = primary.copy(alpha = 0.14f),
                    topLeft = Offset(cardX + 4f, cardY + 4f),
                    size = Size(idCardW - 8f, idCardH * 0.18f),
                    cornerRadius = CornerRadius(5f, 5f)
                )

                // Avatar circle
                val avatarR = idCardH * 0.14f
                val avatarCx = cardX + idCardW * 0.18f
                val avatarCy = cardY + idCardH * 0.55f
                drawCircle(primaryContainer.copy(alpha = 0.40f), radius = avatarR, center = Offset(avatarCx, avatarCy))
                drawCircle(primary.copy(alpha = 0.20f), radius = avatarR, center = Offset(avatarCx, avatarCy), style = Stroke(width = 1.2f))

                // Content lines (to the right of avatar)
                val lineStartX = cardX + idCardW * 0.34f
                val lineEndX = cardX + idCardW * 0.86f
                drawLine(onSurface.copy(alpha = 0.14f), Offset(lineStartX, cardY + idCardH * 0.42f), Offset(lineEndX, cardY + idCardH * 0.42f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(onSurface.copy(alpha = 0.10f), Offset(lineStartX, cardY + idCardH * 0.56f), Offset(lineEndX * 0.90f, cardY + idCardH * 0.56f), strokeWidth = 2f, cap = StrokeCap.Round)
                drawLine(onSurface.copy(alpha = 0.08f), Offset(lineStartX, cardY + idCardH * 0.70f), Offset(lineEndX * 0.82f, cardY + idCardH * 0.70f), strokeWidth = 2f, cap = StrokeCap.Round)
            }
        }

        // === Count badge ===
        if (cardCount > 1) {
            val badgeR = w * 0.08f
            val badgeCx = cx + idCardW * 0.32f
            val badgeCy = cy - idCardH * 0.28f + floatY * 0.5f

            drawCircle(secondary.copy(alpha = 0.18f), radius = badgeR, center = Offset(badgeCx, badgeCy))
            drawCircle(secondary.copy(alpha = 0.40f), radius = badgeR, center = Offset(badgeCx, badgeCy), style = Stroke(width = 1.5f))

            // Count representation: small dots
            val dotR = 2.5f
            val dotSpacing = 5f
            val totalDotsWidth = (cardCount - 1) * dotSpacing
            val dotsStartX = badgeCx - totalDotsWidth / 2f
            for (d in 0 until cardCount) {
                drawCircle(secondary.copy(alpha = 0.55f), radius = dotR, center = Offset(dotsStartX + d * dotSpacing, badgeCy))
            }
        }
    }
}
