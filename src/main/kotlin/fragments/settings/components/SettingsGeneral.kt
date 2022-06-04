package fragments.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import fragments.settings.*
import java.io.File

@Composable
fun SettingsGeneral(
    textWelcome: String?,
    dirInput: String?,
    dirOutput: String?,
    dirStickers: String?,
    onTextWelcomeChange: (text: String) -> Unit,
    onDirInputChange: (dirInput: String) -> Unit,
    onDirOutputChange: (dirOutput: String) -> Unit,
    onDirStickersChange: (dirStickers: String) -> Unit,
) {
    Row {
        Column(
            modifier = Modifier
                .width(elWidth + 2 * 5.dp)
                .wrapContentHeight()
                .padding(20.dp)
        ) {
            Text(
                text = "Текст приветствия",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            OutlinedTextField(
                value = textWelcome ?: "",
                maxLines = 5,
                textStyle = textStyle,
                onValueChange = { onTextWelcomeChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(elHeight * 5.6f / 2)
                    .padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория исходных фотографий",
                dialogTitle = "Выбор директории исходных фото",
                file = File(dirInput ?: "."),
                textStyle = textStyle,
                onDirChoose = onDirInputChange,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория готовых фотографий",
                dialogTitle = "Выбор директории готовых фото",
                file = File(dirOutput ?: "."),
                textStyle = textStyle,
                onDirChoose = onDirOutputChange,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория стикеров",
                dialogTitle = "Выбор директории стикеров",
                file = File(dirStickers ?: "."),
                textStyle = textStyle,
                onDirChoose = onDirStickersChange,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}