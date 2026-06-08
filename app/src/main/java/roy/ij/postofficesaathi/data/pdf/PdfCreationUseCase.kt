package roy.ij.postofficesaathi.data.pdf

import android.content.Context
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import java.io.File

class PdfCreationUseCase(private val context: Context) {
    fun createPdf(
        customerName: String,
        layoutType: PdfLayoutType,
        imagePaths: List<String>,
        placements: List<PdfImagePlacement>
    ): File =
        PdfGenerator.createPdf(
            context = context,
            customerName = customerName,
            layoutType = layoutType,
            imageFiles = imagePaths.map(::File),
            placements = placements
        )
}
