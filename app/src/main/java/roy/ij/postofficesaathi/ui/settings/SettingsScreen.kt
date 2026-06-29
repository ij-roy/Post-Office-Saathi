package roy.ij.postofficesaathi.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.data.preferences.ThemeMode
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

@Composable
fun SettingsScreen(
    state: AppSettingsUiState,
    onBack: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onRateApp: () -> Unit,
    onFeedback: () -> Unit,
    onHelp: () -> Unit,
    onPrivacy: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsTopBar(title = "Settings", onBack = onBack)
            SaathiCard {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                ThemeModeSelector(
                    selected = state.preferences.themeMode,
                    onThemeSelected = onThemeSelected
                )
            }
            SaathiCard {
                Text("App support", style = MaterialTheme.typography.titleLarge)
                SettingsAction("Help & FAQ", Icons.AutoMirrored.Filled.Help, onHelp)
                SettingsAction("Privacy", Icons.Filled.Lock, onPrivacy)
                SettingsAction("Send feedback", Icons.Filled.Email, onFeedback)
                SettingsAction("Rate this app", Icons.Filled.Star, onRateApp)
            }
        }
    }
}

@Composable
fun InfoScreen(
    title: String,
    sections: List<Pair<String, String>>,
    onBack: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsTopBar(title = title, onBack = onBack)
            sections.forEach { (heading, body) ->
                SaathiCard {
                    Text(heading, style = MaterialTheme.typography.titleLarge)
                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            Surface(
                onClick = { onThemeSelected(mode) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (selected == mode) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                },
                contentColor = if (selected == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(
                    1.dp,
                    if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
                ),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = mode.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    onBack: () -> Unit
) {
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
        Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }
