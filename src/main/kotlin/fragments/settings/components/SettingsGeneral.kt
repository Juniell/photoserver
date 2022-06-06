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
fun SettingsGeneral() {
    Row {
        Column(
            modifier = Modifier
                .width(elWidth + 2 * 5.dp)  //todo: пофиксить
                .wrapContentHeight()
                .padding(20.dp)
        ) {

            OutlinedTextField(
                label = {
                    Text(
                        text = "Текст приветствия",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.textWelcome.value,
                maxLines = 5,
                textStyle = textStyle,
                onValueChange = { Settings.textWelcome.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(elHeight * 5.6f / 2)
                    .padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория исходных фотографий",
                dialogTitle = "Выбор директории исходных фото",
                file = File(Settings.dirInput.value),
                textStyle = textStyle,
                onDirChoose = { Settings.dirInput.value = it },
                modifier = Modifier.padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория готовых фотографий",
                dialogTitle = "Выбор директории готовых фото",
                file = File(Settings.dirOutput.value),
                textStyle = textStyle,
                onDirChoose = { Settings.dirOutput.value = it },
                modifier = Modifier.padding(bottom = 15.dp)
            )

            PathChooser(
                mode = PathChooserMode.DIR,
                title = "Директория стикеров",
                dialogTitle = "Выбор директории стикеров",
                file = File(Settings.dirStickers.value),
                textStyle = textStyle,
                onDirChoose = { Settings.dirStickers.value = it },
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}