package bayern.kickner.videoconverter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bayern.kickner.loadingdialogs.Type
import bayern.kickner.videoconverter.ui.theme.VIdeoConverterTheme
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File


class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    val fileChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            mainViewModel.handleChosenVideos(it, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.clearFilesToConvertAndDeleteThem(this)
        setContent {
            VIdeoConverterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Root()

                    mainViewModel.infoDialog?.let {
                        InfoDialog(infoDialog = it)
                    }
                    LoadingDialog()
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Root() {
        Column(modifier = Modifier.padding(8.dp)) {
            OutlinedButton(onClick = { mainViewModel.chooseVideos(this@MainActivity) }) {
                Text(text = "Select Video")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                stickyHeader {
                    Text(
                        text = "Selected Videos",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(mainViewModel.filesToConvert.toList()) {
                    Text(it.name)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (mainViewModel.convertedFiles.isNotEmpty()) {
                    stickyHeader {
                        Text(
                            text = "Converted Videos",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(mainViewModel.convertedFiles.toList()) {
                    Text("${it.file.name} (${it.state.name})")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(R.string.raw_input_hint), fontStyle = FontStyle.Italic, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = mainViewModel.ffmpegCommand,
                onValueChange = { mainViewModel.ffmpegCommand = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { mainViewModel.convertFiles(this@MainActivity) }) {
                Text(
                    text = "Convert File(s)", modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), textAlign = TextAlign.Center
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InfoDialog(infoDialog: InfoDialog) {
        AlertDialog(onDismissRequest = { }) {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                Text(text = infoDialog.msg)
                Button(onClick = infoDialog.onFinish) {
                    Text(text = "Ok")
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun LoadingDialog() {
        bayern.kickner.loadingdialogs.LoadingDialog(
            show = mainViewModel.loadingDialog.isNotBlank(),
            mainViewModel.loadingDialog,
            type = Type.KITT
        )
    }
}
