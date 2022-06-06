package fragments.photoEditor

import Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import fragments.photoChooser.DIR_MINIS
import fragments.photoEditor.components.EditorToolbar
import fragments.photoEditor.components.SlidersSizeAngle
import fragments.photoEditor.components.TEXT_FONT_SIZE
import fragments.photoEditor.components.Tools
import fragments.settings.components.getRatio
import kotlinx.coroutines.launch
import loadImageBitmap
import org.burnoutcrew.reorderable.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.jetbrains.skia.Canvas
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


private var backStack = BackStack()
private var colorFilter: ColorFilter? = null

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
fun PhotoEditorFragment(
    onBackButtonClick: () -> Unit,
    onNextButtonClick: (resPhoto: File) -> Unit,
    renew: Boolean = true
) {
    if (!renew) {
        backStack = BackStack()
        colorFilter = null
    }

    var selectedTools by remember { mutableStateOf(Tools.BRUSH) }
    var selectedColor by remember { mutableStateOf(Color.Blue) }
    var brushSize by remember { mutableStateOf(10f) }
    var filter by remember { mutableStateOf(colorFilter) }
    var size: IntSize? = null
    val imageBitmap = loadImageBitmap(Settings.selectedPhoto)

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
                        EditorToolbar(PaddingValues(10.dp), selectedTools) { newTools ->
                            selectedTools = newTools
                        }

                        Row {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = null,
                                tint = if (backStack.undoEnabled) Color.Black else Color.Gray,
                                modifier = Modifier.clickable(enabled = backStack.undoEnabled) {
                                    backStack.undo()
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Icon(
                                imageVector = Icons.Filled.Redo,
                                contentDescription = null,
                                tint = if (backStack.redoEnabled) Color.Black else Color.Gray,
                                modifier = Modifier.clickable(enabled = backStack.redoEnabled) {
                                    backStack.redo()
                                }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Button(
                                onClick = {
                                    onBackButtonClick()
                                }
                            ) {
                                Text("Назад")
                            }

                            Button(
                                onClick = {
                                    colorFilter = filter
                                    val resPhotoPath =
                                        savePhoto(
                                            imageBitmap,
                                            size!!.width,
                                            size!!.height,
                                            filter,
                                        )
                                    onNextButtonClick(resPhotoPath)
                                }
                            ) {
                                Text("Готово")
                            }
                        }
                    }
                }
                second(700.dp) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(color = Color.Gray)
                    ) {
                        Editor(
                            image = imageBitmap,
                            colorFilter = filter,
                            selectedColor = selectedColor,
                            selectedTools = selectedTools,
                            brushSize = brushSize,
                            onSizeChange = { size = it },
                            onEndPaint = { path ->
                                backStack.addToLayerList(BrushLayer(path, selectedColor, brushSize))
                            }
                        )
                    }
                }
            }
        }

        second(400.dp) {
            VerticalSplitPane(
                splitPaneState = rememberSplitPaneState(
                    initialPositionPercentage = 4f / 1,
                    moveEnabled = false
                )
            ) {
                first(500.dp) {
                    Tools(
                        modifier = Modifier.padding(5.dp, 0.dp),
                        selectedTools = selectedTools,
                        selectedColor = selectedColor,
                        photoMini = File(Settings.selectedPhoto.parent + File.separator + DIR_MINIS + File.separator + Settings.selectedPhoto.name),
                        stickerPath = Settings.dirStickers.value,
                        onColorChange = { newColor: Color -> selectedColor = newColor },
                        onChooseFilter = { selectedFilter: ColorFilter? -> filter = selectedFilter },
                        onBrushSizeChange = { newSize: Float -> brushSize = newSize },
                        onStickerClick = { sticker: File ->
                            backStack.addToLayerList(
                                ImageLayer(
                                    sticker,
                                    mutableStateOf(1f),
                                    mutableStateOf(0f),
                                    mutableStateOf(Offset.Zero)
                                )
                            )
                        },
                        onCreatingTextComplete = { text: String, color: Color, font: FontFamily, scale: Float, angle: Float ->
                            backStack.addToLayerList(
                                TextLayer(
                                    text,
                                    color,
                                    font,
                                    mutableStateOf(scale),
                                    mutableStateOf(angle),
                                    mutableStateOf(Offset.Zero)
                                )
                            )
                        }
                    )
                }
                second(200.dp) {
                    LayersList(
                        backStack.currLayers(),
                        onLayersOrderChanged = { first, second ->
                            backStack.moveInLayerList(first.index, second.index)
                        },
                        onDeleteLayer = { layerIndex ->
                            backStack.deleteFromLayerList(layerIndex)
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun Editor(
    image: ImageBitmap,
    colorFilter: ColorFilter?,
    onSizeChange: (newSize: IntSize) -> Unit,
    brush: @Composable (modifier: Modifier) -> Unit = { }
) {
    val ratio = getRatio(image, Settings.paper)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clipToBounds()
            .aspectRatio(ratio.first / ratio.second)
            .paint(BitmapPainter(image), colorFilter = colorFilter, contentScale = ContentScale.Crop)
            .onSizeChanged {
                onSizeChange(it)
            }
            .then(
                if (image.width > image.height)
                    Modifier.fillMaxWidth() else Modifier.fillMaxHeight()
            )
    ) {

        Layers(
            modifierBrush = Modifier.matchParentSize(),
            layers = backStack.currLayers()
        )

        brush(Modifier.matchParentSize())
    }
}

//@ExperimentalFoundationApi
//@Composable
//fun EditorCheck(
//    stack: List<Layer>,
//    image: ImageBitmap,
//    paperSize: MediaSizeName,
//    colorFilter: ColorFilter?,
//    onSizeChange: (newSize: IntSize) -> Unit,
//    brush: @Composable (modifier: Modifier) -> Unit = { }
//) {
//    val size = MediaSize.getMediaSizeForName(paperSize).getSize(MediaSize.MM)
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .aspectRatio(if (image.height > image.width) size[0] / size[1] else size[1] / size[0])
//            .paint(BitmapPainter(image), colorFilter = colorFilter, contentScale = ContentScale.Crop)
//            .onSizeChanged {
//                onSizeChange(it)
//            }
//            .clipToBounds()
//    ) {
//
//        Layers(
//            modifierBrush = Modifier.matchParentSize(),
//            layers = stack
//        )
//
//        brush(Modifier.matchParentSize())
//    }
//}

@ExperimentalFoundationApi
@Composable
fun Editor(
    image: ImageBitmap,
    colorFilter: ColorFilter?,
    selectedColor: Color,
    selectedTools: Tools,
    brushSize: Float,
    onSizeChange: (newSize: IntSize) -> Unit,
    onEndPaint: (path: Path) -> Unit
) {
    Editor(
        image = image,
        colorFilter = colorFilter,
        onSizeChange = onSizeChange,
        brush = { modifier ->
            BrushPoint(
                modifier = Modifier.zIndex((BACKSTACK_MAX_INDEX + 1).toFloat()).then(modifier),
                enabled = selectedTools == Tools.BRUSH,
                color = selectedColor,
                brushSize = brushSize,
                onEndPaint = onEndPaint
            )
        }
    )
}

@ExperimentalFoundationApi
@Composable
fun Layers(modifierBrush: Modifier = Modifier, layers: List<Layer>, editingEnabled: Boolean = true) {

    layers.forEachIndexed { index, layer ->
        var popupEnabled by remember { mutableStateOf(false) }

        when (layer) {
            is BrushLayer -> BrushOld(
                modifier = Modifier.zIndex(index.toFloat()).then(modifierBrush),
                layer = layer
            )

            else -> {
                val modifier = Modifier
                    .zIndex(index.toFloat())
                    .offset {
                        IntOffset(
                            (layer.offset.value.x).roundToInt(),
                            (layer.offset.value.y).roundToInt()
                        )
                    }
                    .scale(layer.scale.value)
                    .rotate(layer.angle.value)
                    .pointerInput(layer) {
                        detectDragGestures(
                            onDragEnd = { backStack.saveLayerList() },
                            onDrag = { change, dragAmount ->
                                if (editingEnabled) {
                                    change.consumeAllChanges()
                                    layer.offset.value =
                                        ((layer.offset.value / layer.scale.value) + dragAmount.rotateBy(layer.angle.value)) * layer.scale.value
                                }
                            }
                        )
                    }
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = editingEnabled,
                        onClick = { },
                        onLongClick = { popupEnabled = true }
                    )


                if (popupEnabled && editingEnabled) {
                    PopupAngleSizeMenu(
                        modifier = Modifier.offset {
                            IntOffset(
                                layer.offset.value.x.roundToInt(),
                                layer.offset.value.y.roundToInt()
                            )
                        },
                        startAngle = layer.angle.value,
                        startScale = layer.scale.value,
                        onAngleChange = { layer.angle.value = it },
                        onScaleChange = { layer.scale.value = it },
                        onDismiss = { popupEnabled = false },
                        onChangeFinished = { backStack.saveLayerList() },
                    )
                }

                if (layer is ImageLayer) {
                    Image(
                        painter = BitmapPainter(loadImageBitmap(layer.image)),
                        contentDescription = "Sticker",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .then(modifier),
                    )
                }

                if (layer is TextLayer) {
                    Text(
                        text = layer.text,
                        color = layer.color,
                        fontSize = TEXT_FONT_SIZE.sp,
                        fontFamily = layer.fontFamily,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun PopupAngleSizeMenu(
    modifier: Modifier,
    startAngle: Float,
    startScale: Float,
    onAngleChange: (newAngle: Float) -> Unit,
    onScaleChange: (newScale: Float) -> Unit,
    onChangeFinished: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(modifier = modifier) {
        Popup(onDismissRequest = { onDismiss() }, focusable = true) {
            Card(
                elevation = 5.dp,
                backgroundColor = Color.White,
                shape = RoundedCornerShape(5.dp)
            ) {
                SlidersSizeAngle(
                    modifier = Modifier.width(300.dp),
                    startScale = startScale,
                    startAngle = startAngle,
                    onScaleChange = onScaleChange,
                    onAngleChange = onAngleChange,
                    onChangeFinished = onChangeFinished
                )
            }
        }
    }
}

@Composable
fun LayersList(
    layersList: SnapshotStateList<Layer>,
    onLayersOrderChanged: (ItemPosition, ItemPosition) -> Unit,
    onDeleteLayer: (layerIndex: Int) -> Unit
) {
    val state = rememberReorderState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = state.listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        state.listState.scrollBy(-delta)
                    }
                },
            )
            .reorderable(
                state = state,
                onMove = onLayersOrderChanged
            )
    ) {
        itemsIndexed(layersList) { idx, item ->
            Card(
                modifier = Modifier
                    .draggedItem(state.offsetByIndex(idx))
                    .fillParentMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        if (item is BrushLayer || item is TextLayer) {
                            val color = when (item) {
                                is BrushLayer -> item.color
                                is TextLayer -> item.color
                                else -> Color.White
                            }
                            Image(
                                bitmap = createColorCircle(color),
                                contentDescription = null
                            )
                        }

                        if (item is ImageLayer)
                            Image(
                                painter = BitmapPainter(loadImageBitmap(item.image)),
                                contentDescription = null,
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.width(30.dp).aspectRatio(1f)
                            )

                        val layerName = item.name + if (item is TextLayer) ": ${item.text}" else ""

                        Text(
                            text = layerName,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Row (horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            tint = Color.DarkGray,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                onDeleteLayer(idx)
                            }
                        )

                        Icon(
                            imageVector = Icons.Outlined.DragHandle,
                            tint = Color.DarkGray,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .detectReorder(state)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrushOld(modifier: Modifier = Modifier, layer: BrushLayer) {
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
    color: Color,
    brushSize: Float,
    onEndPaint: (path: Path) -> Unit
) {
    if (!enabled)
        return

    val points = remember { mutableStateListOf<Offset>() }
    var count = 2 // чтобы не обрабатывать каждый сдвиг

    Canvas(modifier = Modifier
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
                    onEndPaint(path)
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
        .then(modifier)
    ) {
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
            color = color,
            style = Stroke(width = brushSize)
        )
    }

}

private fun createColorCircle(color: Color): ImageBitmap {
    val imageBitmap = ImageBitmap(30, 30)
    val canvasBitmap = Canvas(imageBitmap.asSkiaBitmap())

    val paint = org.jetbrains.skia.Paint()
    paint.color = color.toArgb()
    val paintBorder = org.jetbrains.skia.Paint()
    paintBorder.color = org.jetbrains.skia.Color.BLACK

    canvasBitmap.drawCircle(15f, 15f, 15f, paintBorder)
    canvasBitmap.drawCircle(15f, 15f, 14f, paint)
    return imageBitmap
}

private fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

private fun generateId(): String {
    val time = System.currentTimeMillis() / 1000
    val base = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val b = 62L
    var r = (time % b)
    var res = base[r.toInt()].toString()
    var q = time / b

    while (q != 0L) {
        r = q % b
        q /= b
        res = base[r.toInt()] + res
    }
    return res
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
private fun savePhoto(
    imageBitmap: ImageBitmap,
    width: Int,
    height: Int,
    filter: ColorFilter?,
): File {
    val image = renderComposeScene(width, height) {
        Box {
            Editor(
                image = imageBitmap,
                colorFilter = filter,
                onSizeChange = {}
            ) {
                if (Settings.frameNeed.value)
                    Image(
                        bitmap = loadImageBitmap(File(Settings.photoFramePath.value)),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize().zIndex((BACKSTACK_MAX_INDEX + 1).toFloat())
                    )
            }
        }
    }

//    val newStack = resizeLayers(imageBitmap.width, imageBitmap.height, width, height)

//    val image = renderComposeScene(width, height) {
//        Editor(
//            image = imageBitmap,
//            filter,
//            onSizeChange = {}
//        )
//    }

    val path = Settings.dirOutput.value + File.separator + generateId() + ".jpg"
    val outputFile = File(path)

    outputFile.writeBytes(image.encodeToData()!!.bytes)
    return outputFile
}

//fun resizeLayers(widthOrigPhoto: Int, heightOrigPhoto: Int, widthNew: Int, heightNew: Int): List<Layer> {
//    println("widthOrigPhoto = $widthOrigPhoto, heightOrigPhoto = $heightOrigPhoto")
//    println("widthNew = $widthNew, heightNew = $heightNew")
//    val scale = widthOrigPhoto / widthNew
//
//    val resultLayers = mutableListOf<Layer>()
//
//    for (layer in backStack.currLayers()) {
//        resultLayers.add(
//            when (layer) {
//                // todo: вопрос с path
//                is BrushLayer -> BrushLayer(
//                    layer.path,
//                    layer.color,
//                    layer.brushSize * scale,
//                    layer.name
//                )
//
//                is ImageLayer -> ImageLayer(
//                    image = layer.image,
//                    scale = mutableStateOf(layer.scale.value * scale * scale),
//                    angle = layer.angle,
//                    offset = mutableStateOf(
//                        Offset(
//                            (layer.offset.value.x / widthNew) * widthOrigPhoto,
//                            (layer.offset.value.y / heightNew) * heightOrigPhoto
//                        )
//                    ),
//                    name = layer.name
//                )
//
//                is TextLayer -> TextLayer(
//                    text = layer.text,
//                    color = layer.color,
//                    fontFamily = layer.fontFamily,
//                    scale = mutableStateOf(layer.scale.value * scale * scale),
//                    angle = layer.angle,
//                    offset = mutableStateOf(
//                        Offset(
//                            (layer.offset.value.x / widthNew) * widthOrigPhoto,
//                            (layer.offset.value.y / heightNew) * heightOrigPhoto
//                        )
//                    ),
//                    name = layer.name
//                )
//
//                else -> throw IllegalArgumentException("WHAAAT")
//            }
//        )
//    }
//    return resultLayers
//}

