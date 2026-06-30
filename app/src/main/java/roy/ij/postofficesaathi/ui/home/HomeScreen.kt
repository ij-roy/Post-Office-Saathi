package roy.ij.postofficesaathi.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.data.recent.RecentWorkItem
import roy.ij.postofficesaathi.data.recent.RecentWorkType
import roy.ij.postofficesaathi.ui.components.HomeFormsIllustration
import roy.ij.postofficesaathi.ui.components.HomeCalculatorIllustration
import roy.ij.postofficesaathi.ui.components.HomePdfIllustration
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenForms: () -> Unit,
    onCreatePdf: () -> Unit,
    onOpenCalculator: () -> Unit,
    onSuggestPlans: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecent: (RecentWorkItem) -> Unit,
    onShareRecent: (RecentWorkItem) -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = topInset + 66.dp

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
                        .padding(horizontal = PagePadding)
                        .padding(top = 54.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
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
                    HomeActionCard(
                        title = "Interest Calculator",
                        visual = HomeVisual.Calculator,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenCalculator
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding)
                        .padding(bottom = 112.dp)
                ) {
                    RecentWorkPanel(
                        items = state.recentItems,
                        onOpen = onOpenRecent,
                        onShare = onShareRecent
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topInset, start = PagePadding, end = PagePadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Post Office Saathi",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SaathiIconButton(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Open settings",
                        onClick = onOpenSettings
                    )
                }
            }
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

@Composable
private fun RecentWorkPanel(
    items: List<RecentWorkItem>,
    onOpen: (RecentWorkItem) -> Unit,
    onShare: (RecentWorkItem) -> Unit
) {
    SaathiCard {
        Text("Recent Work", style = MaterialTheme.typography.titleLarge)
        if (items.isEmpty()) {
            Text(
                "Saved forms and created PDFs will appear here once you use the tools.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { item ->
                    RecentWorkRow(item = item, onOpen = { onOpen(item) }, onShare = { onShare(item) })
                }
            }
        }
    }
}

@Composable
private fun RecentWorkRow(
    item: RecentWorkItem,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.54f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                when (item.type) {
                    RecentWorkType.CreatedPdf -> "Created PDF"
                    RecentWorkType.Form -> "Downloaded form"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecentWorkButton("Open", onOpen, Modifier.weight(1f), primary = true)
                RecentWorkButton("Share", onShare, Modifier.widthIn(min = 104.dp), primary = false)
            }
        }
    }
}

@Composable
private fun RecentWorkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    primary: Boolean
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.64f),
        contentColor = if (primary) Color.White else MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (primary) 1f else 0.28f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge)
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
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)),
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                when (visual) {
                    HomeVisual.Forms -> HomeFormsIllustration(Modifier.fillMaxSize())
                    HomeVisual.Pdf -> HomePdfIllustration(Modifier.fillMaxSize())
                    HomeVisual.Calculator -> HomeCalculatorIllustration(Modifier.fillMaxSize())
                }
            }
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private enum class HomeVisual {
    Forms,
    Pdf,
    Calculator
}
