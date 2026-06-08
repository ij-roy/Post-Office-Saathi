package roy.ij.postofficesaathi.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.ui.forms.FormsRoute
import roy.ij.postofficesaathi.ui.home.HomeScreen
import roy.ij.postofficesaathi.ui.pdf.DocumentCaptureScreen
import roy.ij.postofficesaathi.ui.pdf.PdfFlowViewModel
import roy.ij.postofficesaathi.ui.pdf.PdfCreatedSuccessScreen
import roy.ij.postofficesaathi.ui.pdf.PdfLayoutSelectionScreen
import roy.ij.postofficesaathi.ui.pdf.PdfNameInputScreen
import roy.ij.postofficesaathi.ui.pdf.PdfPreviewEditorScreen
import roy.ij.postofficesaathi.ui.pdf.analyticsName

private object Routes {
    const val Home = "home"
    const val Forms = "forms"
    const val PdfLayout = "pdf-layout"
    const val Capture = "capture"
    const val Preview = "preview"
    const val Name = "name"
    const val Success = "success"
}

@Composable
fun PostOfficeSaathiApp(analytics: SaathiAnalytics) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val pdfFlowFactory = remember(context, analytics) { PdfFlowViewModel.Factory(context, analytics) }
    val pdfFlowViewModel: PdfFlowViewModel = viewModel(key = "pdf-flow", factory = pdfFlowFactory)
    val pdfState by pdfFlowViewModel.uiState.collectAsStateWithLifecycle()

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
                FormsRoute(
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
                        pdfFlowViewModel.selectLayout(it)
                        navController.navigate(Routes.Capture)
                    }
                )
            }
            composable(Routes.Capture) {
                TrackScreen(analytics, AnalyticsScreen.Capture)
                DocumentCaptureScreen(
                    analytics = analytics,
                    layoutType = pdfState.selectedLayout,
                    pdfFlowViewModel = pdfFlowViewModel,
                    onBack = {
                        analytics.logButtonTap("capture_back", AnalyticsScreen.Capture)
                        navController.popBackStack()
                    },
                    onCaptureComplete = { files ->
                        pdfFlowViewModel.setCapturedFiles(files)
                        navController.navigate(Routes.Preview)
                    }
                )
            }
            composable(Routes.Preview) {
                TrackScreen(analytics, AnalyticsScreen.Preview)
                PdfPreviewEditorScreen(
                    analytics = analytics,
                    layoutType = pdfState.selectedLayout,
                    capturedFiles = pdfState.capturedFiles,
                    placements = pdfState.placements,
                    onBack = {
                        analytics.logButtonTap("pdf_preview_back", AnalyticsScreen.Preview)
                        navController.popBackStack()
                    },
                    onContinue = { placements ->
                        analytics.logButtonTap("pdf_preview_continue", AnalyticsScreen.Preview)
                        pdfFlowViewModel.setPlacements(placements)
                        navController.navigate(Routes.Name)
                    }
                )
            }
            composable(Routes.Name) {
                TrackScreen(analytics, AnalyticsScreen.Name)
                PdfNameInputScreen(
                    analytics = analytics,
                    state = pdfState,
                    viewModel = pdfFlowViewModel,
                    onBack = {
                        analytics.logButtonTap("pdf_name_back", AnalyticsScreen.Name)
                        navController.popBackStack()
                    },
                    onPdfCreated = { navController.navigate(Routes.Success) }
                )
            }
            composable(Routes.Success) {
                TrackScreen(analytics, AnalyticsScreen.Success)
                PdfCreatedSuccessScreen(
                    analytics = analytics,
                    pdfPath = pdfState.createdPdfPath,
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
