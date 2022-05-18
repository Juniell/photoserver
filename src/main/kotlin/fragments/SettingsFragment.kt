package fragments

import Info
import Info.dirInput
import Info.dirOutput
import Info.dirStickers
import Info.printerName
import Info.textWelcome
import InfoSettings
import Spinnable
import Spinner
import SpinnerModifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.print.PrintServiceLookup
import javax.swing.JFileChooser

val settings = readDatabase()

@Composable
fun SettingFragment(onNextButtonClick: (newSettings: InfoSettings) -> Unit) {
    val printServices = PrintServiceLookup.lookupPrintServices(null, null)
    val printerList = printServices.map { it.name }

    var textWelcome by remember { mutableStateOf(settings?.textWelcome ?: "") }
    val printerName = remember {
        mutableStateOf<Spinnable>(
            if (settings?.printerName != null && printerList.contains(settings.printerName))
                Printers(settings.printerName)
            else Printers("")
        )
    }
    val dirInput = remember { mutableStateOf(settings?.dirInput ?: "") }
    val dirOutput = remember { mutableStateOf(settings?.dirOutput ?: "") }
    val dirStickers = remember { mutableStateOf(settings?.dirStickers ?: "") }
    var snackVisible by remember { mutableStateOf(false) }

    val textStyle = TextStyle(fontSize = 18.sp)
    val elHeight = 55.dp
    val elWidth = 580.dp


    MaterialTheme {
        Column(modifier = Modifier/*.fillMaxSize()*/.width(elWidth + 2 * 5.dp).wrapContentHeight().padding(20.dp)) {
            Text(
                text = "Текст приветствия",
                fontSize = 20.sp,
                modifier = Modifier.width(width = elWidth).padding(start = 5.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                visualTransformation = VisualTransformation.None,
                value = textWelcome,
                maxLines = 5,
                textStyle = textStyle,
                onValueChange = { textWelcome = it },
                modifier = Modifier.width(elWidth).height(elHeight * 5 / 2)/*.defaultMinSize(minHeight = elHeight)*/
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Принтер",
                fontSize = 20.sp,
                modifier = Modifier.width(width = elWidth).padding(start = 5.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Spinner(
                data = printerList.toPrintersList(), selectedValue = printerName, onSelected = {}, SpinnerModifier(
                    width = elWidth,
                    padding = PaddingValues(0.dp),
                    height = elHeight,
                    textFontSize = textStyle.fontSize
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            DirChooser(
                "Директория исходных фотографий",
                "Выбор директории исходных фото",
                savedPath = dirInput,
                textStyle = textStyle,
                textBoxHeight = elHeight,
                textBoxWidth = elWidth
            )

            Spacer(modifier = Modifier.height(15.dp))

            DirChooser(
                "Директория готовых фотографий",
                "Выбор директории готовых фото",
                savedPath = dirOutput,
                textStyle = textStyle,
                textBoxHeight = elHeight,
                textBoxWidth = elWidth
            )

            Spacer(modifier = Modifier.height(15.dp))

            DirChooser(
                "Директория стикеров",
                "Выбор директории стикеров",
                savedPath = dirStickers,
                textStyle = textStyle,
                textBoxHeight = elHeight,
                textBoxWidth = elWidth
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val newSettings =
                            InfoSettings(textWelcome, printerName.value.toString(), dirInput.value, dirOutput.value, dirStickers.value)

                        if (newSettings.textWelcome.isNullOrEmpty() || newSettings.printerName.isNullOrEmpty() ||
                            newSettings.dirInput.isNullOrEmpty() || newSettings.dirOutput.isNullOrEmpty()
                        )
                            snackVisible = true
                        else {
                            snackVisible = false
                            writeDatabase(newSettings, settings == null)
                            onNextButtonClick(newSettings)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "Запуск",
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp, vertical = 20.dp)
                    )
                }
            }

            // todo: поднять
            if (snackVisible)
                Snackbar(modifier = Modifier.padding(10.dp)) {
                    Row {
                        Text(text = "Необходимо указать все настройки")
                        Button(onClick = { snackVisible = false }) {
                            Text("OK")
                        }
                    }
                }
        }
    }
}


@Composable
fun DirChooser(
    title: String,
    dialogTitle: String,
    savedPath: MutableState<String>,
    textStyle: TextStyle,
    textBoxHeight: Dp,
    textBoxWidth: Dp
) {
//    var path by remember { mutableStateOf(savedPath.value ?: "") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(width = textBoxWidth)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 5.dp)
        )

        Spacer(modifier = Modifier.width(25.dp))

        Button(modifier = Modifier.padding(end = 5.dp), onClick = {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            chooser.currentDirectory = File(savedPath.value)
            chooser.dialogTitle = dialogTitle
//            chooser.isAcceptAllFileFilterUsed = false
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                savedPath.value = chooser.selectedFile.path // отлавливаем путь
//                onChoose(path)
            }
        }) {
            Text("Выбрать")
        }
    }

    Spacer(modifier = Modifier.height(7.dp))

    OutlinedTextField(
        value = savedPath.value,
        onValueChange = {},
        readOnly = true,
        textStyle = textStyle,
        modifier = Modifier.width(textBoxWidth).height(textBoxHeight)
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
                printerName = row[printerName],
                dirInput = row[dirInput],
                dirOutput = row[dirOutput],
                dirStickers = row[dirStickers]
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
                it[printerName] = info.printerName!!
                it[dirInput] = info.dirInput!!
                it[dirOutput] = info.dirOutput!!
                it[dirStickers] = info.dirStickers!!
            }
        else
            Info.update {
                it[textWelcome] = info.textWelcome!!
                it[printerName] = info.printerName!!
                it[dirInput] = info.dirInput!!
                it[dirOutput] = info.dirOutput!!
                it[dirStickers] = info.dirStickers!!
            }
        commit()
    }
}

class Printers(private val printer: String) : Spinnable {
    override fun toString(): String = printer
}

fun List<String>.toPrintersList(): List<Printers> {
    val list = mutableListOf<Printers>()
    this.forEach { list.add(Printers(it)) }
    return list
}