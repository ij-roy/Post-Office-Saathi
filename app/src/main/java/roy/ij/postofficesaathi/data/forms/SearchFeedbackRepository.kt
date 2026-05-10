package roy.ij.postofficesaathi.data.forms

interface SearchFeedbackRepository {
    suspend fun recordFailedSearch(query: String)
}

object NoOpSearchFeedbackRepository : SearchFeedbackRepository {
    override suspend fun recordFailedSearch(query: String) = Unit
}
