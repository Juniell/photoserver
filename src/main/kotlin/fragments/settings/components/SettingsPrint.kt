package fragments.settings.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import components.Spinnable
import components.Spinner
import fragments.settings.*
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
        val frameFile = if (Settings.photoFramePath.value.isNotEmpty())
            File(Settings.photoFramePath.value)
        else
            null

        return if (Settings.frameNeed.value)
            !(frameFile != null && frameFile.exists() && frameFile.isFile && frameFile.extension == "png")
        else
            false
    }

    var frameError by remember(Settings.frameNeed.value, Settings.photoFramePath.value) {
        mutableStateOf(checkFrameError())
    }
    var paperSizeList by remember { mutableStateOf(printer?.getPaperSizeList()) }
    var sampleVisible by remember { mutableStateOf(true) }
    var copiesEnabled by remember { mutableStateOf(Settings.photoCopiesNum.value > 1) }
    var copiesNum by remember { mutableStateOf(Settings.photoCopiesNum.value.toFloat()) }


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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            ) {
                CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                    Checkbox(
                        checked = Settings.printNeed.value,
                        onCheckedChange = {
//                            sampleVisible = true
                            Settings.printNeed.value = it
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text(
                    text = "Разрешить печатать фотографии",
                    fontSize = textStyle.fontSize
                )
            }

            if (Settings.printNeed.value) {
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
                        Settings.printerName.value = element.printService.name
                        Settings.paperSize.value = ""
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
                        Settings.paperSize.value = it.toString()
                    },
                    padding = PaddingValues(bottom = 15.dp),
                    textFontSize = textStyle.fontSize
                )

                Text(
                    text = "Пользователь сможет распечатать максимум ${Settings.photoCopiesNum.value} фото",
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
                                Settings.photoCopiesNum.value = copiesNum.toInt()
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
                    onValueChangeFinished = {
                        Settings.photoCopiesNum.value = copiesNum.toInt()
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            ) {
                CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                    Checkbox(
                        checked = Settings.frameNeed.value,
                        onCheckedChange = {
                            sampleVisible = true
                            Settings.frameNeed.value = it
                            frameError = checkFrameError()

                            if (frameError)
                                Settings.photoFramePath.value = ""
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text(
                    text = "Клеить рамку на итоговое фото",
                    fontSize = textStyle.fontSize
                )
            }

            if (Settings.frameNeed.value)
                PathChooser(
                    mode = PathChooserMode.IMAGE,
                    title = "Рамка фотографии",
                    dialogTitle = "Выбор рамки фотографий",
                    file = File(Settings.photoFramePath.value),
                    isError = frameError,
                    textStyle = textStyle,
                    onDirChoose = {
                        Settings.photoFramePath.value = it
                        frameError = checkFrameError()

                        if (frameError)
                            Settings.photoFramePath.value = ""

                    },
                    modifier = Modifier.padding(bottom = 15.dp)
                )
        }

        if ((Settings.printNeed.value && paperSize != null) || (!Settings.printNeed.value && Settings.frameNeed.value && !frameError))
            Column(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                val sample by remember { mutableStateOf(loadImageBitmap(File("sample.jpg"))) }
                val ratio: Pair<Float, Float> = getRatio(sample, paperSize)

                Text(
                    text = "Пример" +
                            if (paperSize != null && Settings.printNeed.value)
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

                    if (Settings.frameNeed.value && !frameError)
                        Image(
                            bitmap = loadImageBitmap(File(Settings.photoFramePath.value)),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sampleVisible,
                        onCheckedChange = { sampleVisible = it },
                        enabled = Settings.frameNeed.value && !frameError
                    )
                    Text("Показать пример фото")
                }
            }
    }
}

fun getRatio(
    imageBitmap: ImageBitmap,
    paperSize: MediaSizeName?
): Pair<Float, Float> {
    return when {
        Settings.printNeed.value && paperSize != null -> {
            val size = MediaSize.getMediaSizeForName(paperSize).getSize(MediaSize.MM)
            size[0] to size[1]
        }

        !Settings.printNeed.value && Settings.frameNeed.value && Settings.photoFramePath.value.isNotEmpty() -> {
            val frame = loadImageBitmap(File(Settings.photoFramePath.value))
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