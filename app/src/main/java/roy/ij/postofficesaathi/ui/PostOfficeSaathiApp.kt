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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import roy.ij.postofficesaathi.analytics.AnalyticsEvent
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.analytics.AnalyticsScreen
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.ui.calculator.CalculatorFlowViewModel
import roy.ij.postofficesaathi.ui.calculator.CalculatorHomeRoute
import roy.ij.postofficesaathi.ui.calculator.CalculatorPlaceholderScreen
import roy.ij.postofficesaathi.ui.calculator.scheme.CalculatorResultScreen
import roy.ij.postofficesaathi.ui.calculator.scheme.SchemeCalculatorRoute
import roy.ij.postofficesaathi.ui.calculator.suggest.SuggestBottomSheet
import roy.ij.postofficesaathi.ui.forms.FormsRoute
import roy.ij.postofficesaathi.ui.home.HomeExternalAction
import roy.ij.postofficesaathi.ui.home.HomeScreen
import roy.ij.postofficesaathi.ui.home.HomeViewModel
import roy.ij.postofficesaathi.ui.onboarding.OnboardingScreen
import roy.ij.postofficesaathi.ui.pdf.DocumentCaptureScreen
import roy.ij.postofficesaathi.ui.pdf.PdfFlowViewModel
import roy.ij.postofficesaathi.ui.pdf.PdfCreatedSuccessScreen
import roy.ij.postofficesaathi.ui.pdf.PdfLayoutSelectionScreen
import roy.ij.postofficesaathi.ui.pdf.PdfNameInputScreen
import roy.ij.postofficesaathi.ui.pdf.PdfPreviewEditorScreen
import roy.ij.postofficesaathi.ui.pdf.analyticsName
import roy.ij.postofficesaathi.ui.settings.AppSettingsUiState
import roy.ij.postofficesaathi.ui.settings.AppSettingsViewModel
import roy.ij.postofficesaathi.ui.settings.HelpScreen
import roy.ij.postofficesaathi.ui.settings.PrivacyScreen
import roy.ij.postofficesaathi.ui.settings.SettingsScreen
import java.io.File

private object Routes {
    const val Gate = "gate"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Forms = "forms"
    const val Settings = "settings"
    const val Help = "help"
    const val Privacy = "privacy"
    const val Calculator = "calculator"
    const val SchemeCalculator = "calculator/scheme/{schemeType}?amount={amount}"
    const val CalculatorResult = "calculator/result"
    const val AgentDirectory = "calculator/agents"
    const val PlanResult = "calculator/plan-result"
    const val PdfLayout = "pdf-layout"
    const val Capture = "capture"
    const val Preview = "preview"
    const val Name = "name"
    const val Success = "success"
}

private const val PlayStoreUrl = "https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi"
private const val FeedbackEmail = "ijroy037@gmail.com"

@Composable
fun PostOfficeSaathiApp(
    analytics: SaathiAnalytics,
    settingsState: AppSettingsUiState,
    settingsViewModel: AppSettingsViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val pdfFlowFactory = remember(context, analytics) { PdfFlowViewModel.Factory(context, analytics) }
    val pdfFlowViewModel: PdfFlowViewModel = viewModel(key = "pdf-flow", factory = pdfFlowFactory)
    val pdfState by pdfFlowViewModel.uiState.collectAsStateWithLifecycle()
    val calculatorFlowViewModel: CalculatorFlowViewModel = viewModel(key = "calculator-flow")
    val calculatorResult by calculatorFlowViewModel.latestResult.collectAsStateWithLifecycle()
    val homeFactory = remember(context) { HomeViewModel.Factory(context) }
    val homeViewModel: HomeViewModel = viewModel(key = "home", factory = homeFactory)
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingStorageAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showSuggestSheet by remember { mutableStateOf(false) }
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
            startDestination = Routes.Gate,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Gate) {
                Box(modifier = Modifier.fillMaxSize())
                LaunchedEffect(settingsState.isLoading, settingsState.preferences.hasSeenOnboarding) {
                    if (!settingsState.isLoading) {
                        navController.navigate(
                            if (settingsState.preferences.hasSeenOnboarding) Routes.Home else Routes.Onboarding
                        ) {
                            popUpTo(Routes.Gate) { inclusive = true }
                        }
                    }
                }
            }
            composable(Routes.Onboarding) {
                TrackScreen(analytics, AnalyticsScreen.Onboarding)
                OnboardingScreen(
                    onSkip = {
                        settingsViewModel.completeOnboarding(skipped = true)
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    },
                    onFinish = {
                        settingsViewModel.completeOnboarding(skipped = false)
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    }
                )
            }
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
                    onOpenCalculator = {
                        analytics.logButtonTap("home_interest_calculator", AnalyticsScreen.Home)
                        navController.navigate(Routes.Calculator)
                    },
                    onSuggestPlans = {
                        analytics.logButtonTap("home_suggest_plans", AnalyticsScreen.Home)
                        showSuggestSheet = true
                    },
                    onOpenSettings = {
                        analytics.logButtonTap("home_settings", AnalyticsScreen.Home)
                        navController.navigate(Routes.Settings)
                    },
                    onOpenRecent = homeViewModel::openRecent,
                    onShareRecent = homeViewModel::shareRecent
                )
            }
            composable(Routes.Settings) {
                TrackScreen(analytics, AnalyticsScreen.Settings)
                LaunchedEffect(Unit) { settingsViewModel.logSettingsOpened() }
                SettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onThemeSelected = settingsViewModel::setThemeMode,
                    onRateApp = {
                        settingsViewModel.logRateAppTapped()
                        openPlayStore(context)
                    },
                    onFeedback = {
                        settingsViewModel.logFeedbackEmailTapped()
                        sendFeedbackEmail(context)
                    },
                    onHelp = {
                        settingsViewModel.logHelpOpened()
                        navController.navigate(Routes.Help)
                    },
                    onPrivacy = {
                        settingsViewModel.logPrivacyOpened()
                        navController.navigate(Routes.Privacy)
                    }
                )
            }
            composable(Routes.Help) {
                TrackScreen(analytics, AnalyticsScreen.Help)
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Privacy) {
                TrackScreen(analytics, AnalyticsScreen.Privacy)
                PrivacyScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Calculator) {
                TrackScreen(analytics, AnalyticsScreen.Calculator)
                CalculatorHomeRoute(
                    analytics = analytics,
                    onBack = {
                        analytics.logButtonTap("calculator_back", AnalyticsScreen.Calculator)
                        navController.popBackStack()
                    },
                    onOpenScheme = { schemeType ->
                        navController.navigate(schemeRoute(schemeType, null))
                    },
                    onSuggestPlans = {
                        analytics.logButtonTap("calculator_suggest_plans", AnalyticsScreen.Calculator)
                        showSuggestSheet = true
                    }
                )
            }
            composable(
                route = Routes.SchemeCalculator,
                arguments = listOf(
                    navArgument("schemeType") { type = NavType.StringType },
                    navArgument("amount") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                TrackScreen(analytics, AnalyticsScreen.SchemeCalculator)
                val schemeType = SchemeType.fromRoute(entry.arguments?.getString("schemeType").orEmpty())
                val initialAmount = entry.arguments?.getString("amount")?.toDoubleOrNull()
                SchemeCalculatorRoute(
                    analytics = analytics,
                    schemeType = schemeType,
                    initialAmount = initialAmount,
                    onBack = {
                        analytics.logButtonTap("scheme_calculator_back", AnalyticsScreen.SchemeCalculator)
                        navController.popBackStack()
                    },
                    onResult = { result ->
                        calculatorFlowViewModel.setResult(result)
                        navController.navigate(Routes.CalculatorResult)
                    }
                )
            }
            composable(Routes.CalculatorResult) {
                TrackScreen(analytics, AnalyticsScreen.CalculatorResult)
                CalculatorResultScreen(
                    result = calculatorResult,
                    onBack = {
                        analytics.logButtonTap("calculator_result_back", AnalyticsScreen.CalculatorResult)
                        navController.popBackStack()
                    },
                    onShare = {
                        analytics.logEvent(AnalyticsEvent.ResultShared)
                        calculatorFlowViewModel.shareText()?.let { shareText(context, it) }
                    }
                )
            }
            composable(Routes.AgentDirectory) {
                TrackScreen(analytics, AnalyticsScreen.AgentDirectory)
                CalculatorPlaceholderScreen(
                    title = "Agent Directory",
                    message = "Use Suggest Plans to search agents by pincode.",
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PlanResult) {
                TrackScreen(analytics, AnalyticsScreen.PlanSuggester)
                CalculatorPlaceholderScreen(
                    title = "Plan Results",
                    message = "Use Suggest Plans to compare schemes from the latest saved rates.",
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.Forms) {
                TrackScreen(analytics, AnalyticsScreen.Forms)
                FormsRoute(
                    analytics = analytics,
                    onMeaningfulActionCompleted = settingsViewModel::recordMeaningfulAction,
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
                    onPdfCreated = {
                        settingsViewModel.recordMeaningfulAction()
                        navController.navigate(Routes.Success)
                    }
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
        if (showSuggestSheet) {
            TrackScreen(analytics, AnalyticsScreen.PlanSuggester)
            SuggestBottomSheet(
                analytics = analytics,
                onDismiss = { showSuggestSheet = false },
                onOpenScheme = { schemeType, amount ->
                    navController.navigate(schemeRoute(schemeType, amount))
                }
            )
        }
    }
}

private fun schemeRoute(schemeType: SchemeType, amount: Double?): String =
    "calculator/scheme/${schemeType.name}?amount=${amount?.takeIf { it > 0.0 } ?: ""}"

private fun openPlayStore(context: Context) {
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
    runCatching { context.startActivity(marketIntent) }
        .onFailure {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PlayStoreUrl)))
        }
}

private fun sendFeedbackEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$FeedbackEmail")
        putExtra(Intent.EXTRA_SUBJECT, "Post Office Saathi feedback")
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Send feedback")) }
        .onFailure {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
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

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share calculation"))
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
