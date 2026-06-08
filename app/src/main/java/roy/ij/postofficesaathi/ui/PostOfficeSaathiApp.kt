package roy.ij.postofficesaathi.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import roy.ij.postofficesaathi.ui.home.HomeExternalAction
import roy.ij.postofficesaathi.ui.home.HomeScreen
import roy.ij.postofficesaathi.ui.home.HomeViewModel
import roy.ij.postofficesaathi.ui.pdf.DocumentCaptureScreen
import roy.ij.postofficesaathi.ui.pdf.PdfFlowViewModel
import roy.ij.postofficesaathi.ui.pdf.PdfCreatedSuccessScreen
import roy.ij.postofficesaathi.ui.pdf.PdfLayoutSelectionScreen
import roy.ij.postofficesaathi.ui.pdf.PdfNameInputScreen
import roy.ij.postofficesaathi.ui.pdf.PdfPreviewEditorScreen
import roy.ij.postofficesaathi.ui.pdf.analyticsName
import java.io.File

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
    val homeFactory = remember(context) { HomeViewModel.Factory(context) }
    val homeViewModel: HomeViewModel = viewModel(key = "home", factory = homeFactory)
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingStorageAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingStorageAction
        pendingStorageAction = null
        if (granted) {
            action?.invoke()
        } else {
            Toast.makeText(context, "Storage permission is needed to save files in Documents.", Toast.LENGTH_SHORT).show()
        }
    }
    fun runWithLegacyStoragePermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingStorageAction = action
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    DisposableEffect(lifecycleOwner, homeViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refreshRecentWork()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(homeViewModel) {
        homeViewModel.externalActions.collect { action ->
            runCatching {
                when (action) {
                    is HomeExternalAction.Open -> openDocument(context, action.item.uri)
                    is HomeExternalAction.Share -> shareDocument(context, action.item.uri)
                }
            }.onFailure {
                homeViewModel.onExternalActionFailed()
            }
        }
    }
    LaunchedEffect(homeState.messageId) {
        homeState.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Home) {
                TrackScreen(analytics, AnalyticsScreen.Home)
                HomeScreen(
                    state = homeState,
                    onOpenForms = {
                        runWithLegacyStoragePermission {
                            analytics.logButtonTap("home_download_forms", AnalyticsScreen.Home)
                            navController.navigate(Routes.Forms)
                        }
                    },
                    onCreatePdf = {
                        runWithLegacyStoragePermission {
                            analytics.logButtonTap("home_create_pdf", AnalyticsScreen.Home)
                            analytics.logEvent(AnalyticsEvent.PdfFlowStarted, mapOf(AnalyticsParam.Flow to AnalyticsFlow.Pdf))
                            analytics.setContext(AnalyticsParam.Flow, AnalyticsFlow.Pdf)
                            navController.navigate(Routes.PdfLayout)
                        }
                    },
                    onOpenRecent = homeViewModel::openRecent,
                    onShareRecent = homeViewModel::shareRecent
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
                    pdfName = pdfState.createdPdfName,
                    onCreateAnother = {
                        analytics.logButtonTap("pdf_success_create_another", AnalyticsScreen.Success)
                        homeViewModel.refreshRecentWork()
                        navController.popBackStack(Routes.PdfLayout, inclusive = false)
                    },
                    onHome = {
                        analytics.logButtonTap("pdf_success_home", AnalyticsScreen.Success)
                        homeViewModel.refreshRecentWork()
                        navController.popBackStack(Routes.Home, inclusive = false)
                    }
                )
            }
        }
    }
}

private fun openDocument(context: Context, uriString: String) {
    val uri = uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}

private fun shareDocument(context: Context, uriString: String) {
    val uri = uriString.toDocumentUri(context)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share PDF"))
}

private fun String.toDocumentUri(context: Context): Uri {
    val uri = Uri.parse(this)
    if (uri.scheme == "content") return uri
    val file = if (uri.scheme == "file") File(uri.path.orEmpty()) else File(this)
    return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
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
