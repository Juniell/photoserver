package fragments.settings.components

import BotClient
import BotSettings
import Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import components.CheckboxWithText
import fragments.settings.elWidth
import fragments.settings.textStyle
import io.ktor.http.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@ExperimentalMaterialApi
@Composable
fun SettingsBots() {
    var botSettings by remember { mutableStateOf<BotSettings?>(null) }
    var title by remember { mutableStateOf("Последняя сохранённая информация") }
    var emailAddressError by remember { mutableStateOf(emailAddressIsError()) }
    var botAddressInfo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(20.dp)
    ) {

        CheckboxWithText(
            checked = Settings.emailNeed,
            onCheckedChange = {
                Settings.emailNeed = it
            },
            text = "Разрешить пользователям получать фото по почте",
            fontSize = textStyle.fontSize,
            spaceBetween = 10.dp,
            modifier = Modifier
                .padding(bottom = 15.dp)
        )

        if (Settings.emailNeed) {
            OutlinedTextField(
                label = {
                    Text(
                        text = "Почта для отправки фотографий*",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.emailAddress,
                textStyle = textStyle,
                isError = emailAddressError,
                onValueChange = {
                    Settings.emailAddress = it
                    emailAddressError = emailAddressIsError()
                },
                modifier = Modifier
                    .width(elWidth)
            )
            Text(
                text = "* Пока поддерживается только Gmail",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            OutlinedTextField(
                label = {
                    Text(
                        text = "Пароль от почты*",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.emailPassword,
                textStyle = textStyle,
                visualTransformation = { text ->
                    TransformedText(AnnotatedString("*".repeat(text.length)), OffsetMapping.Identity)
                },
                isError = Settings.emailPassword.isEmpty(),
                onValueChange = { Settings.emailPassword = it },
                modifier = Modifier
                    .width(elWidth)
            )
            Text(
                text = "* Необходим пароль, сгенерированный специально для приложений",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }

        CheckboxWithText(
            checked = Settings.botNeed,
            onCheckedChange = {
                Settings.botNeed = it
            },
            text = "Работать с ботами",
            fontSize = textStyle.fontSize,
            spaceBetween = 10.dp,
            modifier = Modifier
                .padding(bottom = 15.dp)
        )

        if (!Settings.botNeed)
            return@Column

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                label = {
                    Text(
                        text = "Адрес сервера с ботами",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.botServerAddress,
                textStyle = textStyle,
                onValueChange = {
                    Settings.botServerAddress = it
                    Settings.botServerState = ConnectionState.UNKNOWN
                },
                modifier = Modifier
                    .width(elWidth)
            )

            Button(
                enabled = Settings.botServerAddress.isNotEmpty() && Settings.botServerPhrase.isNotEmpty(),
                onClick = {
                    Settings.botServerState = ConnectionState.CHECK

                    try {
                        BotClient.initApi(Settings.botServerAddress)
                        botAddressInfo = ""
                    } catch (e: IllegalArgumentException) {
                        botAddressInfo = "Указан неверный адрес"
                        Settings.botServerState = ConnectionState.BAD
                        return@Button
                    }
                    val botServer = BotClient.getApi()

                    val call = botServer.getSettings(Settings.botServerPhrase)
                    call.enqueue(object : Callback<BotSettings> {
                        override fun onResponse(call: Call<BotSettings>, response: Response<BotSettings>) {
                            botSettings = response.body()
                            if (botSettings != null && response.code() == HttpStatusCode.OK.value) {
                                Settings.botServerState = ConnectionState.OK
                                saveNewServerInfo(botSettings!!)
                                title = "Полученная от сервера информация"
                            } else
                                Settings.botServerState = ConnectionState.BAD
                        }

                        override fun onFailure(call: Call<BotSettings>, t: Throwable) {
                            Settings.botServerState = ConnectionState.BAD
                        }
                    })
                }
            ) {
                Text(
                    text = "Проверить\nсоединение",
                    textAlign = TextAlign.Center
                )
            }

            progress(
                connectionState = Settings.botServerState,
                size = 40.dp
            )
        }

        Text(
            text = botAddressInfo,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 15.dp)
        )

        OutlinedTextField(
            label = {
                Text(
                    text = "Пароль для общения с сервером ботов",
                    fontSize = 15.sp,
                )
            },
            value = Settings.botServerPhrase,
            visualTransformation = { text ->
                TransformedText(AnnotatedString("*".repeat(text.length)), OffsetMapping.Identity)
            },
            isError = Settings.botServerPhrase.isEmpty(),
            textStyle = textStyle,
            onValueChange = { Settings.botServerPhrase = it },
            modifier = Modifier
                .width(elWidth)
                .padding(bottom = 10.dp)
        )

        if (botSettings != null || Settings.botServerState == ConnectionState.BAD) {

            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                if (botSettings != null || Settings.botServerState == ConnectionState.BAD)

                    TwoColumnText(
                        listOf(
                            Pair(
                                "Чат во ВКонтакте:",
                                if (Settings.vkGroupId < 0)
                                    ""
                                else
                                    "vk.me/club${Settings.vkGroupId}"
                            ),
                            Pair(
                                "Чат в Telegram:",
                                if (Settings.telegramBotName.isEmpty())
                                    ""
                                else
                                    "tg://resolve?domain=${Settings.telegramBotName}"
                            ),
                            Pair(
                                "Время хранения фотографий (в сутках):",
                                if (Settings.photoLifeTime < 0)
                                    ""
                                else
                                    Settings.photoLifeTime.toString()
                            )
                        ),
                        fontSize = 17.sp,
                        bottomPaddingItem = 10.dp,
                        spaceBetweenColumn = 10.dp,
                        modifier = Modifier
                            .padding(start = 10.dp)
                    )

                if (Settings.botServerState == ConnectionState.BAD) {
                    var dialogVisible by remember { mutableStateOf(false) }

                    Button(
                        onClick = { dialogVisible = true }
                    ) {
                        Text("Изменить")
                    }


                    if (dialogVisible)
                        ChangeSettingsBotDialog(
                            vkId = Settings.vkGroupId,
                            tgmName = Settings.telegramBotName,
                            photoLifeTime = Settings.photoLifeTime,
                            onCloseRequest = {
                                dialogVisible = false
                            },
                            onSaveRequest = { vkId, tgmName, photoLifeTime ->
                                botSettings = BotSettings(vkId, tgmName, photoLifeTime)
                                saveNewServerInfo(botSettings!!)
                            }
                        )
                }
            }
        }
    }
}

fun saveNewServerInfo(botSettings: BotSettings) {
    Settings.vkGroupId = botSettings.vkId
    Settings.telegramBotName = botSettings.tgmId
    Settings.photoLifeTime = botSettings.photoLife
}

fun emailAddressIsError() = !Settings.emailAddress.matches(Regex("""(\w|\d|_)+@gmail\.com"""))

@Composable
fun progress(
    connectionState: ConnectionState,
    size: Dp
) {
    Box(modifier = Modifier.size(size)) {
        when (connectionState) {
            ConnectionState.OK -> Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.matchParentSize()
            )
            ConnectionState.BAD -> Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.matchParentSize()
            )
            ConnectionState.CHECK -> CircularProgressIndicator(
                modifier = Modifier.matchParentSize()
            )
            ConnectionState.UNKNOWN -> {}
        }
    }
}

@Composable
private fun ChangeSettingsBotDialog(
    vkId: Int?,
    tgmName: String?,
    photoLifeTime: Int?,
    onCloseRequest: () -> Unit,
    onSaveRequest: (vkId: Int, tgmName: String, photoLifeTime: Int) -> Unit,
) {
    var vkEditableText by remember { mutableStateOf(vkId?.toString() ?: "") }
    var tgmEditableText by remember { mutableStateOf(tgmName ?: "") }
    var lifeTimeEditableText by remember { mutableStateOf(photoLifeTime?.toString() ?: "") }
    val textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)

    Dialog(
        title = "Изменить параметры социальных сетей",
        state = rememberDialogState(height = Dp.Unspecified),
        onCloseRequest = {
            onCloseRequest()
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(10.dp)
                .width(400.dp)
        ) {
            Text(
                text = "Внимание: Эти данные влияют только на генерацию QR-кодов и информацию, " +
                        "предоставляемую пользователям. Работа сервера от этого не изменится",
                maxLines = 4,
                textAlign = TextAlign.Start,
                fontSize = 15.sp,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("ID группы во Вконтакте") },
                value = vkEditableText,
                textStyle = textStyle,
                singleLine = true,
                maxLines = 1,
                onValueChange = {
                    vkEditableText = Regex("""[^\d]""").replace(it, "")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Имя бота в Telegram") },
                value = tgmEditableText,
                textStyle = textStyle,
                singleLine = true,
                maxLines = 1,
                onValueChange = {
                    tgmEditableText = Regex("""[^a-zA-Z]""").replace(it, "")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Время хранения фото на сервере (в сутках)") },
                value = lifeTimeEditableText,
                textStyle = textStyle,
                singleLine = true,
                maxLines = 1,
                onValueChange = {
                    lifeTimeEditableText = Regex("""[^\d]""").replace(it, "")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onSaveRequest(vkEditableText.toInt(), tgmEditableText, lifeTimeEditableText.toInt())
                    onCloseRequest()
                },
                enabled = vkEditableText.isNotEmpty() && tgmEditableText.isNotEmpty() && lifeTimeEditableText.isNotEmpty()
            ) {
                Text("Применить")
            }
        }
    }
}

enum class ConnectionState {
    UNKNOWN,
    OK,
    CHECK,
    BAD
}