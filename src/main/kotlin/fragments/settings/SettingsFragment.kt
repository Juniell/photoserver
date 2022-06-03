package fragments.settings

import BotServer
import BotSettings
import Info
import Info.botServerAddress
import Info.dirInput
import Info.dirOutput
import Info.dirStickers
import Info.paperSize
import Info.photoCopiesNum
import Info.photoFramePath
import Info.photoLifeTime
import Info.printerName
import Info.telegramBotName
import Info.textWelcome
import Info.vkGroupId
import InfoSettings
import components.Spinnable
import components.Spinner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.ktor.http.*
import kotlinx.coroutines.launch
import loadImageBitmap
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.standard.Media
import javax.print.attribute.standard.MediaSize
import javax.print.attribute.standard.MediaSizeName
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val settings = readDatabase()

private val elHeight = 55.dp
private val elWidth = 580.dp
private val textStyle = TextStyle(fontSize = 18.sp)

//todo: Давать возможность работать без сервера (галочка). Если так, то даже не отправлять фотки
//todo: Показывать свободное место на диске

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingFragment(onNextButtonClick: (newSettings: InfoSettings, printer: Printer) -> Unit) {
    val printServices = PrintServiceLookup.lookupPrintServices(null, null)
    val printerList = printServices.map { it.name }

    var textWelcome by remember { mutableStateOf(settings?.textWelcome) }
    var dirInput by remember { mutableStateOf(settings?.dirInput) }
    var dirOutput by remember { mutableStateOf(settings?.dirOutput) }
    var dirStickers by remember { mutableStateOf(settings?.dirStickers) }

    var botServerAddr by remember { mutableStateOf(settings?.botServerAddress) }
    var botServerState by remember { mutableStateOf(ConnectionState.UNKNOWN) }
    var vkGroupId by remember { mutableStateOf(settings?.vkGroupId) }
    var tgmBotName by remember { mutableStateOf(settings?.telegramBotName) }
    var photoLifeTime by remember { mutableStateOf(settings?.photoLifeTime/* ?: 2*/) }
    // todo: подумать над photoLifeTime (указывать стандартное или просто null?)

    var printer by remember {
        mutableStateOf(
            if (settings?.printerName != null && printerList.contains(settings.printerName))
                Printer(printServices[printerList.indexOf(settings.printerName)])
            else null
        )
    }
    val sizeList = (printer?.printService?.getSupportedAttributeValues(
        Media::class.java,
        null,
        null
    ) as Array<*>?)?.filterIsInstance<MediaSizeName>()
    var paperSize by remember { mutableStateOf(sizeList?.find { it.toString() == settings?.paperSize }) }
    var photoFramePath by remember { mutableStateOf(settings?.photoFramePath) }
    var photoCopiesNum by remember { mutableStateOf(settings?.photoCopiesNum ?: 1) }

    var currSettingTab by remember { mutableStateOf(SettingTabs.values().first()) }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(it) {
                    Snackbar(modifier = Modifier.wrapContentSize()) {
                        Text(text = it.message)
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Tabs(
                    selectedTab = currSettingTab,
                    onTabSelected = { currSettingTab = it }
                )

                when (currSettingTab) {
                    SettingTabs.GENERAL ->
                        SettingsGeneral(
                            textWelcome = textWelcome,
                            dirInput = dirInput,
                            dirOutput = dirOutput,
                            dirStickers = dirStickers,
                            onTextWelcomeChange = { textWelcome = it },
                            onDirInputChange = { dirInput = it },
                            onDirOutputChange = { dirOutput = it },
                            onDirStickersChange = { dirStickers = it }
                        )

                    SettingTabs.BOTS ->
                        SettingsBots(
                            serverAddr = botServerAddr,
                            serverState = botServerState,
                            vkId = vkGroupId,
                            tgmName = tgmBotName,
                            photoLifeTime = photoLifeTime,
                            onServerAddrChange = { botServerAddr = it },
                            onServerStateChange = { botServerState = it },
                            onServerInfoChange = { vkId, tgmName, photoLife ->
                                vkGroupId = vkId
                                tgmBotName = tgmName
                                photoLifeTime = photoLife
                            }
                        )

                    SettingTabs.PRINT ->
                        SettingsPrint(
                            printer = printer,
                            paperSize = paperSize,
                            photoFramePath = photoFramePath,
                            photoCopiesNum = photoCopiesNum,
                            printerList = printServices.toPrintersList(),
                            onPrinterNameChange = {
                                printer = it
                                paperSize = null
                            },
                            onPaperSizeChange = { paperSize = it },
                            onPhotoFrameChange = { photoFramePath = it },
                            onPhotoCopiesChange = { photoCopiesNum = it }
                        )

                    else -> {}
                }

                if (currSettingTab == SettingTabs.GENERAL)
                    Button(
                        onClick = {
                            val newSettings = InfoSettings(
                                textWelcome = textWelcome,
                                dirInput = dirInput,
                                dirOutput = dirOutput,
                                dirStickers = dirStickers,

                                botServerAddress = botServerAddr,
                                vkGroupId = vkGroupId,
                                telegramBotName = tgmBotName,
                                photoLifeTime = photoLifeTime,

                                printerName = printer.toString(),
                                paperSize = paperSize.toString(),
                                photoFramePath = photoFramePath,
                                photoCopiesNum = photoCopiesNum
                            )

                            if (botServerState == ConnectionState.UNKNOWN) {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        "Пожалуйста, проверьте указанный адрес сервера",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            if (newSettings.textWelcome.isNullOrEmpty() || newSettings.dirInput.isNullOrEmpty() ||
                                newSettings.dirOutput.isNullOrEmpty() || newSettings.dirStickers.isNullOrEmpty() ||
                                newSettings.botServerAddress.isNullOrEmpty() || newSettings.vkGroupId == null ||
                                newSettings.telegramBotName.isNullOrEmpty() || newSettings.photoLifeTime == null ||
                                newSettings.printerName.isNullOrEmpty() || newSettings.paperSize.isNullOrEmpty() ||
                                /*newSettings.photoFramePath.isNullOrEmpty() ||*/ newSettings.photoCopiesNum == null
                            ) {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        "Необходимо указать все настройки",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                writeDatabase(newSettings, settings == null)
                                onNextButtonClick(newSettings, printer!!)
                            }
                        }
                    ) {
                        Text(
                            text = "Запуск",
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                        )
                    }
            }
        }
    }
}

@Composable
fun Tabs(
    selectedTab: SettingTabs,
    onTabSelected: (tab: SettingTabs) -> Unit
) {
    val tabs = SettingTabs.values()
    var selectedTabId by remember { mutableStateOf(tabs.indexOf(selectedTab)) }
    TabRow(
        selectedTabIndex = selectedTabId,
        modifier = Modifier.fillMaxWidth(),
//        indicator = { tabPositions: List<TabPosition> ->
//        },
//        divider = {},
        tabs = {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    text = { Text(tab.nameRus) },
                    selected = selectedTabId == index,
                    onClick = {
                        selectedTabId = index
                        onTabSelected(tab)
                    },
                )
            }
        }
    )
}

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
                            label = "Изменить параметры социальных сетей",
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
fun ChangeSettingsBotDialog(
    label: String,
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
        title = label,
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


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingsPrint(
    printer: Printer?,
    paperSize: MediaSizeName?,
    photoFramePath: String?,
    photoCopiesNum: Int?,
    printerList: List<Printer>,
    onPrinterNameChange: (printer: Printer) -> Unit,
    onPaperSizeChange: (size: MediaSizeName?) -> Unit,
    onPhotoFrameChange: (pathFrame: String?) -> Unit,
    onPhotoCopiesChange: (copiesNum: Int) -> Unit
) {

    var paperSizeList by remember { mutableStateOf(printer?.getPaperSizeList()) }
    var frameFile by remember { mutableStateOf(if (!photoFramePath.isNullOrEmpty()) File(photoFramePath) else null) }
    var frameNeed by remember { mutableStateOf(true) }

    fun checkFrameError() = if (frameNeed)
        !(frameFile != null && frameFile!!.exists() && frameFile!!.isFile && frameFile!!.extension == "png")
    else
        false

    var frameError by remember(frameNeed, frameFile) { mutableStateOf(checkFrameError()) }
    var sampleVisible by remember { mutableStateOf(true) }
    var copiesEnabled by remember { mutableStateOf(photoCopiesNum != null && photoCopiesNum > 1) }
    var copiesNum by remember { mutableStateOf(photoCopiesNum?.toFloat() ?: 1f) }


    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .wrapContentSize()
    ) {

        Column(
            modifier = Modifier
                .width(elWidth + 2 * 5.dp)  //todo: везде поправить
                .padding(20.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = "Принтер",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            Spinner(
                value = printer,
                data = printerList,
                onSelected = { element: Spinnable ->
                    onPrinterNameChange(element as Printer)
                    paperSizeList = element.getPaperSizeList()
                },
                padding = PaddingValues(bottom = 15.dp),
                textFontSize = textStyle.fontSize
            )

            Text(
                text = "Размер бумаги",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )

            Spinner(
                value = paperSize?.let { PaperSize(it) },
                data = paperSizeList ?: emptyList(),
                onSelected = {
                    onPaperSizeChange((it as PaperSize).size)
                },
                padding = PaddingValues(bottom = 15.dp),
                textFontSize = textStyle.fontSize
            )

            Text(
                text = "Пользователь сможет распечатать максимум ${copiesNum.toInt()} фото",
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                    Checkbox(
                        checked = copiesEnabled,
                        onCheckedChange = {
                            copiesEnabled = it
                            copiesNum = if (it) 2f else 1f
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text("Разрешить пользователям печатать несколько копий")
            }

            Slider(
                value = copiesNum,
                valueRange = 2f..6f,
                steps = 3,
                enabled = copiesEnabled,
                onValueChange = { copiesNum = it },
                onValueChangeFinished = { onPhotoCopiesChange(copiesNum.toInt()) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            ) {
                CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                    Checkbox(
                        checked = frameNeed,
                        onCheckedChange = {
                            frameNeed = it
                            sampleVisible = true
                            frameError = checkFrameError()
                            if (!frameNeed)
                                onPhotoFrameChange(null)
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text(
                    text = "Использовать рамку",
                    fontSize = textStyle.fontSize
                )
            }

            if (frameNeed)
                PathChooser(
                    mode = PathChooserMode.IMAGE,
                    title = "Рамка фотографии",
                    dialogTitle = "Выбор рамки фотографий",
                    file = frameFile,
                    isError = frameError,
                    textStyle = textStyle,
                    onDirChoose = {
                        frameFile = File(it)
                        onPhotoFrameChange(it)
                        if (frameError)     // Если корявая рамка, записываем как null //todo: проверить, что в бд записывается null
                            onPhotoFrameChange(null)
                    },
                    modifier = Modifier.padding(bottom = 15.dp)
                )
        }

        if (paperSize != null)
            Column(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                val size = MediaSize.getMediaSizeForName(paperSize).getSize(MediaSize.MM)

                Text(
                    text = "Рамка",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Box(
                    modifier = Modifier
                        .height(500.dp)
                        .aspectRatio(size[0].dp / size[1].dp)
                        .then(
                            if (sampleVisible)
                                Modifier.background(Color.Gray)
                            else
                                Modifier
                        )
                ) {
                    if (sampleVisible)
                        Image(
                            bitmap = loadImageBitmap(File("sample.jpg")),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                    if (frameNeed && !frameError)
                        Image(
                            bitmap = loadImageBitmap(frameFile!!),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sampleVisible,
                        onCheckedChange = { sampleVisible = it },
                        enabled = frameNeed && !frameError
                    )
                    Text("Показать пример фото")
                }
            }
    }
}

@Composable
fun TwoColumnText(
    listPairsOfText: List<Pair<String, String>>,
    fontSize: TextUnit,
    bottomPaddingItem: Dp,
    spaceBetweenColumn: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spaceBetweenColumn),
        modifier = modifier.wrapContentSize()
    ) {
        ColumnText(
            listText = listPairsOfText.map { it.first },
            fontSize = fontSize,
            bottomPaddingItem = bottomPaddingItem
        )
        ColumnText(
            listText = listPairsOfText.map { it.second },
            fontSize = fontSize,
            bottomPaddingItem = bottomPaddingItem
        )
    }
}


@Composable
private fun ColumnText(
    listText: List<String>,
    fontSize: TextUnit,
    bottomPaddingItem: Dp
) {
    Column(modifier = Modifier.wrapContentSize())
    {
        listText.forEach {
            Text(
                text = it,
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = bottomPaddingItem).wrapContentSize()
            )
        }
    }
}


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

fun readDatabase(): InfoSettings? {
    Database.connect("jdbc:sqlite:info.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(Info) // if not exist
        commit()
    }

    return try {
        transaction {
            val row = Info.selectAll().single()

            InfoSettings(
                textWelcome = row[textWelcome],
                dirInput = row[dirInput],
                dirOutput = row[dirOutput],
                dirStickers = row[dirStickers],

                botServerAddress = row[botServerAddress],
                vkGroupId = row[vkGroupId],
                telegramBotName = row[telegramBotName],
                photoLifeTime = row[photoLifeTime],

                printerName = row[printerName],
                paperSize = row[paperSize],
                photoFramePath = row[photoFramePath],
                photoCopiesNum = row[photoCopiesNum]
            )
        }
    } catch (e: NoSuchElementException) {
        return null
    }
}

fun writeDatabase(info: InfoSettings, needInsert: Boolean) {
    transaction {
        if (needInsert)
            Info.insert {
                it[textWelcome] = info.textWelcome!!
                it[dirInput] = info.dirInput!!
                it[dirOutput] = info.dirOutput!!
                it[dirStickers] = info.dirStickers!!

                it[printerName] = info.printerName!!
                it[paperSize] = info.paperSize!!
                it[photoFramePath] = info.photoFramePath!!
                it[photoCopiesNum] = info.photoCopiesNum!!

                it[botServerAddress] = info.botServerAddress!!
                it[vkGroupId] = info.vkGroupId!!
                it[telegramBotName] = info.telegramBotName!!
                it[photoLifeTime] = info.photoLifeTime!!
            }
        else
            Info.update {
                it[textWelcome] = info.textWelcome!!
                it[dirInput] = info.dirInput!!
                it[dirOutput] = info.dirOutput!!
                it[dirStickers] = info.dirStickers!!

                it[printerName] = info.printerName!!
                it[paperSize] = info.paperSize!!
                it[photoFramePath] = info.photoFramePath
                it[photoCopiesNum] = info.photoCopiesNum!!

                it[botServerAddress] = info.botServerAddress!!
                it[vkGroupId] = info.vkGroupId!!
                it[telegramBotName] = info.telegramBotName!!
                it[photoLifeTime] = info.photoLifeTime!!
            }
        commit()
    }
}

class Printer(
    val printService: PrintService
) : Spinnable {

    override fun toString(): String = printService.name

    fun getPaperSizeList(): List<PaperSize> {
        val list = mutableListOf<PaperSize>()
        (printService.getSupportedAttributeValues(Media::class.java, null, null) as Array<*>).forEach {
            if (it is MediaSizeName) {
                println("$it ${it::class.java}")
                list.add(PaperSize(it))
            }
        }
        return list
    }
}

class PaperSize(
    val size: MediaSizeName
) : Spinnable {
    override fun toString(): String {
        return size.toString()
    }
}

fun Array<PrintService>.toPrintersList(): List<Printer> {
    val list = mutableListOf<Printer>()
    this.forEach { list.add(Printer(it)) }
    return list
}

enum class ConnectionState {
    UNKNOWN,
    OK,
    CHECK,
    BAD
}

enum class PathChooserMode(val modeInt: Int) {
    DIR(JFileChooser.DIRECTORIES_ONLY),
    IMAGE(JFileChooser.FILES_ONLY)
}

enum class SettingTabs(val nameRus: String) {
    GENERAL("Общие"),
    BOTS("Боты"),
    PRINT("Принтер"), // Печать?
    MIRROR("Фотозеркало"),
    CAMERA("Фотоаппарат"),
}