package fragments.settings.components

import Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.CheckboxWithText
import components.Spinnable
import components.Spinner
import fragments.settings.elWidth
import fragments.settings.textStyle
import loadImageBitmap
import java.io.File
import javax.print.PrintService
import javax.print.attribute.standard.Media
import javax.print.attribute.standard.MediaSize
import javax.print.attribute.standard.MediaSizeName

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SettingsPrint(
    printer: Printer?,
    paperSize: MediaSizeName?,
    printerList: List<Printer>,
    onPrinterNameChange: (printer: Printer) -> Unit,
    onPaperSizeChange: (size: MediaSizeName?) -> Unit,
) {
    fun checkFrameError(): Boolean {
        val frameFile = if (Settings.photoFramePath.isNotEmpty())
            File(Settings.photoFramePath)
        else
            null

        return if (Settings.frameNeed)
            !(frameFile != null && frameFile.exists() && frameFile.isFile && frameFile.extension == "png")
        else
            false
    }

    var frameError by remember(Settings.frameNeed, Settings.photoFramePath) {
        mutableStateOf(checkFrameError())
    }
    var paperSizeList by remember { mutableStateOf(printer?.getPaperSizeList()) }
    var sampleVisible by remember { mutableStateOf(true) }
    var copiesEnabled by remember { mutableStateOf(Settings.photoCopiesNum > 1) }
    var copiesNum by remember { mutableStateOf(Settings.photoCopiesNum.toFloat()) }


    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .wrapContentSize()
    ) {

        Column(
            modifier = Modifier
                .padding(20.dp)
                .width(elWidth)
                .wrapContentHeight()
        ) {
            CheckboxWithText(
                checked = Settings.printNeed,
                onCheckedChange = {
                    Settings.printNeed = it
                },
                text = "Разрешить печатать фотографии",
                fontSize = textStyle.fontSize,
                spaceBetween = 10.dp,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            )

            if (Settings.printNeed) {
                Spinner(
                    label = {
                        Text(
                            text = "Принтер",
                            fontSize = 15.sp,
                        )
                    },
                    value = printer,
                    data = printerList,
                    onSelected = { element: Spinnable ->
                        onPrinterNameChange(element as Printer)
                        Settings.printerName = element.printService.name
                        Settings.paperSize = ""
                        Settings.printer = element.printService
                        Settings.paper = null
                        paperSizeList = element.getPaperSizeList()
                    },
                    padding = PaddingValues(bottom = 15.dp),
                    textFontSize = textStyle.fontSize
                )

                Spinner(
                    label = {
                        Text(
                            text = "Размер бумаги",
                            fontSize = 15.sp,
                        )
                    },
                    value = paperSize?.let { PaperSize(it) },
                    data = paperSizeList ?: emptyList(),
                    onSelected = {
                        onPaperSizeChange((it as PaperSize).size)
                        Settings.paper = it.size
                        Settings.paperSize = it.toString()
                    },
                    padding = PaddingValues(bottom = 15.dp),
                    textFontSize = textStyle.fontSize
                )

                Text(
                    text = "Пользователь сможет распечатать максимум ${Settings.photoCopiesNum} фото",
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                )

                CheckboxWithText(
                    checked = copiesEnabled,
                    onCheckedChange = {
                        copiesEnabled = it
                        copiesNum = if (it) 2f else 1f
                        Settings.photoCopiesNum = copiesNum.toInt()
                    },
                    text = "Разрешить пользователям печатать несколько копий",
                    fontSize = textStyle.fontSize,
                    spaceBetween = 10.dp,
                )

                Slider(
                    value = copiesNum,
                    valueRange = 2f..6f,
                    steps = 3,
                    enabled = copiesEnabled,
                    onValueChange = { copiesNum = it },
                    onValueChangeFinished = {
                        Settings.photoCopiesNum = copiesNum.toInt()
                    }
                )
            }

            CheckboxWithText(
                checked = Settings.frameNeed,
                onCheckedChange = {
                    sampleVisible = true
                    Settings.frameNeed = it
                    frameError = checkFrameError()

                    if (frameError)
                        Settings.photoFramePath = ""
                },
                text = "Клеить рамку на итоговое фото",
                fontSize = textStyle.fontSize,
                spaceBetween = 10.dp,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            )

            if (Settings.frameNeed)
                PathChooser(
                    mode = PathChooserMode.IMAGE,
                    title = "Рамка фотографии",
                    dialogTitle = "Выбор рамки фотографий",
                    file = File(Settings.photoFramePath),
                    isError = frameError,
                    textStyle = textStyle,
                    onDirChoose = {
                        Settings.photoFramePath = it
                        frameError = checkFrameError()

                        if (frameError)
                            Settings.photoFramePath = ""

                    },
                    modifier = Modifier.padding(bottom = 15.dp)
                )
        }

        if ((Settings.printNeed && paperSize != null) || (!Settings.printNeed && Settings.frameNeed && !frameError))
            Column(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                val sample by remember { mutableStateOf(loadImageBitmap(File("sample.jpg"))) }
                val ratio: Pair<Float, Float> = getRatio(sample, paperSize)

                Text(
                    text = "Пример" +
                            if (paperSize != null && Settings.printNeed)
                                " (${ratio.first}mm x ${ratio.second}mm)"
                            else
                                "",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Box(
                    modifier = Modifier
                        .height(500.dp)
                        .aspectRatio(ratio.first / ratio.second)
                        .then(
                            if (sampleVisible)
                                Modifier.background(Color.Gray)
                            else
                                Modifier
                        )
                ) {
                    if (sampleVisible)
                        Image(
                            bitmap = sample,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                    if (Settings.frameNeed && !frameError)
                        Image(
                            bitmap = loadImageBitmap(File(Settings.photoFramePath)),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                }

                CheckboxWithText(
                    checked = sampleVisible,
                    onCheckedChange = { sampleVisible = it },
                    enabled = Settings.frameNeed && !frameError,
                    text = "Показать пример фото",
                    fontSize = textStyle.fontSize,
                    spaceBetween = 10.dp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
    }
}

fun getRatio(
    imageBitmap: ImageBitmap,
    paperSize: MediaSizeName?
): Pair<Float, Float> {
    return when {
        Settings.printNeed && paperSize != null -> {
            val size = MediaSize.getMediaSizeForName(paperSize).getSize(MediaSize.MM)
            if (imageBitmap.height > imageBitmap.width)
                size[0] to size[1]
            else
                size[1] to size[0]
        }

        !Settings.printNeed && Settings.frameNeed && Settings.photoFramePath.isNotEmpty() -> {
            val frame = loadImageBitmap(File(Settings.photoFramePath))
            val frameBigSide: Int
            val frameSmallSide: Int

            if (frame.width > frame.height) {
                frameBigSide = frame.width
                frameSmallSide = frame.height
            } else {
                frameBigSide = frame.height
                frameSmallSide = frame.width
            }

            if (imageBitmap.height > imageBitmap.width)
                frameSmallSide.toFloat() to frameBigSide.toFloat()
            else
                frameBigSide.toFloat() to frameSmallSide.toFloat()
        }

        else ->
            imageBitmap.width.toFloat() to imageBitmap.height.toFloat()
    }
}

class Printer(val printService: PrintService) : Spinnable {

    override fun toString(): String = printService.name

    fun getPaperSizeList(): List<PaperSize> {
        val list = mutableListOf<PaperSize>()
        (printService.getSupportedAttributeValues(Media::class.java, null, null) as Array<*>).forEach {
            if (it is MediaSizeName) {
                list.add(PaperSize(it))
            }
        }
        return list
    }
}

class PaperSize(val size: MediaSizeName) : Spinnable {
    override fun toString(): String = size.toString()
}