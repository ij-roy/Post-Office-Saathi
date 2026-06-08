package roy.ij.postofficesaathi.data.recent

import android.content.Context
import roy.ij.postofficesaathi.data.storage.PublicDocumentStorage

enum class RecentWorkType {
    CreatedPdf,
    Form
}

data class RecentWorkItem(
    val title: String,
    val uri: String,
    val type: RecentWorkType,
    val modifiedAtMillis: Long
)

fun sortRecentWorkItems(items: List<RecentWorkItem>): List<RecentWorkItem> =
    items.sortedByDescending { it.modifiedAtMillis }

class RecentWorkRepository(private val context: Context) {
    fun loadRecentWork(limit: Int = 8): List<RecentWorkItem> {
        val createdPdfs = PublicDocumentStorage.listPdfs(context).map {
            RecentWorkItem(
                title = it.displayName,
                uri = it.uriString,
                type = RecentWorkType.CreatedPdf,
                modifiedAtMillis = it.modifiedAtMillis
            )
        }
        val forms = PublicDocumentStorage.listPdfs(context, PublicDocumentStorage.FormsFolder).map {
            RecentWorkItem(
                title = it.displayName,
                uri = it.uriString,
                type = RecentWorkType.Form,
                modifiedAtMillis = it.modifiedAtMillis
            )
        }
        return sortRecentWorkItems(createdPdfs + forms).take(limit)
    }
}
