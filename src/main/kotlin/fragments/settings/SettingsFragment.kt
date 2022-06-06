package fragments.settings

import Settings
import Settings.readDatabase
import Settings.writeDatabase
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
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.standard.Media
import javax.print.attribute.standard.MediaSizeName

private val settingsExists = readDatabase()
private val oldVkGroupId = Settings.vkGroupId

val elHeight = 55.dp
val elWidth = 580.dp
val textStyle = TextStyle(fontSize = 18.sp)

//todo: Показывать свободное место на диске

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingFragment(onNextButtonClick: () -> Unit) {
    val printServices = PrintServiceLookup.lookupPrintServices(null, null)
    val printerList = printServices.map { it.name }
    var botServerState by remember { mutableStateOf(ConnectionState.UNKNOWN) }

    var printer by remember {
        mutableStateOf(
            if (Settings.printerName.value.isNotEmpty() && printerList.contains(Settings.printerName.value))
                Printer(printServices[printerList.indexOf(Settings.printerName.value)])
            else null
        )
    }
    val sizeList = (printer?.printService?.getSupportedAttributeValues(
        Media::class.java,
        null,
        null
    ) as Array<*>?)?.filterIsInstance<MediaSizeName>()
    var paperSize by remember { mutableStateOf(sizeList?.find { it.toString() == Settings.paperSize.value }) }

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
                        SettingsGeneral()

                    SettingTabs.BOTS ->
                        SettingsBots(
                            serverState = botServerState,
                            onServerStateChange = { botServerState = it },
                        )

                    SettingTabs.PRINT ->
                        SettingsPrint(
                            printer = printer,
                            paperSize = paperSize,
                            printerList = printServices.toPrintersList(),
                            onPrinterNameChange = {
                                printer = it
                                paperSize = null
                            },
                            onPaperSizeChange = {
                                paperSize = it
                            },
                        )

                    else -> {}
                }

                if (currSettingTab == SettingTabs.GENERAL)
                    Button(
                        onClick = {
                            val msg = when {
                                Settings.dirInput.value.isEmpty() || Settings.dirOutput.value.isEmpty() ||
                                        Settings.dirStickers.value.isEmpty()
                                -> "Необходимо указать все настройки в разделе \"Основные\""

                                Settings.botNeed.value && (Settings.botServerAddress.value.isEmpty() ||
                                        botServerState == ConnectionState.UNKNOWN || Settings.telegramBotName.value.isEmpty() ||
                                        Settings.vkGroupId.value < 0 || Settings.photoLifeTime.value < 0)
                                -> "При режиме работы с ботами необходимо указать сервер, проверить соединение с ним " +
                                        "и проверить все настройки в его разделе"

                                Settings.emailNeed.value &&
                                        (Settings.emailAddress.value.isEmpty() || Settings.emailPassword.value.isEmpty())
                                -> "Для отправки фото по почте необходимо указать почту и пароль"

                                Settings.printNeed.value &&
                                        (Settings.printerName.value.isEmpty() || Settings.paperSize.value.isEmpty())
                                -> "Для печати необходимо указать принтер и бумагу"

                                Settings.frameNeed.value && Settings.photoFramePath.value.isEmpty()
                                -> "Вы выбрали режим с наложением рамки, но не выбрали рамку"

                                !Settings.printNeed.value && !Settings.botNeed.value && !Settings.emailNeed.value
                                -> "Вы не выбрали ни одного режима работы (печать, отправка на почту или работа с ботами)"

                                else -> ""
                            }

                            if (msg.isNotEmpty()) {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        msg,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val vkGroupChange = Settings.vkGroupId != oldVkGroupId

                            writeDatabase(!settingsExists)
                            // Необходимо, потому что могут не вызываться методы, в которых присваиваются эти значения
                            Settings.printer = printer?.printService
                            Settings.paper = paperSize
                            Settings.vkGroupChange = vkGroupChange

                            onNextButtonClick()
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

private fun Array<PrintService>.toPrintersList(): List<Printer> {
    val list = mutableListOf<Printer>()
    this.forEach { list.add(Printer(it)) }
    return list
}

enum class SettingTabs(val nameRus: String) {
    GENERAL("Общие"),
    BOTS("Социальные сети"),
    PRINT("Печать и рамка"),
    MIRROR("Фотозеркало"),
    CAMERA("Фотоаппарат"),
}