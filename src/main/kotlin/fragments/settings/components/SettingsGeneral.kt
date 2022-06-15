package fragments.settings.components

import Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fragments.settings.elHeight
import fragments.settings.elWidth
import fragments.settings.textStyle
import getImageFilesInDir
import java.io.File

@Composable
fun SettingsGeneral() {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp)
            .width(elWidth)
    ) {

        OutlinedTextField(
            label = {
                Text(
                    text = "Текст приветствия",
                    fontSize = 15.sp,
                )
            },
            value = Settings.textWelcome,
            maxLines = 5,
            textStyle = textStyle,
            onValueChange = {
                if (it.length <= 500)
                    Settings.textWelcome = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(elHeight * 3)
                .padding(bottom = 3.dp)
        )
        Text(
            text = "${Settings.textWelcome.length}/500",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        )

        PathChooser(
            mode = PathChooserMode.DIR,
            title = "Директория исходных фотографий",
            dialogTitle = "Выбор директории исходных фото",
            file = File(Settings.dirInput),
            textStyle = textStyle,
            onDirChoose = { Settings.dirInput = it },
            modifier = Modifier
                .padding(bottom = 3.dp)
        )
        Text(
            text = getImageFilesInDir(File(Settings.dirInput)).let {
                if (it.isEmpty())
                    ""
                else
                    "Внимание: найдено ${it.size} фото (будут видны при выборе фото)"
            },
            maxLines = 1,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        PathChooser(
            mode = PathChooserMode.DIR,
            title = "Директория готовых фотографий",
            dialogTitle = "Выбор директории готовых фото",
            file = File(Settings.dirOutput),
            textStyle = textStyle,
            onDirChoose = { Settings.dirOutput = it },
            modifier = Modifier
                .padding(bottom = 3.dp)
        )

        Text(
            text = getImageFilesInDir(File(Settings.dirStickers)).let {
                if (it.isNotEmpty())
                    "Внимание: выбранная директория не пуста"
                else
                    ""
            },
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        PathChooser(
            mode = PathChooserMode.DIR,
            title = "Директория стикеров",
            dialogTitle = "Выбор директории стикеров",
            file = File(Settings.dirStickers),
            textStyle = textStyle,
            onDirChoose = { Settings.dirStickers = it },
            modifier = Modifier
                .padding(bottom = 3.dp)
        )

        Text(
            text = getImageFilesInDir(File(Settings.dirStickers)).let {
                if (it.isEmpty())
                    "Внимание: выбранная директория стикеров пуста"
                else
                    ""
            },
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        PathChooser(
            mode = PathChooserMode.DIR,
            title = "Директория смайликов",
            dialogTitle = "Выбор директории смайликов",
            file = File(Settings.dirSmileys),
            textStyle = textStyle,
            onDirChoose = { Settings.dirSmileys = it },
            modifier = Modifier
                .padding(bottom = 3.dp)
        )

        Text(
            text = getImageFilesInDir(File(Settings.dirSmileys)).let {
                if (it.isEmpty())
                    "Внимание: выбранная директория смайликов пуста"
                else
                    ""
            },
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}