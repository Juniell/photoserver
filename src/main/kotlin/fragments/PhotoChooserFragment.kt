package fragments

import InfoSettings
import Spinnable
import Spinner
import SpinnerModifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.LazyGrid
import components.TimeRangePicker
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.KamelImage
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.lazyPainterResource
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import scaleBitmapAspectRatio
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

// todo: добавить выделение на выбранную картинку в Grid
// todo: убрать paddings в картинках в Grid и использовать то, что скидывал Игорь

@ExperimentalSplitPaneApi
@Composable
fun PhotoChooserFragment(
    settings: InfoSettings,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: (photo: File) -> Unit
) {
    val kamelConfig = KamelConfig {
        takeFrom(KamelConfig.Default)
        imageBitmapCacheSize = 1000
    }

    val path = settings.dirInput!!
    val pathMinis = path + File.separator + "minis"
    File(pathMinis).apply {
        if (!exists())
            mkdir()
    }
    val files = (File(path).listFiles()?.toList() ?: listOf())
        .filter { it.isFile && it.name.split(".").last() == "jpg" }
    var filteredFiles by remember { mutableStateOf(files.sortedBy { it.lastModified() }.reversed()) }
    val filesMinisMap = (File(pathMinis).listFiles()?.toList() ?: listOf()).associateBy { it.name }

    val selectedFilter = remember { mutableStateOf<Spinnable>(Filters.NEW_FIRST) }
    var chooserTimeRangeOn by remember { mutableStateOf(false) }
    val timeFirst = remember { mutableStateOf(0) }
    val timeSecond = remember { mutableStateOf(24) }

    MaterialTheme {
        if (files.isEmpty())
            Text(text = "Выбранная директория пуста", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
        else {
            var photoPreview by remember { mutableStateOf(filteredFiles.first()) }

            HorizontalSplitPane(
                splitPaneState = rememberSplitPaneState(
                    initialPositionPercentage = 4 / 2.2f,
                    moveEnabled = false
                )
            ) {
                first(700.dp) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        LazyGrid(
                            items = filteredFiles,
                            rowSize = 4,
                            modifier = Modifier.fillMaxSize().padding(7.dp)
                        ) {

                            val imageFile = if (filesMinisMap.containsKey(it.name))
                                filesMinisMap[it.name]!!
                            else
                                createMini(it, pathMinis) ?: it

                            CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
                                val image = lazyPainterResource(data = imageFile)

                                KamelImage(
                                    resource = image,
                                    contentDescription = "Image",
                                    onLoading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .align(Alignment.Center)
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clickable { photoPreview = it }
                                        .aspectRatio(1f).padding(7.dp).fillMaxSize()
                                )
                            }
                        }
                    }
                }

                second(500.dp) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spinner(
                                Filters.values().toList(),
                                selectedValue = selectedFilter,
                                onSelected = {
                                    filteredFiles = when (selectedFilter.value) {
                                        Filters.OLD_FIRST ->
                                            files.sortedBy { it.lastModified() }
                                        else ->
                                            files.sortedBy { it.lastModified() }.reversed()
                                    }.toMutableList()
                                },
                                SpinnerModifier(
                                    width = 300.dp,
                                    padding = PaddingValues(7.dp),
                                    height = 55.dp,
                                    textFontSize = 20.sp
                                )
                            )

                            Button(
                                modifier = Modifier.padding(end = 15.dp),
                                onClick = {
//                                if (chooserTimeRangeOn) {
                                    // todo: добавить фильтрацию по времени
                                    // todo: спрашивать в настройках, нужно ли указывать ещё и дату?
//                                }
                                    chooserTimeRangeOn = !chooserTimeRangeOn
                                }
                            ) {
                                if (chooserTimeRangeOn)
                                    Text("Подтвердить")
                                else
                                    Text("Выбрать время")
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp).fillMaxWidth())

                        if (chooserTimeRangeOn) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimeRangePicker(
                                    timeFirst = timeFirst,
                                    timeSecond = timeSecond,
                                    spacing = 0.dp,
                                    animationHeight = 50.dp,
                                    textStyle = TextStyle(fontSize = 30.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())
                        }

                        Image(
                            painter = BitmapPainter(loadImageBitmap(photoPreview)),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.padding(7.dp).align(Alignment.CenterHorizontally)
                                .fillMaxHeight(1f / 1.3f)
                        )

                        Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(7.dp).fillMaxWidth().fillMaxHeight()
                        ) {
                            Button(onClick = onBackButtonClick) {
                                Text("Назад", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                            }

                            Button(onClick = { onNextButtonClick(photoPreview) }) {
                                Text("Далее", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


fun createMini(imageFile: File, pathMinis: String): File? {
    val input: InputStream = imageFile.inputStream()
    val result: BufferedImage? = ImageIO.read(input)

    if (result != null) {
        val image: BufferedImage = scaleBitmapAspectRatio(result, 500, 500) //todo: задавать размер как-то по-другому?

        val imageFileMini = File(pathMinis + File.separator + imageFile.name)
        val resSave = ImageIO.write(image, "JPG", imageFileMini.outputStream())
        return if (resSave) imageFileMini else null
    }
    return null
}

enum class Filters(val text: String) : Spinnable {
    NEW_FIRST("Сначала новые") {
        override fun toString() = text
    },
    OLD_FIRST("Сначала старые") {
        override fun toString() = text
    };

    abstract override fun toString(): String
}

//                    AsyncImage(
//                        load = { loadImageBitmap(it) },
//                        painterFor = { remember { BitmapPainter(it) } },
//                        contentDescription = "Image",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.align(Alignment.Center).padding(7.dp).clickable { photoPreview = it }
//                    )

//@Composable
//fun <T> AsyncImage(
//    load: suspend () -> T,
//    painterFor: @Composable (T) -> Painter,
//    contentDescription: String,
//    modifier: Modifier = Modifier,
//    contentScale: ContentScale = ContentScale.Fit,
//) {
//    val image: T? by produceState<T?>(null) {
//        value = withContext(Dispatchers.IO) {
//            try {
//                load()
//            } catch (e: IOException) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }
//
//    if (image != null) {
//        Image(
//            painter = painterFor(image!!),
//            contentDescription = contentDescription,
//            contentScale = contentScale,
//            modifier = modifier
//        )
//    }
//}
//
fun loadImageBitmap(file: File): ImageBitmap =
    file.inputStream().buffered().use(::loadImageBitmap)

//fun loadSvgPainter(file: File, density: Density): Painter =
//    file.inputStream().buffered().use { loadSvgPainter(it, density) }
