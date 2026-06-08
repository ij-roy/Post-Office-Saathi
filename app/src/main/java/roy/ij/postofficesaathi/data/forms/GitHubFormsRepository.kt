package roy.ij.postofficesaathi.data.forms

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import roy.ij.postofficesaathi.data.storage.PublicDocumentStorage
import roy.ij.postofficesaathi.domain.forms.FormItem
import java.io.File
import java.net.URL

class GitHubFormsRepository(
    private val context: Context,
    private val indexUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/forms-index.json",
    private val publicBaseUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/"
) : FormsRepository {
    private val formsDir = java.io.File(context.filesDir, "forms").apply { mkdirs() }
    private val indexCache = File(formsDir, "forms-index.json")

    override suspend fun loadForms(): FormsLoadResult = withContext(Dispatchers.IO) {
        runCatching {
            val freshJson = URL(indexUrl).readText()
            indexCache.writeText(freshJson)
            FormsLoadResult(markDownloaded(FormsIndexParser.parse(freshJson)), isFromCache = false)
        }.getOrElse { networkError ->
            if (indexCache.exists()) {
                FormsLoadResult(
                    forms = markDownloaded(FormsIndexParser.parse(indexCache.readText())),
                    isFromCache = true,
                    message = "No internet. Showing saved forms."
                )
            } else {
                FormsLoadResult(
                    forms = emptyList(),
                    isFromCache = true,
                    message = networkError.message ?: "Could not load forms."
                )
            }
        }
    }

    override suspend fun downloadForm(form: FormItem) = withContext(Dispatchers.IO) {
        val displayName = localFileNameFor(form)
        PublicDocumentStorage.findPdf(context, displayName, PublicDocumentStorage.FormsFolder)?.let {
            return@withContext it
        }

        if (!context.hasInternetConnection()) {
            throw OfflineFormsException()
        }

        PublicDocumentStorage.savePdf(
            context = context,
            baseFileName = displayName,
            subFolder = PublicDocumentStorage.FormsFolder
        ) { output ->
            URL(publicBaseUrl + form.file).openStream().use { input -> input.copyTo(output) }
        }
    }

    override fun localFileNameFor(form: FormItem): String =
        form.file.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")

    private fun markDownloaded(forms: List<FormItem>): List<FormItem> = forms.map { form ->
        form.copy(isDownloaded = PublicDocumentStorage.findPdf(context, localFileNameFor(form), PublicDocumentStorage.FormsFolder) != null)
    }
}

class OfflineFormsException : IllegalStateException("No internet connection.")

private fun Context.hasInternetConnection(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
