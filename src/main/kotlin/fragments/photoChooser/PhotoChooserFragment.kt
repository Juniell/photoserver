package fragments.photoChooser

import Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.LazyGrid
import components.Spinnable
import components.Spinner
import components.TimeRangePicker
import createMini
import getImageFilesInDir
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.fileFetcher
import io.kamel.image.KamelImage
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.imageBitmapDecoder
import io.kamel.image.lazyPainterResource
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val DIR_MINIS = "minis"
val kamelConfig = KamelConfig {
    imageBitmapDecoder()
    fileFetcher()
    imageBitmapCacheSize = 1000
}

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun PhotoChooserFragment(
    onBackButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit
) {
    val path = Settings.dirInput
    val pathMinis = path + File.separator + DIR_MINIS

    File(pathMinis).apply {
        if (!exists())
            mkdir()
    }

    val photosMini = getImageFilesInDir(File(pathMinis))

    val files = getImageFilesInDir(File(path))
    var filteredFiles by remember { mutableStateOf(files.sortedBy { it.lastModified() }.reversed()) }
    val filesMinisMap = photosMini.toList().associateBy { it.name }

    var selectedFilter by remember { mutableStateOf<Spinnable>(Filters.NEW_FIRST) }
    var chooserTimeRangeOn by remember { mutableStateOf(false) }
    val timeFirst = remember { mutableStateOf(0) }
    val timeSecond = remember { mutableStateOf(23) }

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
                                if (!imageFile.exists())
                                    return@CompositionLocalProvider
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
                                label = { Text("Фильтрация") },
                                data = Filters.values().toList(),
                                value = selectedFilter,
                                onSelected = { selectedElement: Spinnable ->
                                    selectedFilter = selectedElement
                                    filteredFiles = when (selectedFilter) {
                                        Filters.OLD_FIRST ->
                                            filteredFiles.sortedBy { it.lastModified() }
                                        else ->
                                            filteredFiles.sortedBy { it.lastModified() }.reversed()
                                    }.toMutableList()
                                },
                                width = 300.dp,
                                padding = PaddingValues(7.dp),
                                textFontSize = 20.sp
                            )

                            Button(
                                modifier = Modifier.padding(end = 15.dp),
                                onClick = {
                                    if (chooserTimeRangeOn) {
                                        filteredFiles = files.filter {
                                            val hour = SimpleDateFormat("HH").format(Date(it.lastModified())).toInt()
                                            if (timeFirst.value <= timeSecond.value)
                                                (timeFirst.value..timeSecond.value).contains(hour)
                                            else
                                                ((timeFirst.value..23).contains(hour) ||
                                                        (0..timeSecond.value).contains(hour))
                                        }
                                    }
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
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp)
                            ) {
                                TimeRangePicker(
                                    timeFirst = timeFirst,
                                    timeSecond = timeSecond,
                                    spacing = 0.dp,
                                    animationHeight = 50.dp,
                                    textStyle = TextStyle(fontSize = 30.sp)
                                )

                                Button(
                                    onClick = {
                                        timeFirst.value = 0
                                        timeSecond.value = 23
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                                    modifier = Modifier.padding(start = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        tint = Color.DarkGray,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 3.dp)
                                    )
                                    Text("Сбросить")
                                }
                            }
                        }

                        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
                            if (!photoPreview.exists())
                                return@CompositionLocalProvider
                            val image = lazyPainterResource(data = photoPreview)

                            KamelImage(
                                resource = image,
                                contentDescription = "Preview",
                                onLoading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .align(Alignment.CenterHorizontally)
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                },
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(7.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxHeight(0.9f)

                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(7.dp).fillMaxWidth().fillMaxHeight()
                        ) {
                            Button(onClick = onBackButtonClick) {
                                Text("Назад", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                            }

                            Button(onClick = {
                                Settings.selectedPhoto = photoPreview
                                Settings.selectedPhotoMini =
                                    File(Settings.dirInput + File.separator + DIR_MINIS + File.separator + photoPreview.name)
                                onNextButtonClick()
                            }) {
                                Text("Далее", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
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


