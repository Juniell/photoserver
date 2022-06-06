package fragments.settings.components

import BotServer
import BotSettings
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
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import fragments.settings.elWidth
import fragments.settings.textStyle
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress

//todo: переименовать в настройки социальных сетей?
@ExperimentalMaterialApi
@Composable
fun SettingsBots(
    serverState: ConnectionState,
    onServerStateChange: (state: ConnectionState) -> Unit,
) {
    var botSettings by remember { mutableStateOf<BotSettings?>(null) }  //todo: если убрать null, то будет сохраняться показ
    var title by remember { mutableStateOf("Последняя сохранённая информация") }
    var emailAddressError by remember { mutableStateOf(false) } //todo: проверка для данных из бд

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(20.dp)
    ) {

        var inetCheckState by remember { mutableStateOf(ConnectionState.UNKNOWN) }

        if (inetCheckState == ConnectionState.UNKNOWN)
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    inetCheckState = ConnectionState.CHECK
                    val inetState = InetAddress.getByName("8.8.8.8").isReachable(15)

                    inetCheckState = if (inetState) ConnectionState.OK else ConnectionState.BAD
                }
            }

        Text(
            text = "Доступ в интернет",
            fontSize = 20.sp,
            modifier = Modifier
                .padding(start = 5.dp, bottom = 10.dp)
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 15.dp)
        ) {

            progress(inetCheckState, 30.dp)

            Text(
                text = when (inetCheckState) {
                    ConnectionState.UNKNOWN -> "Не известно, есть ли доступ в интернет"
                    ConnectionState.CHECK -> "Проверка доступа в интернет"
                    ConnectionState.OK -> "Доступ в интернет есть"
                    ConnectionState.BAD -> "Доступ в интернет отсутствует"
                },
                fontSize = 17.sp,
            )
        }



        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 15.dp)
        ) {
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                Checkbox(
                    checked = Settings.emailNeed.value,
                    onCheckedChange = {
                        Settings.emailNeed.value = it
                    },
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Text(
                text = "Разрешить пользователям получать фото по почте",
                fontSize = textStyle.fontSize
            )
        }

        if (Settings.emailNeed.value) {
            OutlinedTextField(
                label = {
                    Text(
                        text = "Почта для отправки фотографий*",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.emailAddress.value,
                textStyle = textStyle,
                isError = emailAddressError,
                onValueChange = {
                    Settings.emailAddress.value = it
                    emailAddressError = !Settings.emailAddress.value.matches(Regex("""(\w|\d|_)+@gmail\.com"""))
                },
                modifier = Modifier
                    .width(elWidth + 2 * 5.dp - 20 * 2.dp)
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
                value = Settings.emailPassword.value,
                textStyle = textStyle,
                visualTransformation = { text ->
                    TransformedText(AnnotatedString("*".repeat(text.length)), OffsetMapping.Identity)
                },
                onValueChange = {
                    Settings.emailPassword.value = it
                },
                modifier = Modifier
                    .width(elWidth + 2 * 5.dp - 20 * 2.dp)
            )
            Text(
                text = "* Необходим пароль, сгенерированный специально для приложений",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 15.dp)
        ) {
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                Checkbox(
                    checked = Settings.botNeed.value,
                    onCheckedChange = {
                        Settings.botNeed.value = it
                    },
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Text(
                text = "Работать с ботами",
                fontSize = textStyle.fontSize
            )
        }


        if (!Settings.botNeed.value)
            return@Column

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(bottom = 15.dp)
        ) {
            OutlinedTextField(
                label = {
                    Text(
                        text = "Адрес сервера с ботами",
                        fontSize = 15.sp,
                    )
                },
                value = Settings.botServerAddress.value,
                textStyle = textStyle,
                onValueChange = { Settings.botServerAddress.value = it },
                modifier = Modifier
                    .width(elWidth + 2 * 5.dp - 20 * 2.dp)  //todo: пофиксить
            )

            Button(
                enabled = Settings.botServerAddress.value.isNotEmpty(),
                onClick = {
                    onServerStateChange(ConnectionState.CHECK)

                    BotServer.initApi(Settings.botServerAddress.value)
                    val botServer = BotServer.getApi()

                    val call = botServer.getSettings()
                    call.enqueue(object : Callback<BotSettings> {
                        override fun onResponse(call: Call<BotSettings>, response: Response<BotSettings>) {
                            botSettings = response.body()
                            if (botSettings != null && response.code() == HttpStatusCode.OK.value) {
                                onServerStateChange(ConnectionState.OK)
                                saveNewServerInfo(botSettings!!)
                                title = "Полученная от сервера информация"
                            } else
                                onServerStateChange(ConnectionState.BAD)
                        }

                        override fun onFailure(call: Call<BotSettings>, t: Throwable) {
                            onServerStateChange(ConnectionState.BAD)
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
                connectionState = serverState,
                size = 40.dp
            )
        }

        if (botSettings != null || serverState == ConnectionState.BAD) {

            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.wrapContentSize()
            ) {
                if (botSettings != null || serverState == ConnectionState.BAD)

                    TwoColumnText(
                        listOf(
                            Pair(
                                "Чат во ВКонтакте:",
                                if (Settings.vkGroupId.value < 0)
                                    ""
                                else
                                    "vk.me/club${Settings.vkGroupId.value}"
                            ),
                            Pair(
                                "Чат в Telegram:",
                                if (Settings.telegramBotName.value.isEmpty())
                                    ""
                                else
                                    "tg://resolve?domain=${Settings.telegramBotName.value}"
                            ),
                            Pair(
                                "Время хранения фотографий (в сутках):",
                                if (Settings.photoLifeTime.value < 0)
                                    ""
                                else
                                    Settings.photoLifeTime.value.toString()
                            )
                        ),
                        fontSize = 17.sp,
                        bottomPaddingItem = 10.dp,
                        spaceBetweenColumn = 10.dp,
                        modifier = Modifier
                            .padding(start = 10.dp)
                    )

                if (serverState == ConnectionState.BAD) {
                    var dialogVisible by remember { mutableStateOf(false) }

                    Button(
                        onClick = { dialogVisible = true }
                    ) {
                        Text("Изменить")
                    }


                    if (dialogVisible)
                        ChangeSettingsBotDialog(
                            vkId = Settings.vkGroupId.value,
                            tgmName = Settings.telegramBotName.value,
                            photoLifeTime = Settings.photoLifeTime.value,
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
    Settings.vkGroupId.value = botSettings.vkId
    Settings.telegramBotName.value = botSettings.tgmId
    Settings.photoLifeTime.value = botSettings.photoLife
}

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