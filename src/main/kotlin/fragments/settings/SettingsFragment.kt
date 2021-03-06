package fragments.settings

import Settings
import Settings.readDatabase
import Settings.writeDatabase
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
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

val elHeight = 55.dp
val elWidth = 550.dp
val textStyle = TextStyle(fontSize = 18.sp)

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingFragment(onNextButtonClick: () -> Unit) {
    val printServices = PrintServiceLookup.lookupPrintServices(null, null)
    val printerList = printServices.map { it.name }

    var printer by remember {
        mutableStateOf(
            if (Settings.printerName.isNotEmpty() && printerList.contains(Settings.printerName))
                Printer(printServices[printerList.indexOf(Settings.printerName)])
            else null
        )
    }
    val sizeList = (printer?.printService?.getSupportedAttributeValues(
        Media::class.java,
        null,
        null
    ) as Array<*>?)?.filterIsInstance<MediaSizeName>()
    var paperSize by remember { mutableStateOf(sizeList?.find { it.toString() == Settings.paperSize }) }

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
                        SettingsBots()

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

                    SettingTabs.MIRROR_CAMERA ->
                        SettingsMirrorCamera()
                }

                if (currSettingTab == SettingTabs.GENERAL)
                    Button(
                        onClick = {
                            val msg = when {
                                Settings.dirInput.isEmpty() || Settings.dirOutput.isEmpty() ||
                                        Settings.dirStickers.isEmpty() || Settings.dirSmileys.isEmpty()
                                -> "???????????????????? ?????????????? ?????? ?????????????????? ?? ?????????????? \"????????????????\""

                                Settings.botNeed && (Settings.botServerAddress.isEmpty() || Settings.botServerPhrase.isEmpty() ||
                                        Settings.botServerState == ConnectionState.UNKNOWN || Settings.telegramBotName.isEmpty() ||
                                        Settings.vkGroupId < 0 || Settings.photoLifeTime < 0)
                                -> "?????? ???????????? ?? ???????????? ???????????????????? ?????????????? ????????????, ????????????, ?????????????????? ???????????????????? ?? ?????? " +
                                        "?? ?????????????????? ?????? ?????????????????? ?? ?????? ??????????????"

                                Settings.emailNeed &&
                                        (emailAddressIsError() || Settings.emailPassword.isEmpty())
                                -> "?????? ???????????????? ???????? ???? ?????????? ???????????????????? ?????????????? ?????????? ?? ????????????"

                                Settings.printNeed &&
                                        (Settings.printerName.isEmpty() || Settings.paperSize.isEmpty())
                                -> "?????? ???????????? ???????????????????? ?????????????? ?????????????? ?? ????????????"

                                Settings.frameNeed && Settings.photoFramePath.isEmpty()
                                -> "???? ?????????????? ?????????? ?? ???????????????????? ??????????, ???? ???? ?????????????? ??????????"

                                !Settings.printNeed && !Settings.botNeed && !Settings.emailNeed
                                -> "???? ???? ?????????????? ???? ???????????? ???????????? ???????????? (????????????, ???????????????? ???? ?????????? ?????? ???????????? ?? ????????????)"

                                Settings.camerasNeed && (Settings.ftpUserLogin.isEmpty() ||
                                        Settings.ftpUserPassword.isEmpty() || Settings.ftpUserPassword.length < 6)
                                -> "?????????????? ???????????????????????? ?????? ???????????????????????? ??/?????? ???????????? ???????????????????????? ?????? ???????????? ?? ????????????????????????????"

                                !Settings.camerasNeed && !Settings.mirrorNeed
                                -> "???? ???? ?????????????? ???? ???????????? ?????????????? ?????????????????? ???????????????????? (?????????????????????? ?????? ????????????????????)"

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

                            writeDatabase(!settingsExists)
                            // ????????????????????, ???????????? ?????? ?????????? ???? ???????????????????? ????????????, ?? ?????????????? ?????????????????????????? ?????? ????????????????
                            Settings.printer = printer?.printService
                            Settings.paper = paperSize

                            onNextButtonClick()
                        },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "????????????",
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
    GENERAL("??????????"),
    BOTS("???????????????????? ????????"),
    PRINT("???????????? ?? ??????????"),
    MIRROR_CAMERA("?????????????????????? ?? ??????????????????????"),
}