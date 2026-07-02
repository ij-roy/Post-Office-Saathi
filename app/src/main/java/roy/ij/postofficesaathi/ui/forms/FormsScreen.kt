package roy.ij.postofficesaathi.ui.forms

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import java.io.File

@Composable
fun FormsRoute(
    analytics: SaathiAnalytics,
    onMeaningfulActionCompleted: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val factory = remember(context, analytics) { FormsViewModel.Factory(context, analytics) }
    val viewModel: FormsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.externalActions.collect { action ->
            when (action) {
                is FormsExternalAction.OpenPdf -> {
                    runCatching { openPdf(context, action.document) }
                        .onSuccess {
                            viewModel.onFormOpened(action.form, action.query, action.document)
                            onMeaningfulActionCompleted()
                        }
                        .onFailure { viewModel.onExternalActionFailed("form_open", it, action.form, action.query) }
                }
                is FormsExternalAction.SharePdf -> {
                    runCatching { sharePdf(context, action.document) }
                        .onSuccess {
                            viewModel.onFormShared(action.form, action.query, action.document)
                            onMeaningfulActionCompleted()
                        }
                        .onFailure { viewModel.onExternalActionFailed("form_share", it, action.form, action.query) }
                }
            }
        }
    }
    LaunchedEffect(state.activeMessageId) {
        state.activeMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    FormsScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::updateQuery,
        onOpen = viewModel::openForm,
        onShare = viewModel::shareForm
    )
}

@Composable
fun FormsScreen(
    state: FormsUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onOpen: (FormItem) -> Unit,
    onShare: (FormItem) -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormsTopBar(onBack = onBack)

            SearchPanel(
                query = state.query,
                onQueryChange = onQueryChange
            )

            AnimatedVisibility(
                visible = state.activeMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.activeMessage?.let {
                    SaathiCard {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedContent(
                targetState = when {
                    state.isLoading -> "loading"
                    state.visibleForms.isEmpty() -> "empty"
                    else -> "list"
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "formsContent"
            ) { contentState ->
                when (contentState) {
                    "loading" -> SkeletonFormsContent()
                    "empty" -> EmptyFormsState(state.query)
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.visibleForms, key = { it.id }) { form ->
                            FormRow(
                                form = form,
                                activeAction = if (state.activeFormId == form.id) state.activeFormAction else null,
                                actionsEnabled = state.activeFormId == null,
                                onOpen = { onOpen(form) },
                                onShare = { onShare(form) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormsTopBar(onBack: () -> Unit) {
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
            Text("Download Forms", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Search, save, open, or share",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search forms") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun EmptyFormsState(query: String) {
    SaathiCard {
        EmptySearchAnimation()
        Text(
            "No form found",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (query.isBlank()) {
                "Forms will appear here after the index loads."
            } else {
                "Try another spelling or a shorter keyword."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FormRow(
    form: FormItem,
    activeAction: FormActionKind?,
    actionsEnabled: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                form.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        Text(
            form.description.ifBlank { form.language },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AnimatedVisibility(visible = activeAction != null) {
            activeAction?.let { DownloadProgressPill(it) }
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FormActionButton(
                text = if (form.isDownloaded) "Open" else "Download",
                onClick = onOpen,
                modifier = Modifier.weight(1f),
                primary = true,
                enabled = actionsEnabled
            )
            FormActionButton(
                text = "Share",
                onClick = onShare,
                modifier = Modifier.widthIn(min = 98.dp),
                primary = false,
                enabled = actionsEnabled
            )
        }
        }
    }
}

@Composable
private fun DownloadProgressPill(action: FormActionKind) {
    val transition = rememberInfiniteTransition(label = "formDownloadProgress")
    val pulse by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760),
            repeatMode = RepeatMode.Reverse
        ),
        label = "formDownloadPulse"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f),
        contentColor = MaterialTheme.colorScheme.secondary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = pulse)
            )
            Text(
                text = when (action) {
                    FormActionKind.Open -> "Downloading form..."
                    FormActionKind.Share -> "Preparing share..."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptySearchAnimation() {
    val transition = rememberInfiniteTransition(label = "emptySearch")
    val offset by transition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptySearchOffset"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptySearchPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(width = 120.dp, height = 74.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                SkeletonBlock(widthFraction = 0.56f, height = 8.dp, alpha = 0.8f)
            }
        }
        Surface(
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    translationX = offset
                    scaleX = pulse
                    scaleY = pulse
                },
            shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
            border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.34f))
        ) {}
    }
}

@Composable
private fun FormActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(12.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
            primary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        },
        contentColor = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
            primary -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.primary
        },
        border = BorderStroke(
            1.2.dp,
            if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SkeletonFormsContent() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBlock(widthFraction = 0.34f, height = 34.dp, alpha = alpha)
                        SkeletonBlock(widthFraction = 0.22f, height = 34.dp, alpha = alpha)
                    }
                    SkeletonBlock(widthFraction = 0.82f, height = 28.dp, alpha = alpha)
                    SkeletonBlock(widthFraction = 0.96f, height = 20.dp, alpha = alpha)
                    SkeletonBlock(widthFraction = 0.72f, height = 20.dp, alpha = alpha)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkeletonBlock(
                            widthFraction = 1f,
                            height = 52.dp,
                            alpha = alpha,
                            modifier = Modifier.weight(1f)
                        )
                        SkeletonBlock(
                            widthFraction = 1f,
                            height = 52.dp,
                            alpha = alpha,
                            modifier = Modifier.widthIn(min = 112.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.12f))
    )
}

private fun openPdf(context: Context, document: PublicDocumentRef) {
    val uri = document.uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open form"))
}

private fun sharePdf(context: Context, document: PublicDocumentRef) {
    val uri = document.uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share form"))
}

private fun String.toDocumentUri(context: Context): Uri {
    val uri = Uri.parse(this)
    if (uri.scheme == "content") return uri
    val file = if (uri.scheme == "file") File(uri.path.orEmpty()) else File(this)
    return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
}
