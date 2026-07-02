package roy.ij.postofficesaathi.ui.forms

import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.forms.FormItem

fun formsSearchParams(query: String, resultCount: Int): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Forms,
        AnalyticsParam.SearchText to query,
        AnalyticsParam.ResultCount to resultCount
    )

fun formActionParams(
    form: FormItem,
    query: String,
    actionType: String,
    document: PublicDocumentRef? = null,
    throwable: Throwable? = null
): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Forms,
        AnalyticsParam.FormId to form.id,
        AnalyticsParam.FormCategory to form.category,
        AnalyticsParam.FormLanguage to form.language,
        AnalyticsParam.SearchText to query,
        AnalyticsParam.ActionType to actionType,
        AnalyticsParam.DocumentName to document?.displayName,
        AnalyticsParam.NewlySaved to document?.newlySaved,
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )
