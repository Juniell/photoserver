package fragments.settings.components

import FtpServer
import MirrorsServer
import Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.CheckboxWithText
import fragments.settings.elWidth
import fragments.settings.textStyle
import java.net.NetworkInterface

@ExperimentalMaterialApi
@Composable
fun SettingsMirrorCamera() {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(20.dp)
    ) {

        val ipAddress by remember { mutableStateOf(getLocalIp()) }

        CheckboxWithText(
            checked = Settings.mirrorNeed,
            onCheckedChange = { Settings.mirrorNeed = it },
            text = "Получать фото с фотозеркал",
            fontSize = textStyle.fontSize,
            spaceBetween = 10.dp,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        if (Settings.mirrorNeed) {
            Text(
                text = "В настройках фотозеркала укажите следующее:",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )

            TwoColumnText(
                listPairsOfText = listOf(
                    "IP адрес:" to ipAddress,
                    "Номер порта:" to "${MirrorsServer.getPort()}"
                ),
                fontSize = 15.sp,
                bottomPaddingItem = 10.dp,
                spaceBetweenColumn = 7.dp
            )

            Text(
                text = "Настройте фотозеркало на отправку JPG файлов (RAW файлы будут игнорироваться)",
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )
        }


        CheckboxWithText(
            checked = Settings.camerasNeed,
            onCheckedChange = { Settings.camerasNeed = it },
            text = "Получать фото с фотоаппаратов",
            fontSize = textStyle.fontSize,
            spaceBetween = 10.dp,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        if (Settings.camerasNeed) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    label = {
                        Text(
                            text = "Придумайте логин*",
                            fontSize = 15.sp,
                        )
                    },
                    value = Settings.ftpUserLogin,
                    onValueChange = {
                        Settings.ftpUserLogin = it
                    },
                    isError = Settings.ftpUserLogin.isEmpty(),
                    modifier = Modifier
                        .width(elWidth)
                )
            }
            Text(
                text = "* Логин не может быть пустым",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 15.dp)
            )

            OutlinedTextField(
                label = {
                    Text(
                        text = "Придумайте пароль*",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.ftpUserPassword,
                onValueChange = { Settings.ftpUserPassword = it },
                isError = Settings.ftpUserPassword.isEmpty() || Settings.ftpUserPassword.length < 6,
                visualTransformation = { text ->
                    TransformedText(AnnotatedString("*".repeat(text.length)), OffsetMapping.Identity)
                },
                modifier = Modifier
                    .width(elWidth)
            )
            Text(
                text = "* Пароль должен состоять из не менее 6 символов",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 15.dp)
            )

            Text(
                text = "В настройках фотоаппарата укажите следующее:",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )

            TwoColumnText(
                listPairsOfText = listOf(
                    "Адрес FTP-сервера:" to (ipAddress),
                    "Номер порта:" to "${FtpServer.getPort()}",
                    "Режим работы:" to "Любой",
                    "Прокси-сервер:" to "Нет",
                    "Способ аутентификации:" to "Пароль"
                ),
                fontSize = 15.sp,
                bottomPaddingItem = 10.dp,
                spaceBetweenColumn = 7.dp
            )

            ColumnText(
                listText = listOf(
                    "Для подключения используйте логин и пароль, которые вы указали выше",
                    "Настройте фотоаппарат на отправку JPG файлов (RAW файлы будут игнорироваться)"
                ),
                fontSize = 15.sp,
                bottomPaddingItem = 10.dp
            )
        }
    }
}

fun getLocalIp(): String {
    val interfaces = NetworkInterface.networkInterfaces()
    var address = ""

    for (el in interfaces) {
        if (!el.isVirtual && !el.isLoopback && el.isUp && !el.isPointToPoint &&
            !el.displayName.contains("virtual", true)
        )
            address = el.inetAddresses.toList().first().hostAddress
    }
    return address
}