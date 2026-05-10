package roy.ij.postofficesaathi.ui

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
fun PostOfficeSaathiApp() {
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
                HomeScreen(
                    onOpenForms = { navController.navigate(Routes.Forms) },
                    onCreatePdf = { navController.navigate(Routes.PdfLayout) }
                )
            }
            composable(Routes.Forms) {
                FormsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PdfLayout) {
                PdfLayoutSelectionScreen(
                    onBack = { navController.popBackStack() },
                    onLayoutSelected = {
                        selectedLayout = it
                        capturedFiles.clear()
                        pdfPlacements = emptyList()
                        navController.navigate(Routes.Capture)
                    }
                )
            }
            composable(Routes.Capture) {
                DocumentCaptureScreen(
                    layoutType = selectedLayout,
                    onBack = { navController.popBackStack() },
                    onCaptureComplete = { files ->
                        capturedFiles.clear()
                        capturedFiles.addAll(files)
                        pdfPlacements = PdfPlacementFactory.defaultPlacements(files.size, files.map { it.absolutePath })
                        navController.navigate(Routes.Preview)
                    }
                )
            }
            composable(Routes.Corners) {
                CornerAdjustmentScreen(
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    onBack = { navController.popBackStack() },
                    onAdjusted = { files ->
                        capturedFiles.clear()
                        capturedFiles.addAll(files)
                        pdfPlacements = PdfPlacementFactory.defaultPlacements(files.size, files.map { it.absolutePath })
                        navController.navigate(Routes.Preview)
                    }
                )
            }
            composable(Routes.Preview) {
                PdfPreviewEditorScreen(
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    placements = pdfPlacements,
                    onBack = { navController.popBackStack() },
                    onContinue = { placements ->
                        pdfPlacements = placements
                        navController.navigate(Routes.Name)
                    }
                )
            }
            composable(Routes.Name) {
                PdfNameInputScreen(
                    layoutType = selectedLayout,
                    capturedFiles = capturedFiles,
                    placements = pdfPlacements,
                    onBack = { navController.popBackStack() },
                    onPdfCreated = { file ->
                        createdPdfPath = file.absolutePath
                        navController.navigate(Routes.Success)
                    }
                )
            }
            composable(Routes.Success) {
                PdfCreatedSuccessScreen(
                    pdfPath = createdPdfPath,
                    onCreateAnother = {
                        navController.popBackStack(Routes.PdfLayout, inclusive = false)
                    },
                    onHome = {
                        navController.popBackStack(Routes.Home, inclusive = false)
                    }
                )
            }
        }
    }
}
