package roy.ij.postofficesaathi.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import roy.ij.postofficesaathi.ui.pdf.state.NormalizedCorner
import java.io.File
import kotlin.math.roundToInt

class PdfImageProcessor(private val context: Context) {
    fun copyGalleryImageToCache(uri: Uri, label: String): File {
        val captureDir = File(context.cacheDir, "pdf-captures").apply { mkdirs() }
        val safeLabel = label.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "gallery" }
        val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
        val extension = displayName?.substringAfterLast('.', missingDelimiterValue = "jpg") ?: "jpg"
        val outputFile = File(captureDir, "${safeLabel}_${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open image" }
            outputFile.outputStream().use { output -> input.copyTo(output) }
        }
        return outputFile
    }

    fun rewriteImageRespectingExif(sourceFile: File): File {
        val bitmap = decodeBitmapRespectingExif(sourceFile) ?: return sourceFile
        sourceFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
        }
        bitmap.recycle()
        return sourceFile
    }

    fun rotateImageFile(sourceFile: File, clockwise: Boolean): File {
        val bitmap = decodeBitmapRespectingExif(sourceFile) ?: error("Photo unavailable")
        val matrix = Matrix().apply { postRotate(if (clockwise) 90f else -90f) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val outputDir = File(context.cacheDir, "pdf-captures").apply { mkdirs() }
        val outputFile = File(outputDir, "rotated_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { output ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 94, output)
        }
        if (rotated !== bitmap) bitmap.recycle()
        rotated.recycle()
        return outputFile
    }

    fun detectDocumentCorners(file: File): List<NormalizedCorner> {
        val bitmap = decodeBitmapRespectingExif(file, maxDimension = 1800) ?: return defaultCorners()
        val detected = runCatching {
            require(OpenCVLoader.initLocal()) { "OpenCV could not initialize" }
            detectDocumentCornersWithOpenCv(bitmap)
        }.getOrNull()
        bitmap.recycle()
        return detected ?: defaultCorners()
    }

    fun createCorrectedCardImage(
        sourceFile: File,
        corners: List<NormalizedCorner>,
        index: Int
    ): File {
        val sourceBitmap = decodeBitmapRespectingExif(sourceFile) ?: error("Photo unavailable")
        val w = sourceBitmap.width.toFloat()
        val h = sourceBitmap.height.toFloat()
        val widthTop = kotlin.math.hypot((corners[1].x - corners[0].x) * w, (corners[1].y - corners[0].y) * h)
        val widthBottom = kotlin.math.hypot((corners[2].x - corners[3].x) * w, (corners[2].y - corners[3].y) * h)
        val realW = maxOf(widthTop, widthBottom)
        val heightLeft = kotlin.math.hypot((corners[3].x - corners[0].x) * w, (corners[3].y - corners[0].y) * h)
        val heightRight = kotlin.math.hypot((corners[2].x - corners[1].x) * w, (corners[2].y - corners[1].y) * h)
        val realH = maxOf(heightLeft, heightRight)
        val scale = 1600f / maxOf(1f, maxOf(realW, realH))
        val outputWidth = (realW * scale).roundToInt().coerceAtLeast(100)
        val outputHeight = (realH * scale).roundToInt().coerceAtLeast(100)
        val outputBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val src = floatArrayOf(
            corners[0].x * sourceBitmap.width, corners[0].y * sourceBitmap.height,
            corners[1].x * sourceBitmap.width, corners[1].y * sourceBitmap.height,
            corners[2].x * sourceBitmap.width, corners[2].y * sourceBitmap.height,
            corners[3].x * sourceBitmap.width, corners[3].y * sourceBitmap.height
        )
        val dst = floatArrayOf(
            0f, 0f,
            outputWidth.toFloat(), 0f,
            outputWidth.toFloat(), outputHeight.toFloat(),
            0f, outputHeight.toFloat()
        )
        val matrix = Matrix().apply { setPolyToPoly(src, 0, dst, 0, 4) }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(sourceBitmap, matrix, paint)
        val outputDir = File(context.cacheDir, "corrected-cards").apply { mkdirs() }
        val outputFile = File(outputDir, "card_${index + 1}_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { output ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
        }
        sourceBitmap.recycle()
        outputBitmap.recycle()
        return outputFile
    }

    private fun detectDocumentCornersWithOpenCv(bitmap: Bitmap): List<NormalizedCorner>? {
        val rgba = Mat()
        val gray = Mat()
        val blurred = Mat()
        val edges = Mat()
        val closed = Mat()
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()

        try {
            Utils.bitmapToMat(bitmap, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(blurred, edges, 50.0, 150.0)
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(7.0, 7.0))
            Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, kernel)
            kernel.release()
            Imgproc.findContours(closed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            val imageArea = bitmap.width.toDouble() * bitmap.height.toDouble()
            val bestQuad = contours
                .asSequence()
                .mapNotNull { contour ->
                    val area = Imgproc.contourArea(contour)
                    if (area < imageArea * 0.02 || area > imageArea * 0.92) return@mapNotNull null
                    val curve = MatOfPoint2f(*contour.toArray())
                    val perimeter = Imgproc.arcLength(curve, true)
                    val approx = MatOfPoint2f()
                    Imgproc.approxPolyDP(curve, approx, perimeter * 0.025, true)
                    val points = approx.toArray()
                    curve.release()
                    approx.release()
                    if (points.size == 4 && Imgproc.isContourConvex(MatOfPoint(*points))) points to area else null
                }
                .maxByOrNull { it.second }
                ?.first
                ?: return null

            return orderOpenCvPoints(bestQuad).map {
                NormalizedCorner(
                    x = (it.x / bitmap.width.toDouble()).toFloat().coerceIn(0f, 1f),
                    y = (it.y / bitmap.height.toDouble()).toFloat().coerceIn(0f, 1f)
                )
            }
        } finally {
            rgba.release()
            gray.release()
            blurred.release()
            edges.release()
            closed.release()
            hierarchy.release()
            contours.forEach { it.release() }
        }
    }

    private fun orderOpenCvPoints(points: Array<Point>): List<Point> {
        val topLeft = points.minBy { it.x + it.y }
        val bottomRight = points.maxBy { it.x + it.y }
        val topRight = points.maxBy { it.x - it.y }
        val bottomLeft = points.minBy { it.x - it.y }
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    companion object {
        fun decodeBitmapRespectingExif(file: File, maxDimension: Int? = null): Bitmap? {
            val options = if (maxDimension == null) {
                null
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                BitmapFactory.Options().apply {
                    inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                }
            }
            val bitmap = if (options == null) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                BitmapFactory.decodeFile(file.absolutePath, options)
            } ?: return null
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

            if (rotation == 0f) return bitmap
            val matrix = Matrix().apply { postRotate(rotation) }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
                if (it !== bitmap) bitmap.recycle()
            }
        }

        fun defaultCorners(): List<NormalizedCorner> = listOf(
            NormalizedCorner(0.10f, 0.34f),
            NormalizedCorner(0.90f, 0.34f),
            NormalizedCorner(0.90f, 0.66f),
            NormalizedCorner(0.10f, 0.66f)
        )

        private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
            var sampleSize = 1
            val longestSide = maxOf(width, height)
            while (longestSide / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            return sampleSize
        }
    }
}
