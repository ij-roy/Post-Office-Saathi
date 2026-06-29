package roy.ij.postofficesaathi.data.review

data class ReviewPromptResult(
    val completedActionCount: Int,
    val shouldRequestReview: Boolean
)

object ReviewPromptPolicy {
    private const val RequiredCompletedActions = 2

    fun onMeaningfulActionCompleted(
        completedActionCount: Int,
        lastReviewPromptVersion: Long?,
        currentVersionCode: Long
    ): ReviewPromptResult {
        val updatedCount = completedActionCount + 1
        return ReviewPromptResult(
            completedActionCount = updatedCount,
            shouldRequestReview = updatedCount >= RequiredCompletedActions &&
                lastReviewPromptVersion != currentVersionCode
        )
    }
}
