package roy.ij.postofficesaathi.domain.pdf

import java.time.LocalDate

object PdfFileNameFactory {
    fun create(
        customerName: String,
        layoutType: PdfLayoutType,
        date: LocalDate = LocalDate.now()
    ): String {
        val safeCustomer = customerName
            .trim()
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "Customer" }

        return "${safeCustomer}_${layoutType.fileLabel}_$date.pdf"
    }
}
