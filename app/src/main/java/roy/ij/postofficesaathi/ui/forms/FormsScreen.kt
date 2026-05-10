package roy.ij.postofficesaathi.ui.forms

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import roy.ij.postofficesaathi.data.forms.FormsLoadResult
import roy.ij.postofficesaathi.data.forms.GitHubFormsRepository
import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.domain.forms.FormSearchEngine
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import java.io.File

@Composable
fun FormsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { GitHubFormsRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var loadResult by remember { mutableStateOf(FormsLoadResult(emptyList(), isFromCache = true)) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var activeMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        loadResult = repository.loadForms()
        activeMessage = loadResult.message
        isLoading = false
    }

    val visibleForms = FormSearchEngine.search(loadResult.forms, query)

    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormsTopBar(onBack = onBack)

            SearchPanel(
                query = query,
                onQueryChange = { query = it }
            )

            AnimatedVisibility(
                visible = activeMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeMessage?.let {
                    SaathiCard {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedContent(
                targetState = when {
                    isLoading -> "loading"
                    visibleForms.isEmpty() -> "empty"
                    else -> "list"
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "formsContent"
            ) { state ->
                when (state) {
                    "loading" -> SkeletonFormsContent()
                    "empty" -> EmptyFormsState(query)
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(visibleForms, key = { it.id }) { form ->
                            FormRow(
                                form = form,
                                onOpen = {
                                    scope.launch {
                                        runCatching { repository.downloadForm(form) }
                                            .onSuccess { openPdf(context, it) }
                                            .onFailure { activeMessage = "Could not download this form. Please try again." }
                                    }
                                },
                                onShare = {
                                    scope.launch {
                                        runCatching { repository.downloadForm(form) }
                                            .onSuccess { sharePdf(context, it) }
                                            .onFailure { activeMessage = "Could not share this form. Please try again." }
                                    }
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
private fun FormsTopBar(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(width = 86.dp, height = 46.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.58f),
            contentColor = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
        ) {
            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("< Back", style = MaterialTheme.typography.labelLarge)
            }
        }
        Text("Forms", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.62f)),
        border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search forms") },
                placeholder = { Text("Aadhaar, PAN, RD, KYC...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    focusedContainerColor = Color.White.copy(alpha = 0.72f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.52f)
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
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.68f)),
        border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormActionButton(
                text = if (form.isDownloaded) "Open" else "Download",
                onClick = onOpen,
                modifier = Modifier.weight(1f),
                primary = true
            )
            FormActionButton(
                text = "Share",
                onClick = onShare,
                modifier = Modifier.widthIn(min = 112.dp),
                primary = false
            )
        }
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
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(width = 120.dp, height = 74.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.58f),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
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
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.34f))
        ) {}
    }
}

@Composable
private fun FormActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (primary) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.58f),
        contentColor = if (primary) Color.White else MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            1.2.dp,
            if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.58f)),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
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

private fun openPdf(context: Context, file: File) {
    val uri = file.contentUri(context)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open form"))
}

private fun sharePdf(context: Context, file: File) {
    val uri = file.contentUri(context)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share form"))
}

private fun File.contentUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.files", this)
