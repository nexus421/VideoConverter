package bayern.kickner.videoconverter

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import bayern.kickner.kotlin_extensions_android.uri.FileHelper
import bayern.kickner.kotlin_extensions_android.uri.UriResult
import bayern.kickner.kotlin_extensions_android.uri.readContentFromUri
import java.io.File


object FileManager {

    val workingDir = File("${Environment.getExternalStorageDirectory()}/VideoConverter")

    init {
        workingDir.mkdir()
    }

    fun deleteAllFilesInAppDir(context: Context) = FileHelper.getAppExternalDirectory(context)?.listFiles()?.forEach(File::delete)

    fun copyFileToInternal(uri: Uri, context: Context): File? {
        return when (val result = uri.readContentFromUri(context)) {
            is UriResult.Failure -> null
            is UriResult.Success -> {
                val filename = context.getFileName(uri) ?: "ErrorFilename"
                val root = FileHelper.getAppExternalDirectory(context)
                File(root, filename).apply {
                    writeBytes(result.value)
                }
            }
        }
    }

    fun Context.getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor: Cursor = contentResolver.query(uri, null, null, null, null) ?: return null
            cursor.use { c ->
                if (c.moveToFirst()) {
                    return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        uri.path?.let {
            val cut = it.lastIndexOf('/')

            if (cut != -1) {
                return it.substring(cut + 1)
            }
        }

        return null
    }

}