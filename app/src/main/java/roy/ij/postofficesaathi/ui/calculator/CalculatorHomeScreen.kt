package roy.ij.postofficesaathi.ui.calculator

import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

@Composable
fun CalculatorHomeRoute(
    analytics: SaathiAnalytics,
    onBack: () -> Unit,
    onOpenScheme: (SchemeType) -> Unit,
    onSuggestPlans: () -> Unit
) {
    val context = LocalContext.current
    val factory = remember(context, analytics) { CalculatorHomeViewModel.Factory(context, analytics) }
    val viewModel: CalculatorHomeViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.messageId) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    CalculatorHomeScreen(
        state = state,
        onBack = onBack,
        onOpenScheme = onOpenScheme,
        onSuggestPlans = onSuggestPlans
    )
}

@Composable
fun CalculatorHomeScreen(
    state: CalculatorHomeUiState,
    onBack: () -> Unit,
    onOpenScheme: (SchemeType) -> Unit,
    onSuggestPlans: () -> Unit
) {
    SaathiScreen {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PagePadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalculatorTopBar(onBack)
                if (state.isLoading) {
                    CalculatorSkeletonList()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                    ) {
                        items(state.cards, key = { it.schemeType.name }) { card ->
                            SchemeCard(card = card, onClick = { onOpenScheme(card.schemeType) })
                        }
                    }
                }
            }
            val showSuggestPlansFab = false // Set to true to show the Floating Action Button (FAB)
            if (showSuggestPlansFab) {
                ExtendedFloatingActionButton(
                    onClick = onSuggestPlans,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) },
                    text = { Text("Suggest Plans") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CalculatorTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SaathiIconButton(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            onClick = onBack
        )
        Column(modifier = Modifier.weight(1f)) {
            Text("Interest Calculator", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Post office savings estimates",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SchemeCard(card: CalculatorSchemeCardUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = card.schemeType.schemeIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        card.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (card.isDiscontinued || card.rateLabel.isNotBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (card.isDiscontinued) {
                        Text(
                            "Discontinued",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1
                        )
                    }
                    if (card.isDiscontinued && card.rateLabel.isNotBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (card.rateLabel.isNotBlank()) {
                        Text(
                            card.rateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    card.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CalculatorSkeletonList() {
    val transition = rememberInfiniteTransition(label = "calculatorSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "calculatorSkeletonAlpha"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.10f))
            )
        }
    }
}
