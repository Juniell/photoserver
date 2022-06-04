package fragments.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

//todo: подумать над отображением длинных путей
@Composable
fun PathChooser(
    modifier: Modifier = Modifier,
    mode: PathChooserMode,
    title: String,
    dialogTitle: String,
    file: File?,
    textStyle: TextStyle,
    onDirChoose: (path: String) -> Unit,
    isError: Boolean = false
) {
    var savedPath by remember { mutableStateOf(file?.path) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp)
            )

            Button(
                modifier = Modifier
                    .padding(end = 5.dp),
                onClick = {
                    val chooser = JFileChooser()
                    chooser.dialogTitle = dialogTitle
                    chooser.fileSelectionMode = mode.modeInt

                    if (mode == PathChooserMode.IMAGE) {
                        chooser.currentDirectory = file?.parentFile ?: File(".")
                        chooser.fileFilter = FileNameExtensionFilter(
                            "Image files",
//                        *ImageIO.getReaderFileSuffixes()
                            "png"
                        )
                    } else {
                        chooser.currentDirectory = File(savedPath ?: ".")
                    }

                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        val newPath = chooser.selectedFile.path
                        savedPath = newPath // отлавливаем путь
                        onDirChoose(newPath)
                    }
                }
            ) {
                Text("Выбрать")
            }
        }

        OutlinedTextField(
            value = savedPath ?: "",
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { this.canFocus = false },
            isError = isError
        )
    }
}


enum class PathChooserMode(val modeInt: Int) {
    DIR(JFileChooser.DIRECTORIES_ONLY),
    IMAGE(JFileChooser.FILES_ONLY)
}