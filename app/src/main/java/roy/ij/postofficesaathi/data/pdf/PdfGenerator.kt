package roy.ij.postofficesaathi.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.exifinterface.media.ExifInterface
import roy.ij.postofficesaathi.domain.pdf.PdfFileNameFactory
import roy.ij.postofficesaathi.domain.pdf.PdfImagePlacement
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.domain.pdf.PdfPlacementFactory
import java.io.File
import java.time.LocalDate

object PdfGenerator {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    fun createPdf(
        context: Context,
        customerName: String,
        layoutType: PdfLayoutType,
        imageFiles: List<File>,
        placements: List<PdfImagePlacement> = PdfPlacementFactory.defaultPlacements(
            imageFiles.size,
            imageFiles.map { it.absolutePath }
        )
    ): File {
        val outputDir = File(context.filesDir, "created-pdfs").apply { mkdirs() }
        val outputFile = File(
            outputDir,
            PdfFileNameFactory.create(customerName, layoutType, LocalDate.now())
        )

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw each card at its placement position
        val finalPlacements = if (placements.size == imageFiles.size) placements
        else PdfPlacementFactory.defaultPlacements(imageFiles.size, imageFiles.map { it.absolutePath })

        finalPlacements.forEachIndexed { index, placement ->
            val clamped = placement.clamped()
            val bitmap = imageFiles.getOrNull(index)?.let(::decodePdfBitmap)

            // Convert normalized placement to A4 pixel coordinates
            val destLeft = clamped.x * A4_WIDTH
            val destTop = clamped.y * A4_HEIGHT
            val destWidth = clamped.width * A4_WIDTH
            val destHeight = clamped.height * A4_HEIGHT
            val destRect = RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight)

            if (bitmap != null) {
                // Calculate source crop rectangle from normalized crop bounds
                val srcLeft = (clamped.cropLeft * bitmap.width).toInt()
                val srcTop = (clamped.cropTop * bitmap.height).toInt()
                val srcRight = (clamped.cropRight * bitmap.width).toInt()
                val srcBottom = (clamped.cropBottom * bitmap.height).toInt()
                val srcRect = Rect(srcLeft, srcTop, srcRight, srcBottom)

                canvas.save()
                canvas.rotate(clamped.rotationDegrees, destRect.centerX(), destRect.centerY())
                canvas.drawBitmap(bitmap, srcRect, destRect, imagePaint)
                canvas.restore()
                bitmap.recycle()
            } else {
                // Draw placeholder box for missing images
                val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(224, 224, 224)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRect(destRect, boxPaint)
            }
        }

        document.finishPage(page)
        outputFile.outputStream().use { document.writeTo(it) }
        document.close()
        return outputFile
    }

    private fun decodePdfBitmap(file: File): Bitmap? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeFile(file.absolutePath, options)?.rotateForExif(file)
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val longestSide = maxOf(width, height)
        while (longestSide / sampleSize > 1800) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun Bitmap.rotateForExif(file: File): Bitmap {
        val rotation = runCatching {
            when (ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)

        if (rotation == 0f) return this
        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also {
            if (it !== this) recycle()
        }
    }
}
