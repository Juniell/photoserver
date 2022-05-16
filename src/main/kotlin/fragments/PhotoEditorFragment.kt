package fragments

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import components.LazyGrid
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.KamelImage
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.lazyPainterResource
import org.burnoutcrew.reorderable.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.jetbrains.skia.Canvas
import java.io.File


const val BACKSTACK_MAX_INDEX = 30 - 1
val backStack = mutableListOf(emptyList<Layer>())
val filters = listOf(
    ColorFilter.tint(Color(0f, 0f, 0f), BlendMode.Color),
    ColorFilter.tint(Color(0f, 0f, 0f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.35546875f, 0f, 0f), BlendMode.Color),
    ColorFilter.tint(Color(0.171875f, 0f, 0f), BlendMode.Difference),
    ColorFilter.tint(Color(0.34375f, 0.4163424f, 0f), BlendMode.Hardlight),
    ColorFilter.tint(Color(0.34375f, 0.22957198f, 0f), BlendMode.Color),
    ColorFilter.tint(Color(1.0f, 0.77042794f, 0.49416342f), BlendMode.ColorBurn),
    ColorFilter.tint(Color(1.0f, 0.8832685f, 0.5953307f), BlendMode.ColorBurn),
    ColorFilter.tint(Color(1.0f, 0.7120622f, 0.16342413f), BlendMode.Darken),
    ColorFilter.tint(Color(0.02734375f, 0.031128407f, 0.031128407f), BlendMode.Difference),
    ColorFilter.tint(Color(0.375f, 0.3696498f, 0.36575872f), BlendMode.Difference),
    ColorFilter.tint(Color(0.375f, 0.3696498f, 0.36575872f), BlendMode.Hue),
    ColorFilter.tint(Color(0.51953125f, 0.3696498f, 0.36575872f), BlendMode.Hue),
    ColorFilter.tint(Color(0.51953125f, 0.7470817f, 0.36575872f), BlendMode.Hue),
    ColorFilter.tint(Color(0.37109375f, 0.3696498f, 0.36575872f), BlendMode.Hue),
    ColorFilter.tint(Color(0.76171875f, 0.3696498f, 0.36575872f), BlendMode.Hue),
    ColorFilter.tint(Color(0.6171875f, 0.22178988f, 0.000002f), BlendMode.Hue),
    ColorFilter.tint(Color(1.0f, 0.22957198f, 0.00000002f), BlendMode.Hue),
    ColorFilter.tint(Color(0.46875f, 0.47859922f, 0.14785992f), BlendMode.Hardlight),
    ColorFilter.tint(Color(0.5546875f, 0.5019455f, 0.20233463f), BlendMode.Hardlight),
    ColorFilter.tint(Color(0.2734375f, 0.16342413f, 0.14785992f), BlendMode.Lighten),
    ColorFilter.tint(Color(0.76171875f, 0.85214007f, 0.4241245f), BlendMode.Modulate),
    ColorFilter.tint(Color(0.7109375f, 0.59143966f, 0.41245136f), BlendMode.Overlay),
    ColorFilter.tint(Color(0.609375f, 0.48638132f, 0.41245136f), BlendMode.Overlay),
    ColorFilter.tint(Color(0.2578125f, 0.20622568f, 0.06614786f), BlendMode.Plus),
    ColorFilter.tint(Color(0.6171875f, 0.20622568f, 0.06614786f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.6171875f, 0.3035019f, 0.06614786f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.6171875f, 0.4241245f, 0.06614786f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.6171875f, 0.5097276f, 0.06614786f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.28125f, 0.36186767f, 0.17898831f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.5f, 0.4280156f, 0.12062257f), BlendMode.Softlight),
    ColorFilter.tint(Color(0.78125f, 0.78988326f, 1.0f), BlendMode.Saturation),
    ColorFilter.tint(Color(0.19921875f, 0.046692606f, 0.13618676f), BlendMode.Screen)
)

val kamelConfig = KamelConfig {
    takeFrom(KamelConfig.Default)
    imageBitmapCacheSize = 1000
}

@ExperimentalSplitPaneApi
@Composable
fun PhotoEditorFragment(photo: File, stickerPath: String) {
    val stickers = File(stickerPath).listFiles()?.filter { it.isFile } ?: listOf()
    val tools = Tools.values().toList()

    var currPosition = 0
    val currLayers = remember { mutableStateListOf<Layer>() }

    var selectedTools by remember { mutableStateOf(tools.first()) }
    val selectedColor = remember { mutableStateOf(Color.Blue) }
    var brushSize by remember { mutableStateOf(10f) }
    var undoEnabled by remember { mutableStateOf(currPosition > 0 && backStack.isNotEmpty()) }
    var redoEnabled by remember { mutableStateOf(currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty()) }
    val filter = remember { mutableStateOf<ColorFilter?>(null) }


    fun updateUndoRedoEnabled() {
        undoEnabled = currPosition > 0 && backStack.isNotEmpty()
        redoEnabled = currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty()
    }

    fun addCurrLayersToBackStack() {
        if (currPosition != backStack.lastIndex) {
            val firstInd = currPosition + 1

            for (i in firstInd..backStack.lastIndex)
                backStack.removeAt(i)
            currPosition = backStack.lastIndex
        }

        if (backStack.lastIndex == BACKSTACK_MAX_INDEX)
            backStack.removeAt(0)

        backStack.add(currLayers.toList())
        currPosition++

        println("backstack (size = ${backStack.size}): $backStack")
    }

    fun addToLayerList(layer: Layer) {
        currLayers.add(layer)
        println("currLayers (size = ${currLayers.size}): $currLayers")
        addCurrLayersToBackStack()
        updateUndoRedoEnabled()
    }

    fun undo() {
        if (currPosition != 0) {
            currPosition--
//            currLayers = backStack[currPosition].toMutableStateList()
            //todo: можно ли упростить??? как выше
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }

    fun redo() {
        if (currPosition != BACKSTACK_MAX_INDEX) {
            currPosition++
//            currLayers = backStack[currPosition].toMutableStateList()
            //todo: можно ли упростить??? как выше
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }


    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(
            initialPositionPercentage = 7f / 4f,
            moveEnabled = false
        )
    ) {
        first(700.dp) {

            VerticalSplitPane(
                splitPaneState = rememberSplitPaneState(
                    initialPositionPercentage = 1f / 7,
                    moveEnabled = false
                )
            ) {
                first(120.dp) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditorToolbar(PaddingValues(10.dp), tools, selectedTools) { newTools ->
                            selectedTools = newTools
                        }

                        Row {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = null,
                                tint = if (undoEnabled) Color.Black else Color.Gray,
                                modifier = Modifier.clickable(enabled = undoEnabled) {
                                    if (undoEnabled)
                                        undo()
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Icon(
                                imageVector = Icons.Filled.Redo,
                                contentDescription = null,
                                tint = if (redoEnabled) Color.Black else Color.Gray,
                                modifier = Modifier.clickable(enabled = redoEnabled) {
                                    if (redoEnabled)
                                        redo()
                                }
                            )
                        }
                    }
                }
                second(700.dp) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(color = Color.Gray)
                    ) {
                        Image(
                            painter = BitmapPainter(loadImageBitmap(photo)),
                            contentDescription = "Preview",
//                            contentScale = ContentScale.Crop,
                            colorFilter = filter.value,
                            modifier = Modifier/*.padding(7.dp)*//*.align(Alignment.CenterHorizontally)*/
                                .fillMaxHeight(/*1f / 1.3f*/)
                        )

                        Layers(
                            modifierBrush = Modifier.matchParentSize(),
                            layers = currLayers
                        )

                        BrushPoint(
                            modifier = Modifier.matchParentSize(),
                            enabled = selectedTools == Tools.BRUSH,
                            color = selectedColor,
                            brushSize = brushSize
                        ) { path ->
                            addToLayerList(BrushLayer(path, selectedColor.value, brushSize))
                        }
                    }
                }
            }
        }

        second(400.dp) {
            VerticalSplitPane(
                splitPaneState = rememberSplitPaneState(
                    initialPositionPercentage = 4f / 1,
                    moveEnabled = false
                )/*, modifier = Modifier.border(width = 1.dp, color = Color.Black)*/
            ) {
                first(500.dp) {
                    Tools(
                        selectedTools, selectedColor, photo, stickers,
                        onChooseFilter = { selectedFilter ->
                            filter.value = selectedFilter
                        },
                        onBrushSizeChange = { newSize ->
                            brushSize = newSize
                        })
                }
                second(200.dp) {
                    LayersList(currLayers) { first, second ->
                        currLayers.move(first.index, second.index)
                        addCurrLayersToBackStack()
                    }
                }
            }
        }
    }


}

@Composable
fun Layers(modifierBrush: Modifier = Modifier, layers: List<Layer>) {
//    Canvas(modifier = Modifier.fillMaxSize()) {
    for (layer in layers) {
        /*val im =*/ when (layer) {
            is BrushLayer -> BrushOld(modifier = modifierBrush, layer)
//                is BrushLayer -> canvasTest(layer.path, layer.color)
//                else -> ImageBitmap(100, 100)

        }

//            drawImage(im)
//        }
    }
}


@Composable
fun Tools(
    selectedTools: Tools,
    selectedColor: MutableState<Color>,
    photo: File,
    stickers: List<File>,
    onChooseFilter: (selectedFilter: ColorFilter) -> Unit,
    onBrushSizeChange: (newSize: Float) -> Unit
) {
    Box {
        when (selectedTools) {
            Tools.BRUSH -> {
                Column {
                    ColorPicker(selectedColor)
                    BrushSizePicker(onBrushSizeChange)
                }
            }

            Tools.STICKER -> StickerPicker(stickers)

            Tools.FIlTER -> FilterPicker(photo, onChooseFilter)

            else -> Text(text = "")
        }
    }
}

@Composable
fun ColorPicker(selectedColor: MutableState<Color>) {
    var customColor by remember { mutableStateOf(Color.Red) }
    var alpha by remember { mutableStateOf(1f) }

    val colors = listOf(
        Color.Black, Color.Gray, Color.White, Color.Blue, Color.Cyan, Color.Green,
        Color(0.15294118f, 0.48235294f, 0.003921569f), Color.Yellow,
        Color(1.0f, 0.94509804f, 0.54901963f), Color(1.0f, 0.5294118f, 0.0f),
        Color.Red, Color.Magenta, Color(0.67058825f, 0.16470589f, 1.0f), customColor
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyGrid(items = colors, rowSize = 7, modifier = Modifier./*width(310.dp).*/padding(10.dp)) {
            Box(modifier = Modifier
                .clickable { selectedColor.value = Color(it.red, it.green, it.blue, alpha) }
                .aspectRatio(1f)
                .padding(4.dp)
                .border(1.dp, Color.Black)
                .background(it)
            )
        }

        HarmonyColorPicker(
            harmonyMode = ColorHarmonyMode.NONE,
            /*modifier = Modifier.fillMaxWidth(85/100f),*/
            color = customColor,
            onColorChanged = {
                val newColor = it.toColor()
                customColor = Color(newColor.red, newColor.green, newColor.blue, alpha)
                selectedColor.value = customColor
            })

        Slider(
            value = alpha,
            valueRange = 0.2f..1f,
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 5.dp)/*.fillMaxWidth(85/100f)*/,
            onValueChange = {
                alpha = it
                customColor = Color(customColor.red, customColor.green, customColor.blue, alpha)
                selectedColor.value =
                    Color(selectedColor.value.red, selectedColor.value.green, selectedColor.value.blue, alpha)
            }
        )
    }
}

@Composable
fun BrushSizePicker(onSizeChange: (newSize: Float) -> Unit) {
    var size by remember { mutableStateOf(10f) }
    Column {
        Text(text = "Размер кисти")
        Slider(
            value = size,
            valueRange = 3f..30f,
            onValueChange = {
                size = it
                onSizeChange(it)
            }
        )
    }
}

@Composable
fun StickerPicker(stickers: List<File>) {
    if (stickers.isEmpty())
        Text("Стикеры не найдены.")
    else
        LazyGrid(items = stickers, rowSize = 3, modifier = Modifier.fillMaxWidth(2f).padding(10.dp)) {
            Image(
                painter = BitmapPainter(loadImageBitmap(it)),
                contentDescription = "Sticker",
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(300.dp).aspectRatio(1f).align(Alignment.Center).padding(7.dp)
                //todo: .width(50.dp) - разобраться с размером по-другому
            )
        }
}

@Composable
fun FilterPicker(photo: File, onChooseFilter: (selectedFilter: ColorFilter) -> Unit) {

    LazyGrid(items = filters, rowSize = 2, modifier = Modifier.padding(5.dp)) {
        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
            val image = lazyPainterResource(data = photo)

            KamelImage(
                resource = image,
                contentDescription = null,
                colorFilter = it,
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
//                    .align(Alignment.Center)
                    .clickable { onChooseFilter(it) }.aspectRatio(1f).padding(5.dp)
//                    .aspectRatio(1f).padding(7.dp).fillMaxSize()
            )
        }
    }
}

/*@Composable
fun FilterSettings(selectedFilter: MutableState<ColorFilter?>) {
    val modes = listOf(
        BlendMode.Color, BlendMode.ColorBurn, BlendMode.ColorDodge,
        BlendMode.Darken, BlendMode.Difference,
        BlendMode.Hue, BlendMode.Hardlight,
        BlendMode.Lighten, BlendMode.Luminosity,
        BlendMode.Modulate, BlendMode.Multiply,
        BlendMode.Overlay,
        BlendMode.Plus,
        BlendMode.Softlight, BlendMode.Saturation, BlendMode.Src, BlendMode.SrcIn,
        BlendMode.SrcAtop, BlendMode.Screen, BlendMode.SrcOut, BlendMode.SrcOver,
        BlendMode.Xor,
    )
    var r by remember { mutableStateOf(0f) }
    var g by remember { mutableStateOf(0f) }
    var b by remember { mutableStateOf(0f) }
    var color by remember { mutableStateOf(Color(r, g, b)) }


    Column {
        Row {
            Button(onClick = {
                selectedFilter.value = null
            }) {
                Text(text = "Оригинал")
            }

            Box(modifier = Modifier.background(color).size(50.dp, 50.dp)) {

            }
            Text(text = "r: $r, \ng: $g, \nb: $b")

            Button(onClick = {
                println("r: $r, \ng: $g, \nb: $b")
            }) {
                Text("Save")
            }
        }

        Slider(
            value = r,
            valueRange = 0f..1f,
            steps = 255,
            onValueChange = {
                r = it
                color = Color(r, g, b)
            },
        )
        Slider(
            value = g,
            valueRange = 0f..1f,
            steps = 256,
            onValueChange = {
                g = it
                color = Color(r, g, b)

            },
        )
        Slider(
            value = b,
            valueRange = 0f..1f,
            steps = 256,
            onValueChange = {
                b = it
                color = Color(r, g, b)

            },
        )

        LazyColumn {
            items(items = modes) { mode ->
                Button(onClick = {
                    selectedFilter.value = ColorFilter.tint(color, mode)
                    println("mode = $mode")
                }) {
                    Text(text = "$mode")
                }
            }
        }

        Button(onClick = {
            selectedFilter.value = ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }) {
            Text(text = "Gray")
        }
    }


    *//*Row {
//        Column {
//
//            Text(text = "Tint")
//
//            Button(onClick = {
//                selectedFilter.value = null
//            }) {
//                Text(text = "Оригинал")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Darken)
//            }) {
//                Text(text = "BlendMode.Darken")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Color)
//            }) {
//                Text(text = "BlendMode.Color")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.ColorBurn)
//            }) {
//                Text(text = "BlendMode.ColorBurn")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Difference)
//            }) {
//                Text(text = "BlendMode.Difference")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.ColorDodge)
//            }) {
//                Text(text = "BlendMode.ColorDodge")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Exclusion)
//            }) {
//                Text(text = "BlendMode.Exclusion")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Hardlight)
//            }) {
//                Text(text = "BlendMode.Hardlight")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Hue)
//            }) {
//                Text(text = "BlendMode.Hue")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Lighten)
//            }) {
//                Text(text = "BlendMode.Lighten")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Luminosity)
//            }) {
//                Text(text = "BlendMode.Luminosity")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Softlight)
//            }) {
//                Text(text = "BlendMode.Softlight")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Modulate)
//            }) {
//                Text(text = "BlendMode.Modulate")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Multiply)
//            }) {
//                Text(text = "BlendMode.Multiply")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Xor)
//            }) {
//                Text(text = "BlendMode.Xor")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Saturation)
//            }) {
//                Text(text = "BlendMode.Saturation")
//            }
//
//            Button(onClick = {
//                selectedFilter.value = ColorFilter.tint(Color.Red, BlendMode.Plus)
//            }) {
//                Text(text = "BlendMode.Plus")
//            }
//        }

        Column {
            Text(text = "Lighting")

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Red, Color.Yellow)
            }) {
                Text(text = "Red, Yellow")
            }

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Yellow, Color.Red)
            }) {
                Text(text = "Yellow, Red")
            }

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Blue, Color.Red)
            }) {
                Text(text = "Blue, Red")
            }

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Red, Color.Blue)
            }) {
                Text(text = "Red, Blue")
            }

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Yellow, Color.Blue)
            }) {
                Text(text = "Yellow, Blue")
            }

            Button(onClick = {
                selectedFilter.value = ColorFilter.lighting(Color.Blue, Color.Yellow)
            }) {
                Text(text = "Blue, Yellow")
            }








        }
    }*//*
}*/

@Composable
fun LayersList(layersList: List<Layer>, onLayersOrderChanged: (ItemPosition, ItemPosition) -> Unit) {
    //todo: добавить появление штучки для скролла
    val state = rememberReorderState()

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(
                state = state,
                onMove = onLayersOrderChanged
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        itemsIndexed(layersList) { idx, item ->
            Card(
                modifier = Modifier
                    .draggedItem(state.offsetByIndex(idx))
                    .detectReorder(state)
                    .fillParentMaxWidth()
            ) {
                Row {
                    if (item is BrushLayer)
                        Image(
                            bitmap = createColorCircle(item.color),
                            contentDescription = null
                        )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

fun createColorCircle(color: Color): ImageBitmap {
    val imageBitmap = ImageBitmap(30, 30)
    val canvasBitmap = Canvas(imageBitmap.asSkiaBitmap())

    val paint = org.jetbrains.skia.Paint()
    paint.color = color.toArgb()
    val paintBord = org.jetbrains.skia.Paint()
    paintBord.color = org.jetbrains.skia.Color.BLACK

//    paint.color = org.jetbrains.skia.Color.BLUE
    canvasBitmap.drawCircle(15f, 15f, 15f, paintBord)
    canvasBitmap.drawCircle(15f, 15f, 14f, paint)
    return imageBitmap
}

/*fun saveBrush(path: Path, color: Color): ImageBitmap {
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        val k = this.drawContext.canvas
//        k.
//    }

//    val im = loadImageBitmap(file)

//    val imageBitmap =  Bitmap.makeFromImage(im.asSkiaBitmap())
    val imageBitmap = ImageBitmap(500, 500)
    val canvasBitmap = Canvas(imageBitmap.asSkiaBitmap())

    val paint = org.jetbrains.skia.Paint()
    paint.color = org.jetbrains.skia.Color.BLUE
//    canvasBitmap.drawCircle(150f, 150f, 50f, paint)

//    val paint = org.jetbrains.skia.Paint()
//    paint.color = org.jetbrains.skia.Color.BLUE
    canvasBitmap.drawPath(path.asSkiaPath(), paint)
//    canvasBitmap.save()

//    val image: BufferedImage = scaleBitmapAspectRatio(result, 500, 500) //todo: задавать размер как-то по-другому?
//
//    val resSave = ImageIO.write(image, "JPG", imageFileMini.outputStream())

//    return imageBitmap.asComposeImageBitmap()
//    return im
    return imageBitmap
}*/


@Composable
fun BrushOld(modifier: Modifier = Modifier, layer:BrushLayer) {
    Canvas(modifier = Modifier.clipToBounds().then(modifier)) {
        drawPath(
            path = layer.path,
            color = layer.color,
            style = Stroke(layer.brushSize)
        )
    }
}

@Composable
fun BrushPoint(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    color: MutableState<Color>,
    brushSize: Float,
    onEndPaint: (path: Path) -> Unit
) {
    val points = remember { mutableStateListOf<Offset>() }
    var count = 2 // чтобы не обрабатывать каждый сдвиг

    val modifierBrush = if (enabled) {
        Modifier
//            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val path = Path()
                        path.apply {
                            points.forEachIndexed { i, point ->
                                if (i == 0) {
                                    moveTo(point.x, point.y)
                                } else {
                                    lineTo(point.x, point.y)
                                }
                            }
                        }
//                        layers.add(BrushLayer(path, color.value))
                        onEndPaint(path)
                        ////addToLayerList(BrushLayer(path, color), layers)
                        points.clear()
                    }
                ) { change, _ ->
                    if (count == 2) {
                        points.add(change.position)
                        count = 0
                    } else
                        count++
                }
            }
    } else
        Modifier/*.fillMaxSize()*/

    Canvas(modifier = modifierBrush.then(modifier)) {
        if (enabled) {
            drawPath(
                path = Path().apply {
                    points.forEachIndexed { i, point ->
                        if (i == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                },
                color = color.value,
                style = Stroke(width = brushSize)
            )
        }
    }
}


@Composable
fun EditorToolbar(
    padding: PaddingValues,
    tools: List<Tools>,
    selectedTools: Tools,
    onClick: (newTools: Tools) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(padding)) {
        for ((i, el) in tools.withIndex()) {
            if (i != 0)
                Spacer(modifier = Modifier.width(7.dp))

            EditorButton(
                icon = el.imageVector,
                text = el.text,
                onClick = { onClick(el) },
                selected = selectedTools == el,
                modifier = EditorButtonModifier(fontSize = 25.sp, buttonMinWidth = 70.dp, buttonMaxWidth = 100.dp)
            )

            if (i != tools.size - 1)
                Spacer(modifier = Modifier.width(7.dp))
        }
    }
}


@Composable
fun EditorButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: EditorButtonModifier = EditorButtonModifier(15.sp, 50.dp, 100.dp)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(
                width = if (selected) 3.dp else 1.dp,
                shape = RoundedCornerShape(0),
                color = Color.Black
            )
            .width(if (selected) modifier.buttonMaxWidth else modifier.buttonMinWidth)
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .wrapContentHeight(align = Alignment.CenterVertically)
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.fillMaxSize(3f / 5))

            if (selected)
                Text(text = text, fontSize = modifier.fontSize)
        }
    }
}

class EditorButtonModifier(
    val fontSize: TextUnit,
    val buttonMinWidth: Dp,
    val buttonMaxWidth: Dp
)

enum class Tools(val text: String, val imageVector: ImageVector) {
    BRUSH("Кисть", Icons.Outlined.Brush),
    TEXT("Текст", Icons.Outlined.Title),
    STICKER("Стикеры", Icons.Outlined.InsertPhoto),
    SMILE("Смайлики", Icons.Outlined.SentimentSatisfiedAlt),
    FIlTER("Фильтры", Icons.Outlined.PhotoFilter)
    //    ERASER("Ластик", ),
}

open class Layer(open val name: String)

class BrushLayer(val path: Path, val color: Color, val brushSize: Float, override val name: String = "Линия") :
    Layer(name)
