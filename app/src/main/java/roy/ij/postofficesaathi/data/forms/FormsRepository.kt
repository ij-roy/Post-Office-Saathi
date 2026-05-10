package roy.ij.postofficesaathi.data.forms

import roy.ij.postofficesaathi.domain.forms.FormItem
import java.io.File

data class FormsLoadResult(
    val forms: List<FormItem>,
    val isFromCache: Boolean,
    val message: String? = null
)

interface FormsRepository {
    suspend fun loadForms(): FormsLoadResult
    suspend fun downloadForm(form: FormItem): File
    fun localFileFor(form: FormItem): File
}
