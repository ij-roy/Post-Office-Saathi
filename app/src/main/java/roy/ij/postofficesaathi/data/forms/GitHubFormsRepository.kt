package roy.ij.postofficesaathi.data.forms

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import roy.ij.postofficesaathi.domain.forms.FormItem
import java.io.File
import java.net.URL

class GitHubFormsRepository(
    context: Context,
    private val indexUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/forms-index.json",
    private val publicBaseUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/"
) : FormsRepository {
    private val formsDir = File(context.filesDir, "forms").apply { mkdirs() }
    private val downloadsDir = File(formsDir, "downloads").apply { mkdirs() }
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

    override suspend fun downloadForm(form: FormItem): File = withContext(Dispatchers.IO) {
        val local = localFileFor(form)
        if (local.exists()) return@withContext local

        local.parentFile?.mkdirs()
        URL(publicBaseUrl + form.file).openStream().use { input ->
            local.outputStream().use { output -> input.copyTo(output) }
        }
        local
    }

    override fun localFileFor(form: FormItem): File {
        val safeName = form.file.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(downloadsDir, safeName)
    }

    private fun markDownloaded(forms: List<FormItem>): List<FormItem> = forms.map { form ->
        form.copy(isDownloaded = localFileFor(form).exists())
    }
}
