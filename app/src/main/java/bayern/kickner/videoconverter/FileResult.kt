package bayern.kickner.videoconverter

import java.io.File

data class FileResult(val file: File, val state: State)
enum class State {
    Succeed, Failed, Canceled
}
