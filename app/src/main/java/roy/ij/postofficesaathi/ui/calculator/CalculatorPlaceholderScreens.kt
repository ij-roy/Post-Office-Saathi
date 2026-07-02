package roy.ij.postofficesaathi.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

@Composable
fun CalculatorPlaceholderScreen(
    title: String,
    message: String,
    onBack: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SaathiIconButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                onClick = onBack
            )
            SaathiCard {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

