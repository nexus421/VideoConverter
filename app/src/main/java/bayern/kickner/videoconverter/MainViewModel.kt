package bayern.kickner.videoconverter

import android.content.Context
import android.content.Intent
import android.icu.text.IDNA.Info
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import bayern.kickner.kotlin_extensions_android.uri.FileHelper
import bayern.kickner.kotlin_extensions_android.uri.UriResult
import bayern.kickner.kotlin_extensions_android.uri.readContentFromUri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import kotlin.concurrent.thread

class MainViewModel() : ViewModel() {

    init {
        FileManager
    }

    val filesToConvert = mutableStateListOf<File>()
    val convertedFiles = mutableStateListOf<FileResult>()
    var ffmpegCommand by mutableStateOf("-i €1 -c:v mpeg4 €2.mp4")
    var infoDialog by mutableStateOf<InfoDialog?>(null)
    var loadingDialog by mutableStateOf("")

    fun chooseVideos(activity: MainActivity) {
        if (Environment.isExternalStorageManager()) {
            FileManager.workingDir.mkdir()
            activity.fileChooser.launch("*/*")
        } else {
            infoDialog =
                InfoDialog("You will now be asked to give access to all your files. This is necessary, so you can choose any file from anywhere and the app can store the converted files at a easy to find place for you. Otherwise this app won't work!\nAfter you granted the permission, open the file chooser again.") {
                    val uri = Uri.parse("package:${activity.packageName}")
                    infoDialog = null
                    activity.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            uri
                        )
                    )
                }
        }
    }

    fun handleChosenVideos(uri: Uri, context: Context) {
        loadingDialog = "Copy and prepare file..."
        val file = FileManager.copyFileToInternal(uri, context)
        if (file == null) {
            infoDialog = InfoDialog("Error loading file") {
                infoDialog = null
            }
            return
        }
        filesToConvert.add(file)
        loadingDialog = ""
    }

    fun clearFilesToConvertAndDeleteThem(context: Context) {
        filesToConvert.clear()
        convertedFiles.clear()
        FileManager.deleteAllFilesInAppDir(context)
    }

    fun convertFiles(context: Context) {
        if(filesToConvert.isEmpty()) return Toast.makeText(context, "No files selected.", Toast.LENGTH_SHORT).show()
        loadingDialog = "Convert files..."
        thread {
            val iterator = filesToConvert.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                val result = File(FileManager.workingDir, element.nameWithoutExtension)
                val session = FFmpegKit.execute(ffmpegCommand.replace("€1", element.absolutePath).replace("€2", result.absolutePath))

                if (ReturnCode.isSuccess(session.returnCode)) convertedFiles.add(FileResult(element, State.Succeed))
                else if (ReturnCode.isCancel(session.returnCode)) convertedFiles.add(FileResult(element, State.Canceled))
                else convertedFiles.add(FileResult(element, State.Failed))

                iterator.remove()
            }
            loadingDialog = ""
            infoDialog = InfoDialog("Files converted and stored at your internal storage at \"VideoConverter\"") {
                infoDialog = null
            }
        }
    }

}