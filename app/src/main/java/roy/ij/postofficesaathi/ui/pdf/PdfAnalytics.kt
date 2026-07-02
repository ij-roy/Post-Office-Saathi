package roy.ij.postofficesaathi.ui.pdf

import java.time.LocalDate
import roy.ij.postofficesaathi.analytics.AnalyticsFlow
import roy.ij.postofficesaathi.analytics.AnalyticsParam
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.pdf.PdfFileNameFactory
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType

fun requestedPdfFileName(
    customerName: String,
    layoutType: PdfLayoutType,
    date: LocalDate = LocalDate.now()
): String = PdfFileNameFactory.create(customerName, layoutType, date)

fun pdfCreateStartedParams(
    layoutType: PdfLayoutType,
    customerName: String,
    imageCount: Int,
    requestedPdfFilename: String
): Map<String, Any?> =
    pdfParams(layoutType) + mapOf(
        AnalyticsParam.CustomerName to customerName,
        AnalyticsParam.RequestedPdfFilename to requestedPdfFilename,
        AnalyticsParam.ImageCount to imageCount
    )

fun pdfCreateSucceededParams(
    layoutType: PdfLayoutType,
    customerName: String,
    imageCount: Int,
    requestedPdfFilename: String,
    document: PublicDocumentRef
): Map<String, Any?> =
    pdfCreateStartedParams(layoutType, customerName, imageCount, requestedPdfFilename) + mapOf(
        AnalyticsParam.PdfFilename to document.displayName
    )

fun pdfCreateFailedParams(
    layoutType: PdfLayoutType,
    customerName: String,
    imageCount: Int,
    requestedPdfFilename: String,
    throwable: Throwable
): Map<String, Any?> =
    pdfCreateStartedParams(layoutType, customerName, imageCount, requestedPdfFilename) + mapOf(
        AnalyticsParam.ErrorType to throwable.javaClass.simpleName
    )

fun pdfDocumentParams(
    layoutType: PdfLayoutType,
    pdfName: String? = null,
    throwable: Throwable? = null
): Map<String, Any?> =
    pdfParams(layoutType) + mapOf(
        AnalyticsParam.PdfFilename to pdfName,
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )

fun pdfErrorParams(area: String, layoutType: PdfLayoutType, throwable: Throwable): Map<String, Any?> =
    pdfParams(layoutType) + mapOf(
        AnalyticsParam.ErrorArea to area,
        AnalyticsParam.ErrorType to throwable.javaClass.simpleName
    )

fun pdfBasicParams(throwable: Throwable? = null): Map<String, Any?> =
    mapOf(
        AnalyticsParam.Flow to AnalyticsFlow.Pdf,
        AnalyticsParam.ErrorType to throwable?.javaClass?.simpleName
    )
