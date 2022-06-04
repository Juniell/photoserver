package fragments.settings

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fragments.settings.components.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.standard.Media
import javax.print.attribute.standard.MediaSizeName

private val settings = readDatabase()

val elHeight = 55.dp
val elWidth = 580.dp
val textStyle = TextStyle(fontSize = 18.sp)

//todo: Давать возможность работать без сервера (галочка). Если так, то даже не отправлять фотки
//todo: Показывать свободное место на диске

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingFragment(
    onNextButtonClick: (newSettings: InfoSettings, printer: Printer, paperSize: MediaSizeName, vkChange: Boolean) -> Unit
) {
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
                                val vkGroupChange = settings?.vkGroupId != newSettings.vkGroupId

                                writeDatabase(newSettings, settings == null)
                                onNextButtonClick(newSettings, printer!!, paperSize!!, vkGroupChange)
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

private fun Array<PrintService>.toPrintersList(): List<Printer> {
    val list = mutableListOf<Printer>()
    this.forEach { list.add(Printer(it)) }
    return list
}

enum class SettingTabs(val nameRus: String) {
    GENERAL("Общие"),
    BOTS("Боты"),
    PRINT("Печать"),
    MIRROR("Фотозеркало"),
    CAMERA("Фотоаппарат"),
}