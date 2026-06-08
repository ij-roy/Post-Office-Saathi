package roy.ij.postofficesaathi.data.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

data class PublicDocumentRef(
    val displayName: String,
    val uriString: String,
    val modifiedAtMillis: Long = System.currentTimeMillis(),
    val newlySaved: Boolean = false
)

object PublicDocumentStorage {
    const val RootFolder = "PostOfficeSaathi"
    const val FormsFolder = "Forms"

    fun savePdf(
        context: Context,
        baseFileName: String,
        subFolder: String? = null,
        writeBytes: (java.io.OutputStream) -> Unit
    ): PublicDocumentRef {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, baseFileName, subFolder, writeBytes)
        } else {
            saveLegacy(baseFileName, subFolder, writeBytes)
        }
    }

    fun findPdf(context: Context, displayName: String, subFolder: String? = null): PublicDocumentRef? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            findWithMediaStore(context, displayName, subFolder)
        } else {
            legacyDir(subFolder).resolve(displayName).takeIf { it.exists() }?.let {
                PublicDocumentRef(
                    displayName = it.name,
                    uriString = it.toURI().toString(),
                    modifiedAtMillis = it.lastModified(),
                    newlySaved = false
                )
            }
        }
    }

    fun listPdfs(context: Context, subFolder: String? = null): List<PublicDocumentRef> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listWithMediaStore(context, subFolder)
        } else {
            legacyDir(subFolder)
                .listFiles { file -> file.isFile && file.extension.equals("pdf", ignoreCase = true) }
                .orEmpty()
                .map {
                    PublicDocumentRef(
                        displayName = it.name,
                        uriString = it.toURI().toString(),
                        modifiedAtMillis = it.lastModified(),
                        newlySaved = false
                    )
                }
        }
    }

    private fun saveWithMediaStore(
        context: Context,
        baseFileName: String,
        subFolder: String?,
        writeBytes: (java.io.OutputStream) -> Unit
    ): PublicDocumentRef {
        val resolver = context.contentResolver
        val relativePath = relativePath(subFolder)
        val displayName = PublicDocumentFileNameFactory.nextAvailableName(
            baseFileName = baseFileName,
            existingNames = listWithMediaStore(context, subFolder).map { it.displayName }.toSet()
        )
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            ?: error("Could not create PDF in Documents")
        runCatching {
            resolver.openOutputStream(uri)?.use(writeBytes) ?: error("Could not open PDF output")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.onFailure {
            resolver.delete(uri, null, null)
            throw it
        }
        return PublicDocumentRef(displayName, uri.toString(), newlySaved = true)
    }

    private fun findWithMediaStore(context: Context, displayName: String, subFolder: String?): PublicDocumentRef? =
        listWithMediaStore(context, subFolder).firstOrNull { it.displayName == displayName }

    private fun listWithMediaStore(context: Context, subFolder: String?): List<PublicDocumentRef> {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.MediaColumns.MIME_TYPE}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val args = arrayOf("application/pdf", relativePath(subFolder))
        return resolver.query(collection, projection, selection, args, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val uri = Uri.withAppendedPath(collection, id.toString())
                    add(
                        PublicDocumentRef(
                            displayName = cursor.getString(nameIndex),
                            uriString = uri.toString(),
                            modifiedAtMillis = cursor.getLong(modifiedIndex) * 1000L,
                            newlySaved = false
                        )
                    )
                }
            }
        }.orEmpty()
    }

    private fun saveLegacy(
        baseFileName: String,
        subFolder: String?,
        writeBytes: (java.io.OutputStream) -> Unit
    ): PublicDocumentRef {
        val dir = legacyDir(subFolder).apply { mkdirs() }
        val displayName = PublicDocumentFileNameFactory.nextAvailableName(
            baseFileName = baseFileName,
            existingNames = dir.listFiles().orEmpty().map { it.name }.toSet()
        )
        val file = dir.resolve(displayName)
        file.outputStream().use(writeBytes)
        return PublicDocumentRef(
            displayName = file.name,
            uriString = file.toURI().toString(),
            modifiedAtMillis = file.lastModified(),
            newlySaved = true
        )
    }

    private fun legacyDir(subFolder: String?): File {
        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), RootFolder)
        return subFolder?.let { File(root, it) } ?: root
    }

    private fun relativePath(subFolder: String?): String =
        listOfNotNull(Environment.DIRECTORY_DOCUMENTS, RootFolder, subFolder)
            .joinToString(separator = "/")
            .trimEnd('/') + "/"
}
