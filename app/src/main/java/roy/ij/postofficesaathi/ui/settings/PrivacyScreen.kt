package roy.ij.postofficesaathi.ui.settings

import androidx.compose.runtime.Composable

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoScreen(
        title = "Privacy",
        onBack = onBack,
        sections = listOf(
            "Local documents" to "Created PDFs and downloaded forms are stored on your device. The app does not upload your document photos or created PDFs to a backend.",
            "Analytics" to "Firebase Analytics is used to understand which app features are used. Events are designed to avoid sensitive personal data.",
            "Crash reporting" to "Firebase Crashlytics helps identify crashes and stability issues so the app can be improved.",
            "Search privacy" to "Form search text is sanitized before analytics. The app avoids sending long numbers, PAN-like values, file paths, names, or document content.",
            "Independent app" to "Post Office Saathi is an independent utility app and is not an official government app."
        )
    )
}
