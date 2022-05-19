package fragments.photoEditor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import fragments.photoEditor.components.EditorToolbar
import fragments.photoEditor.components.Tools
import loadImageBitmap
import org.burnoutcrew.reorderable.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.jetbrains.skia.Canvas
import java.io.File
import kotlin.math.roundToInt


private var backStack = BackStack()
private var colorFilter: ColorFilter? = null

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun PhotoEditorFragment(
    photo: File,
    stickerPath: String,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: (colorFilter: ColorFilter?, backStack: BackStack) -> Unit,
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
                                    onNextButtonClick(filter, backStack)
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
//                            .paint(BitmapPainter(loadImageBitmap(photo)), colorFilter = filter)
//                            .clipToBounds()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .paint(BitmapPainter(loadImageBitmap(photo)), colorFilter = filter)
                                .clipToBounds()
                        ) {
                            Layers(
                                modifierBrush = Modifier.matchParentSize(),
                                layers = backStack.currLayers()
                            )

                            BrushPoint(
                                modifier = Modifier.matchParentSize().zIndex((BACKSTACK_MAX_INDEX + 1).toFloat()),
                                enabled = selectedTools == Tools.BRUSH,
                                color = selectedColor,
                                brushSize = brushSize
                            ) { path ->
                                backStack.addToLayerList(BrushLayer(path, selectedColor, brushSize))
                            }
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
                )
            ) {
                first(500.dp) {
                    Tools(
                        modifier = Modifier.padding(5.dp, 0.dp),
                        selectedTools, photo, stickerPath,
                        onColorChange = { newColor ->
                            selectedColor = newColor
                        },
                        onChooseFilter = { selectedFilter ->
                            filter = selectedFilter
                        },
                        onBrushSizeChange = { newSize ->
                            brushSize = newSize
                        },
                        onStickerClick = { sticker ->
                            backStack.addToLayerList(
                                ImageLayer(
                                    sticker,
                                    mutableStateOf(1f),
                                    mutableStateOf(0f),
                                    mutableStateOf(Offset.Zero)
                                )
                            )
                        },
                        onCreatingTextComplete = { text, color, size, angle, font ->
                            backStack.addToLayerList(
                                TextLayer(
                                    text,
                                    color,
                                    mutableStateOf(size),
                                    font,
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

@Composable
fun SliderImageScale(startScale: Float, onScaleChange: (newScale: Float) -> Unit) {
    var scale by remember { mutableStateOf(startScale) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.wrapContentSize()
            .padding(5.dp)
    ) {
        Text("Размер")

        Slider(
            value = scale,
            valueRange = 0.3f..2f,
            onValueChange = {
                scale = it
                onScaleChange(scale)
            },
            modifier = Modifier.width(250.dp)
        )
    }
}

@Composable
fun SliderTextSize(startFontSize: Float, onTextSizeChange: (newTextSize: Float) -> Unit) {
    var fontSize by remember { mutableStateOf(startFontSize) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.wrapContentSize()
            .padding(5.dp)
    ) {
        Text("Размер")

        Slider(
            value = fontSize,
            valueRange = 10f..100f,
            onValueChange = {
                fontSize = it
                onTextSizeChange(fontSize)
            },
            modifier = Modifier.width(250.dp)
        )
    }
}


@Composable
fun PopupChangeAngle(
    startAngle: Float,
    onAngleChange: (newAngle: Float) -> Unit,
    onDismiss: () -> Unit,
    sliderChangeSize: @Composable () -> Unit
) {
    var angle by remember { mutableStateOf(startAngle) }

    Box {
        Popup(onDismissRequest = { onDismiss() }, focusable = true) {
            Card(
                elevation = 5.dp,
                backgroundColor = Color.White,
                shape = RoundedCornerShape(5.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.wrapContentSize()
                            .padding(5.dp)
                    ) {
                        Text("Поворот")

                        Slider(
                            value = angle,
                            valueRange = -180f..180f,
                            onValueChange = {
                                angle = it
                                onAngleChange(angle)
                            },
                            modifier = Modifier.width(250.dp)
                        )
                    }

                    sliderChangeSize()
                }
            }
        }
    }
}


@ExperimentalFoundationApi
@Composable
fun Layers(modifierBrush: Modifier = Modifier, layers: List<Layer>, editingEnabled: Boolean = true) {
//    val sortedLayer = layers.sortedBy { it.hashCode() }

    layers.forEachIndexed { index, layer ->
        var popupEnabled by remember { mutableStateOf(false) }

        when (layer) {
            is BrushLayer -> BrushOld(
                modifier = Modifier.zIndex(index.toFloat()).then(modifierBrush),
                layer
            )

            is TextLayer -> {
                if (popupEnabled && editingEnabled) {
                    PopupChangeAngle(
                        layer.angle.value,
                        onAngleChange = { newAngle ->
                            layer.angle.value = newAngle
                        },
                        onDismiss = { popupEnabled = false },
                        sliderChangeSize = {
                            SliderTextSize(layer.size.value, onTextSizeChange = { newTextSize ->
                                layer.size.value = newTextSize
                            })
                        }
                    )
                }

                Text(
                    text = layer.text,
                    color = layer.color,
                    fontSize = layer.size.value.sp,
                    fontFamily = layer.fontFamily,
                    modifier = Modifier
                        .zIndex(index.toFloat())
                        .graphicsLayer(rotationZ = layer.angle.value)
                        .offset {
                            IntOffset(layer.offset.value.x.roundToInt(), layer.offset.value.y.roundToInt())
                        }
                        .pointerInput(layer) {
                            detectDragGestures { change, dragAmount ->
                                if (editingEnabled) {
                                    change.consumeAllChanges()
                                    layer.offset.value += dragAmount
                                }
                            }
                        }.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = editingEnabled,
                            onClick = {
//                                text = "Click! ${count++}"
                            },
                            onLongClick = {
                                popupEnabled = true
                            }
                        )
                )
            }


            is ImageLayer -> {
//                var zoom by remember { mutableStateOf(1f) }
//                var angle by remember { mutableStateOf(0f) }

                if (popupEnabled && editingEnabled) {
                    PopupChangeAngle(
                        layer.angle.value,
                        onAngleChange = { newAngle ->
                            layer.angle.value = newAngle
                        },
                        onDismiss = { popupEnabled = false },
                        sliderChangeSize = {
                            SliderImageScale(layer.scale.value, onScaleChange = { newScale ->
                                layer.scale.value = newScale
                            })
                        }
                    )
                }


                Image(
                    painter = BitmapPainter(loadImageBitmap(layer.image)),
                    contentDescription = "Sticker",
                    modifier = Modifier
                        .zIndex(index.toFloat())
                        .offset {
                            IntOffset(layer.offset.value.x.roundToInt(), layer.offset.value.y.roundToInt())
                        }
                        .pointerInput(layer) {
                            detectDragGestures { change, dragAmount ->
                                if (editingEnabled) {
                                    change.consumeAllChanges()
                                    layer.offset.value += dragAmount
                                }
                            }
//                            detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->
//                                val oldScale = zoom
//                                val newScale = zoom * gestureZoom
//
//                                offset = (offset + centroid / oldScale).rotateBy(gestureRotate) -
//                                        (centroid / newScale + pan / oldScale)
//                                zoom = newScale
//                                angle += gestureRotate
//                            }
                        }
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = editingEnabled,
                            onClick = {
//                                text = "Click! ${count++}"
                            },
                            onLongClick = {
                                popupEnabled = true
                            }
                        )
                        .graphicsLayer {
                            rotationZ = layer.angle.value
                            scaleX = layer.scale.value
                            scaleY = layer.scale.value
                        }
//                        .graphicsLayer {
//                            translationX = -offset.x * zoom
//                            translationY = -offset.y * zoom
//                            scaleX = zoom
//                            scaleY = zoom
//                            rotationZ = angle
//                            transformOrigin = TransformOrigin(0f, 0f)
//                        }
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
    //todo: добавить скрооллбар
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
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
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
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            onDeleteLayer(idx)
                        }
                    )
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
                color = color,
                style = Stroke(width = brushSize)
            )
        }
    }
}

fun createColorCircle(color: Color): ImageBitmap {
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

//fun Offset.rotateBy(angle: Float): Offset {
//    val angleInRadians = angle * PI / 180
//    return Offset(
//        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
//        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
//    )
//}