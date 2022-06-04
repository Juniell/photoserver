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
    var frameNeed by remember { mutableStateOf(photoFramePath != null) }

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
                            onPhotoCopiesChange(copiesNum.toInt())
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
                    text = "Пример (${size[0]}mm x ${size[1]}mm)",
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

class Printer(val printService: PrintService) : Spinnable {

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

class PaperSize(val size: MediaSizeName) : Spinnable {
    override fun toString(): String = size.toString()
}