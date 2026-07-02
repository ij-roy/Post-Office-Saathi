package roy.ij.postofficesaathi.ui.calculator.suggest

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.agent.Agent
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.formatIndianCurrency
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiPrimaryButton
import roy.ij.postofficesaathi.ui.components.SaathiSecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestBottomSheet(
    analytics: SaathiAnalytics,
    onDismiss: () -> Unit,
    onOpenScheme: (SchemeType, Double) -> Unit
) {
    val context = LocalContext.current
    val factory = remember(context, analytics) { SuggestViewModel.Factory(context, analytics) }
    val viewModel: SuggestViewModel = viewModel(key = "suggest-bottom-sheet", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Suggest me Plans", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = viewModel::updateAmount,
                    modifier = Modifier.weight(1f),
                    label = { Text("Amount") },
                    prefix = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.amountError != null,
                    supportingText = { state.amountError?.let { Text(it) } }
                )
                SaathiPrimaryButton(
                    text = if (state.isSuggesting) "..." else "Suggest",
                    onClick = viewModel::suggestPlans,
                    enabled = !state.isSuggesting,
                    modifier = Modifier.weight(0.72f)
                )
            }
            AnimatedVisibility(visible = state.suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Best matches", style = MaterialTheme.typography.titleMedium)
                    state.suggestions.forEachIndexed { index, suggestion ->
                        SuggestionRow(
                            rank = index + 1,
                            suggestion = suggestion,
                            onOpen = {
                                viewModel.logPlanDetailOpened(suggestion)
                                onDismiss()
                                onOpenScheme(suggestion.schemeType, state.amount.toDoubleOrNull() ?: 0.0)
                            }
                        )
                    }
                }
            }
            SaathiCard {
                Text("To get the best advice", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Connect with an Agent", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = state.pincode,
                        onValueChange = viewModel::updatePincode,
                        modifier = Modifier.weight(1f),
                        label = { Text("Pincode") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.pincodeError != null,
                        supportingText = { state.pincodeError?.let { Text(it) } }
                    )
                    SaathiPrimaryButton(
                        text = if (state.isSearchingAgents) "..." else "Find",
                        onClick = viewModel::searchAgents,
                        enabled = !state.isSearchingAgents,
                        modifier = Modifier.weight(0.58f)
                    )
                }
                state.agentMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.agents.forEach { agent ->
                    AgentRow(
                        agent = agent,
                        onCall = {
                            viewModel.logAgentContact(agent, "call")
                            callAgent(context, agent.phone)
                        },
                        onWhatsApp = {
                            viewModel.logAgentContact(agent, "whatsapp")
                            whatsAppAgent(context, agent.phone)
                        },
                        onShare = {
                            viewModel.logAgentContact(agent, "share")
                            shareAgent(context, agent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(rank: Int, suggestion: PlanSuggestionUi, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$rank.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(suggestion.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("${suggestion.ratePercent}% p.a.", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                "Estimated value ${formatIndianCurrency(suggestion.maturityAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SaathiSecondaryButton("Calculate in detail", onClick = onOpen, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun AgentRow(
    agent: Agent,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (agent.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = agent.photoUrl,
                        contentDescription = agent.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(agent.name.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(agent.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(agent.locationLabel.ifBlank { agent.pincode }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (agent.phone.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactAction("Call", onCall, Modifier.weight(1f))
                    CompactAction("WhatsApp", onWhatsApp, Modifier.weight(1f))
                    CompactAction("Share", onShare, Modifier.weight(1f))
                }
            } else {
                Text("Contact details are not available yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CompactAction(text: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 42.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun callAgent(context: Context, phone: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}

private fun whatsAppAgent(context: Context, phone: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$phone")))
}

private fun shareAgent(context: Context, agent: Agent) {
    val text = buildString {
        appendLine(agent.name)
        appendLine(agent.locationLabel)
        appendLine(agent.phone)
    }
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Share agent contact"
        )
    )
}

