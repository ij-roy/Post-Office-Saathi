package roy.ij.postofficesaathi.domain.forms

data class FormItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val keywords: List<String>,
    val file: String,
    val language: String,
    val isDownloaded: Boolean = false
)
