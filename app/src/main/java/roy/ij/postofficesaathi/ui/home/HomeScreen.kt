package roy.ij.postofficesaathi.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiScreen

@Composable
fun HomeScreen(
    onOpenForms: () -> Unit,
    onCreatePdf: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = topInset + 76.dp

    SaathiScreen {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = headerHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = screenHeight - headerHeight - 12.dp)
                        .padding(horizontal = PagePadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeActionCard(
                            title = "Download Forms",
                            visual = HomeVisual.Forms,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenForms
                        )

                        HomeActionCard(
                            title = "Create PDF",
                            visual = HomeVisual.Pdf,
                            modifier = Modifier.weight(1f),
                            onClick = onCreatePdf
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding)
                        .padding(bottom = 32.dp)
                ) {
                    SaathiCard {
                        Text("Recent Work", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Saved forms and created PDFs will appear here once you use the tools.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.88f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topInset, start = PagePadding, end = PagePadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Post Office Saathi",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    visual: HomeVisual,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.76f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 244.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedActionVisual(
                visual = visual,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.52f),
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AnimatedActionVisual(
    visual: HomeVisual,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "homeVisual")
    val float by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "homeVisualFloat"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "homeVisualPulse"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.38f)),
            contentAlignment = Alignment.Center
        ) {
            when (visual) {
                HomeVisual.Forms -> FormsVisual(float = float, pulse = pulse)
                HomeVisual.Pdf -> PdfVisual(float = float, pulse = pulse)
            }
        }
    }
}

@Composable
private fun FormsVisual(float: Float, pulse: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        repeat(3) { index ->
            Surface(
                modifier = Modifier
                    .size(width = 60.dp, height = 74.dp)
                    .graphicsLayer {
                        translationX = (index - 1) * 13f
                        translationY = (index - 1) * 7f + float * (index + 1) / 8f
                        alpha = 0.52f + index * 0.14f
                    },
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Column(
                    modifier = Modifier.padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Line(widthFraction = 0.78f)
                    Line(widthFraction = 0.56f)
                    Line(widthFraction = 0.68f)
                }
            }
        }
        Surface(
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    translationX = 36f
                    translationY = -31f
                    scaleX = pulse
                    scaleY = pulse
                },
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f))
        ) {}
    }
}

@Composable
private fun PdfVisual(float: Float, pulse: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(width = 72.dp, height = 86.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.82f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Line(widthFraction = 0.84f)
                Line(widthFraction = 0.64f)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .graphicsLayer { alpha = pulse },
                    shape = RoundedCornerShape(5.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                ) {}
            }
        }
        Surface(
            modifier = Modifier
                .size(width = 58.dp, height = 38.dp)
                .graphicsLayer {
                    translationX = 32f
                    translationY = float
                    rotationZ = float / 8f
                },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f))
        ) {}
    }
}

@Composable
private fun Line(widthFraction: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(4.dp),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    ) {}
}

private enum class HomeVisual {
    Forms,
    Pdf
}
