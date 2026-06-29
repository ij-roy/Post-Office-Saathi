package roy.ij.postofficesaathi.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.data.review.ReviewPromptPolicy

class ReviewPromptPolicyTest {
    @Test
    fun doesNotPromptBeforeSecondCompletedAction() {
        val result = ReviewPromptPolicy.onMeaningfulActionCompleted(
            completedActionCount = 0,
            lastReviewPromptVersion = null,
            currentVersionCode = 8L
        )

        assertEquals(1, result.completedActionCount)
        assertFalse(result.shouldRequestReview)
    }

    @Test
    fun promptsAfterSecondCompletedAction() {
        val result = ReviewPromptPolicy.onMeaningfulActionCompleted(
            completedActionCount = 1,
            lastReviewPromptVersion = null,
            currentVersionCode = 8L
        )

        assertEquals(2, result.completedActionCount)
        assertTrue(result.shouldRequestReview)
    }

    @Test
    fun doesNotPromptAgainForSameVersion() {
        val result = ReviewPromptPolicy.onMeaningfulActionCompleted(
            completedActionCount = 3,
            lastReviewPromptVersion = 8L,
            currentVersionCode = 8L
        )

        assertEquals(4, result.completedActionCount)
        assertFalse(result.shouldRequestReview)
    }
}
