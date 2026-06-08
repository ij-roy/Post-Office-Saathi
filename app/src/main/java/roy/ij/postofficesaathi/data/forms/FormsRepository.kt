package roy.ij.postofficesaathi.data.forms

import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef

data class FormsLoadResult(
    val forms: List<FormItem>,
    val isFromCache: Boolean,
    val message: String? = null
)

interface FormsRepository {
    suspend fun loadForms(): FormsLoadResult
    suspend fun downloadForm(form: FormItem): PublicDocumentRef
    fun localFileNameFor(form: FormItem): String
}
