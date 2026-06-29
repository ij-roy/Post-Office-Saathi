package roy.ij.postofficesaathi.ui.settings

import androidx.compose.runtime.Composable

@Composable
fun HelpScreen(onBack: () -> Unit) {
    InfoScreen(
        title = "Help & FAQ",
        onBack = onBack,
        sections = listOf(
            "Download Forms" to "Open Download Forms, search by form name or keyword, then download, open, or share a PDF form. Downloading needs internet unless the form is already saved.",
            "Create PDFs" to "Open Create PDF, capture or import card/document photos, adjust corners, review placement, then save the PDF.",
            "Where files are saved" to "Created PDFs are saved in Documents/PostOfficeSaathi. Downloaded forms are saved in Documents/PostOfficeSaathi/Forms.",
            "Offline use" to "PDF creation works locally after photos are captured. Form downloads need internet, and the app shows a clear offline message when a download cannot continue.",
            "Camera permission" to "Camera permission is only used for capturing photos that you choose to include in a PDF."
        )
    )
}
