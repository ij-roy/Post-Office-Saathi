package roy.ij.postofficesaathi.domain.forms

object FormSearchEngine {
    fun search(forms: List<FormItem>, query: String): List<FormItem> {
        val normalizedQuery = query.normalizeForSearch()
        if (normalizedQuery.isBlank()) return forms

        val queryTokens = normalizedQuery
            .split(" ")
            .filter { it.isNotBlank() }
            .flatMap { token -> spellingVariants(token) }
            .toSet()

        return forms.filter { form ->
            val searchable = listOf(
                form.title,
                form.category,
                form.description,
                form.language,
                form.keywords.joinToString(" ")
            ).joinToString(" ").normalizeForSearch()

            queryTokens.all { token -> searchable.contains(token) }
        }
    }

    private fun spellingVariants(token: String): Set<String> = when (token) {
        "adhar", "aadhar", "aadhaar" -> setOf("adhar", "aadhar", "aadhaar", "uidai")
        else -> setOf(token)
    }
}

private fun String.normalizeForSearch(): String = lowercase()
    .replace(Regex("[^a-z0-9]+"), " ")
    .trim()
