package roy.ij.postofficesaathi.ui

import android.os.SystemClock
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import roy.ij.postofficesaathi.ui.forms.FormsScreen
import roy.ij.postofficesaathi.ui.home.HomeScreen
import roy.ij.postofficesaathi.ui.pdf.CornerAdjustmentScreen
import roy.ij.postofficesaathi.ui.pdf.DocumentCaptureScreen
import roy.ij.postofficesaathi.ui.pdf.PdfCreatedSuccessScreen
import roy.ij.postofficesaathi.ui.pdf.PdfLayoutSelectionScreen
import roy.ij.postofficesaathi.ui.pdf.PdfNameInputScreen
import roy.ij.postofficesaathi.ui.pdf.PdfPreviewEditorScreen

private object Routes {
    const val Home = "home"
    const val Forms = "forms"
    const val PdfLayout = "pdf-layout"
    const val Capture = "capture"
    const val Corners = "corners"
    const val Preview = "preview"
    const val Name = "name"
    const val Success = "success"
}

@Composable
fun PostOfficeSaathiApp(analytics: SaathiAnalytics) {
    val navController = rememberNavController()
    var selectedLayout by remember { mutableStateOf(PdfLayoutType.OneDocument) }
    val capturedFiles = remember { mutableStateListOf<java.io.File>() }
    var pdfPlacements by remember { mutableStateOf<List<PdfImagePlacement>>(emptyList()) }
    var createdPdfPath by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Home) {
                TrackScreen(analytics, AnalyticsScreen.Home)
                HomeScreen(
                    onOpenForms = {
                        analytics.logButtonTap("home_download_forms", AnalyticsScreen.Home)
                        navController.navigate(Routes.Forms)
                    },
                    onCreatePdf = {
                        analytics.logButtonTap("home_create_pdf", AnalyticsScreen.Home)
                        analytics.logEvent(AnalyticsEvent.PdfFlowStarted, mapOf(AnalyticsParam.Flow to AnalyticsFlow.Pdf))
                        analytics.setContext(AnalyticsParam.Flow, AnalyticsFlow.Pdf)
                        navController.navigate(Routes.PdfLayout)
                    }
                )
            }
            composable(Routes.Forms) {
                TrackScreen(analytics, AnalyticsScreen.Forms)
                FormsScreen(
                    analytics = analytics,
                    onBack = {
                        analytics.logButtonTap("forms_back", AnalyticsScreen.Forms)
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.PdfLayout) {
                TrackScreen(analytics, AnalyticsScreen.PdfLayout)
                PdfLayoutSelectionScreen(
                    analytics = analytics,
                    onBack = {
                        analytics.logButtonTap("pdf_layout_back", AnalyticsScreen.PdfLayout)
                        navController.popBackStack()
                    },
                    onLayoutSelected = {
                        analytics.logEvent(
                            AnalyticsEvent.PdfLayoutSelected,
                            mapOf(
                                AnalyticsParam.Flow to AnalyticsFlow.Pdf,
                                AnalyticsParam.LayoutType to it.analyticsName()
                            )
                        )
                        analytics.setContext(AnalyticsParam.LayoutType, it.analyticsName())
                        selectedLayout = it
                        capturedFiles.clear()
                        pdfPlacements = emptyList()
                        navController.navigate(Routes.Capture)
                    }
                )
            }
            composable(Routes.Capture) {
                TrackScreen(analytics, AnalyticsScreen.Capture)
                DocumentCaptureScreen(
                    analytics = analytics,
                    layoutType = selectedLayout,
                    onBack = {
                        analytics.logButtonTap("capture_back", AnalyticsScreen.Capture)
                        navController.popBackStack()
                    },
                    onCaptureComplete = { files ->
                        capturedFiles.clear()
                        capturedFiles.addAll(files)
                        pdfPlacements = PdfPlacementFactory.defaultPlacements(files.size, files.map { it.absolutePath })
                        navController.navigate(Routes.Preview)
                    }
                )
            }
            composable(Routes.Corners) {
                TrackScreen(analytics, AnalyticsScreen.Corners)
                CornerAdjustmentScreen(
                    analytics = analytics,
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    onBack = {
                        analytics.logButtonTap("corner_back", AnalyticsScreen.Corners)
                        navController.popBackStack()
                    },
                    onAdjusted = { files ->
                        capturedFiles.clear()
                        capturedFiles.addAll(files)
                        pdfPlacements = PdfPlacementFactory.defaultPlacements(files.size, files.map { it.absolutePath })
                        navController.navigate(Routes.Preview)
                    }
                )
            }
            composable(Routes.Preview) {
                TrackScreen(analytics, AnalyticsScreen.Preview)
                PdfPreviewEditorScreen(
                    analytics = analytics,
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    placements = pdfPlacements,
                    onBack = {
                        analytics.logButtonTap("pdf_preview_back", AnalyticsScreen.Preview)
                        navController.popBackStack()
                    },
                    onContinue = { placements ->
                        analytics.logButtonTap("pdf_preview_continue", AnalyticsScreen.Preview)
                        pdfPlacements = placements
                        navController.navigate(Routes.Name)
                    }
                )
            }
            composable(Routes.Name) {
                TrackScreen(analytics, AnalyticsScreen.Name)
                PdfNameInputScreen(
                    analytics = analytics,
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    placements = pdfPlacements,
                    onBack = {
                        analytics.logButtonTap("pdf_name_back", AnalyticsScreen.Name)
                        navController.popBackStack()
                    },
                    onPdfCreated = { file ->
                        createdPdfPath = file.absolutePath
                        navController.navigate(Routes.Success)
                    }
                )
            }
            composable(Routes.Success) {
                TrackScreen(analytics, AnalyticsScreen.Success)
                PdfCreatedSuccessScreen(
                    analytics = analytics,
                    pdfPath = createdPdfPath,
                    onCreateAnother = {
                        analytics.logButtonTap("pdf_success_create_another", AnalyticsScreen.Success)
                        navController.popBackStack(Routes.PdfLayout, inclusive = false)
                    },
                    onHome = {
                        analytics.logButtonTap("pdf_success_home", AnalyticsScreen.Success)
                        navController.popBackStack(Routes.Home, inclusive = false)
                    }
                )
            }
        }
    }
}

@Composable
private fun TrackScreen(analytics: SaathiAnalytics, screen: String) {
    DisposableEffect(screen) {
        val startedAt = SystemClock.elapsedRealtime()
        analytics.logScreenViewed(screen)
        onDispose {
            analytics.logScreenTime(screen, SystemClock.elapsedRealtime() - startedAt)
        }
    }
}

private fun PdfLayoutType.analyticsName(): String =
    when (this) {
        PdfLayoutType.OneDocument -> "one_document"
        PdfLayoutType.TwoDocuments -> "two_documents"
        PdfLayoutType.ThreeCards -> "three_cards"
    }
