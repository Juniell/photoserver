package fragments.settings.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import fragments.settings.*
import io.ktor.http.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//todo: переименовать в настройки социальных сетей?
//todo: добавить поле для ввода почты и пароля, с которой будет отправляться письмо на почту
@Composable
fun SettingsBots(
    serverAddr: String?,
    serverState: ConnectionState,
    vkId: Int?,
    tgmName: String?,
    photoLifeTime: Int?,
    onServerAddrChange: (newAddr: String) -> Unit,
    onServerStateChange: (state: ConnectionState) -> Unit,
    onServerInfoChange: (vkId: Int, tgmName: String, photoLifeTime: Int) -> Unit
) {
//    var serverState by remember { mutableStateOf(ConnectionState.UNKNOWN) }
    var botSettings by remember { mutableStateOf<BotSettings?>(null) }
    var title = "Последняя сохранённая информация"

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(20.dp)
    ) {

        Text(
            text = "Адрес сервера с ботами",
            maxLines = 1,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(start = 5.dp, bottom = 10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(bottom = 15.dp)
        ) {
            OutlinedTextField(
                value = serverAddr ?: "",
                textStyle = textStyle,
                onValueChange = { onServerAddrChange(it) },
                modifier = Modifier
                    .width(elWidth + 2 * 5.dp - 20 * 2.dp)
            )

            Button(
                enabled = !serverAddr.isNullOrEmpty(),
                onClick = {
                    onServerStateChange(ConnectionState.CHECK)
//                    serverState = ConnectionState.CHECK

                    BotServer.initApi(serverAddr!!)
                    val botServer = BotServer.getApi()

                    val call = botServer.getSettings("")
                    call.enqueue(object : Callback<BotSettings> {
                        override fun onResponse(call: Call<BotSettings>, response: Response<BotSettings>) {
                            botSettings = response.body()
                            if (botSettings != null && response.code() == HttpStatusCode.OK.value) {
                                onServerStateChange(ConnectionState.OK)
//                                serverState = ConnectionState.OK
                                onServerInfoChange(botSettings!!.vkId, botSettings!!.tgmId, botSettings!!.photoLife)
                                title = "Полученная от сервера информация"
                            } else
                                onServerStateChange(ConnectionState.BAD)
//                                serverState = ConnectionState.BAD
                        }

                        override fun onFailure(call: Call<BotSettings>, t: Throwable) {
                            onServerStateChange(ConnectionState.BAD)
//                            serverState = ConnectionState.BAD
                        }
                    })
                }
            ) {
                Text(
                    text = "Проверить\nсоединение",
                    textAlign = TextAlign.Center
                )
            }

            Box(modifier = Modifier.size(40.dp)) {
                when (serverState) {
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

        if (botSettings != null || serverState == ConnectionState.BAD) {

            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
//                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                if (botSettings != null || serverState == ConnectionState.BAD)

                    TwoColumnText(
                        listOf(
                            Pair("Чат во ВКонтакте:", vkId?.let { "vk.me/club$it" } ?: ""),
                            Pair("Чат в Telegram:", tgmName?.let { "tg://resolve?domain=$it" } ?: ""),
                            Pair("Время хранения фотографий (в сутках):", photoLifeTime?.toString() ?: "")
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
                            vkId = vkId,
                            tgmName = tgmName,
                            photoLifeTime = photoLifeTime,
                            onCloseRequest = {
                                dialogVisible = false
                            },
                            onSaveRequest = { vkId, tgmName, photoLifeTime ->
                                onServerInfoChange(vkId, tgmName, photoLifeTime)
                            }
                        )
                }
            }
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