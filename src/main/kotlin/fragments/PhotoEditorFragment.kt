package fragments

import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import components.LazyGrid
import org.burnoutcrew.reorderable.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.io.File
import org.jetbrains.skia.Canvas


const val BACKSTACK_MAX_INDEX = 30 - 1
val backStack =  mutableListOf(emptyList<Layer>())

@ExperimentalSplitPaneApi
@Composable
fun PhotoEditorFragment(photo: File, stickerPath: String) {
    val stickers = File(stickerPath).listFiles()?.filter { it.isFile } ?: listOf()
    val tools = Tools.values().toList()

    var currPosition = 0
    val currLayers = remember { mutableStateListOf<Layer>() }

    var selectedTools by remember { mutableStateOf(tools.first()) }
    val selectedColor = remember { mutableStateOf(Color.Blue) }
    var undoEnabled by remember { mutableStateOf(currPosition > 0 && backStack.isNotEmpty()) }
    var redoEnabled by remember { mutableStateOf(currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty()) }

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
                            contentScale = ContentScale.Crop,
                            modifier = Modifier/*.padding(7.dp)*//*.align(Alignment.CenterHorizontally)*/
                                .fillMaxHeight(/*1f / 1.3f*/)
                        )

                        Layers(currLayers)

                        BrushPoint(selectedTools == Tools.BRUSH, selectedColor) { path ->
                            addToLayerList(BrushLayer(path, selectedColor.value))
                        }
                    }
                }
            }
        }

        second(400.dp) {
//            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
//                Tools(selectedTools, selectedColor, stickers)
//
//                LayersList(layers) { first, second ->
//                    layers.move(first.index, second.index)
//                }
//            }


            VerticalSplitPane(
                splitPaneState = rememberSplitPaneState(
                    initialPositionPercentage = 3f / 1,
                    moveEnabled = false
                )/*, modifier = Modifier.border(width = 1.dp, color = Color.Black)*/
            ) {
                first(500.dp) {
                    Tools(selectedTools, selectedColor, stickers)
                }
                second(300.dp) {
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
fun Layers(layers: List<Layer>) {
//    Canvas(modifier = Modifier.fillMaxSize()) {
    for (layer in layers) {
        /*val im =*/ when (layer) {
            is BrushLayer -> BrushOld(layer.path, layer.color)
//                is BrushLayer -> canvasTest(layer.path, layer.color)
//                else -> ImageBitmap(100, 100)

        }

//            drawImage(im)
//        }
    }
}


@Composable
fun Tools(selectedTools: Tools, selectedColor: MutableState<Color>, stickers: List<File>) {
    Box {
        when (selectedTools) {
            Tools.BRUSH -> ColorPicker(selectedColor)

            Tools.STICKER -> StickerPicker(stickers)

            else -> Text(text = "")
        }
    }
}

@Composable
fun ColorPicker(selectedColor: MutableState<Color>) {
    val colors = listOf(
        Color.Blue, Color.Gray, Color.Green, Color.Cyan, Color.Black, Color.White, Color.Magenta,
        Color.Red, Color.Yellow
    )
    LazyGrid(items = colors, rowSize = 5, padding = PaddingValues(10.dp), modifier = Modifier.width(310.dp)) {
        Box(
            modifier = Modifier.clickable { selectedColor.value = it }
                .width(60.dp).aspectRatio(1f).padding(4.dp).border(1.dp, Color.Black).background(it)
        )
    }
}

@Composable
fun StickerPicker(stickers: List<File>) {
    if (stickers.isEmpty())
        Text("Стикеры не найдены.")
    else
        LazyGrid(items = stickers, rowSize = 3, padding = PaddingValues(10.dp), modifier = Modifier.fillMaxWidth(2f)) {
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
fun BrushOld(path: Path, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawPath(
            path = path,
            color = color,
            style = Stroke(10f)
        )
    }
}

@Composable
fun BrushPoint(enabled: Boolean, color: MutableState<Color>, onEndPaint: (path: Path) -> Unit) {
    val points = remember { mutableStateListOf<Offset>() }
    var count = 2 // чтобы не обрабатывать каждый сдвиг

    val modifier = if (enabled) {
        Modifier
            .fillMaxSize()
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
        Modifier.fillMaxSize()

    Canvas(modifier = modifier) {
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
                style = Stroke(10f)
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

class BrushLayer(val path: Path, val color: Color, override val name: String = "Линия") : Layer(name)
