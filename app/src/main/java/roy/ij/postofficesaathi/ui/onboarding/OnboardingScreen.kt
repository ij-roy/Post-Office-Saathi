package roy.ij.postofficesaathi.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.ui.components.OnboardingFormsIllustration
import roy.ij.postofficesaathi.ui.components.OnboardingPdfIllustration
import roy.ij.postofficesaathi.ui.components.OnboardingRecentIllustration
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiPrimaryButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import roy.ij.postofficesaathi.ui.components.SaathiSecondaryButton

private data class OnboardingPage(
    val title: String,
    val body: String,
    val visualIndex: Int
)

private val pages = listOf(
    OnboardingPage("Download Forms", "Search, save, open, and share postal forms from one place.", 0),
    OnboardingPage("Create PDFs", "Capture or import document photos, adjust corners, and save a clean PDF.", 1),
    OnboardingPage("Find Recent Work", "Saved PDFs and downloaded forms stay easy to open and share.", 2)
)

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var horizontalDrag by remember { mutableFloatStateOf(0f) }
    val page = pages[pageIndex]

    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pageIndex) {
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            horizontalDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                horizontalDrag < -80f && pageIndex < pages.lastIndex -> pageIndex += 1
                                horizontalDrag > 80f && pageIndex > 0 -> pageIndex -= 1
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
                    )
                }
                .padding(PagePadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (pageIndex < pages.lastIndex) {
                    SaathiSecondaryButton(text = "Skip", onClick = onSkip)
                } else {
                    Spacer(modifier = Modifier.height(46.dp))
                }
            }

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> -width / 3 } + fadeOut())
                },
                label = "onboardingPage"
            ) { target ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(width = 232.dp, height = 184.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (target.visualIndex) {
                            0 -> OnboardingFormsIllustration(Modifier.fillMaxSize())
                            1 -> OnboardingPdfIllustration(Modifier.fillMaxSize())
                            2 -> OnboardingRecentIllustration(Modifier.fillMaxSize())
                        }
                    }
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = target.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { index, _ ->
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (index == pageIndex) 28.dp else 10.dp, height = 10.dp),
                            shape = CircleShape,
                            color = if (index == pageIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ) {}
                    }
                }
                SaathiPrimaryButton(
                    text = if (pageIndex == pages.lastIndex) "Get Started" else "Next",
                    onClick = {
                        if (pageIndex == pages.lastIndex) onFinish() else pageIndex += 1
                    }
                )
                Surface(modifier = Modifier.height(8.dp), color = MaterialTheme.colorScheme.background) {}
            }
        }
    }
}
