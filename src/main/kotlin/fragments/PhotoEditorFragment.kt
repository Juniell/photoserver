package fragments

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.io.File

@ExperimentalSplitPaneApi
@Composable
fun PhotoEditorFragment(photo: File, stickerPath: String) {
    val stickers = File(stickerPath).listFiles()?.filter { it.isFile } ?: listOf()
    val tools = Tools.values().toList()
    val toolsWithState = tools.associateWith { mutableStateOf(false) }.toMutableMap()
//    val selectedTools by remember { mutableStateOf(null) }
    val selectedColor = remember { mutableStateOf(Color.Blue) }

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

                    Column(modifier = Modifier.fillMaxSize()) {
                        EditorToolbar(PaddingValues(10.dp), toolsWithState)
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

                        BrushPoint(toolsWithState[Tools.BRUSH]!!, selectedColor)
                    }
                }
            }
        }

        second(400.dp) {
            Box(/*modifier = Modifier.fillMaxSize().background(Color.Cyan)*/ ) {

                if (toolsWithState[Tools.BRUSH]!!.value)
                    ColorPicker(selectedColor)

                if (toolsWithState[Tools.STICKER]!!.value)
                    StickerPicker(stickers)

                Text(text = "")
            }
        }
    }
}

@Composable
fun Tools() {

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

//class Layer(
//    val name: String
//)
//
//@Composable
//fun LayersList(layersList: List<Layer>, onLayersOrderChanged: (ItemPosition, ItemPosition) -> Unit) {
//    val state = rememberReorderState()
//
//    LazyColumn(
//        state = state.listState,
//        modifier = Modifier
//            .reorderable(
//                state = state,
//                onMove = onLayersOrderChanged
//            ),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = PaddingValues(8.dp),
//    ) {
//        itemsIndexed(layersList) { idx, item ->
//            Card(
//                modifier = Modifier
//                    .draggedItem(state.offsetByIndex(idx))
//                    .detectReorder(state)
//                    .fillParentMaxWidth()
//            ) {
//                Text(
//                    text = item.name,
//                    style = MaterialTheme.typography.h6,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                )
//            }
//        }
//    }
//}


@Composable
fun BrushNew(/*points: SnapshotStateList<Offset>*/) {
//    val pointsList = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
//    val points = remember { mutableStateListOf<Offset>() }
    val points = remember { mutableStateListOf<Offset>() }
    val pathList = remember { mutableStateListOf(Path()) }
//    val close = remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = {
            }, onDragEnd = {
                pathList.add(Path().apply {
                    points.forEach {
                        if (this.isEmpty)
                            moveTo(it.x, it.y)
                        else
                            lineTo(it.x, it.y)
                    }
                })
                points.clear()
            }) { change, _ ->
                points.add(change.position)
            }
        }) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = Path().apply {
                    points.forEach {
                        if (this.isEmpty)
                            moveTo(it.x, it.y)
                        else
                            lineTo(it.x, it.y)
                    }
                },
                color = Color.Blue,
                style = Stroke(10f)
            )
        }

        pathList.forEach {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPath(
                    path = it,
                    color = Color.Blue,
                    style = Stroke(10f)
                )
            }
        }
    }
}


@Composable
fun BrushPoint(enabled: MutableState<Boolean>, color: MutableState<Color>) {
    val points = remember { mutableStateListOf<Offset>() }
    val pathList = remember { mutableMapOf<Path, Color>() }
    var count = 2

    Canvas(modifier = if (enabled.value) {
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
//                    if (enabled.value) {
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

                    pathList[path] = color.value
                    points.clear()
//                    }
                }) { change, _ ->
//                    if (enabled.value) {
                    if (count == 2) {
                        points.add(change.position)
                        count = 0
                    } else
                        count++
//                    }
                }
            }
    } else {
        Modifier.fillMaxSize()
    }
    ) {
        for ((path, colorPath) in pathList) {
            drawPath(
                path = path,
                color = colorPath,
                style = Stroke(10f)
            )
        }

        if (enabled.value) {
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
fun Brush(/*points: SnapshotStateList<Offset>*/) {
//    val pointsList = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
    val points = remember { mutableStateListOf<Offset>() }
    val pathList = remember { mutableStateListOf(mutableStateOf(Path())) }
//    val close = remember { mutableStateOf(false) }
    var count = 1

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = {

            }, onDragEnd = {
                pathList.add(mutableStateOf(Path()))
//                points.clear()
            }) { change, _ ->
                if (count == 1) {
                    val pathUpdated = pathList.last()
                    pathUpdated.value.apply {
                        if (this.isEmpty)
                            moveTo(change.position.x, change.position.y)
                        else
                            lineTo(change.position.x, change.position.y)
                    }
                    pathList.last().value = pathUpdated.value
                    count--
                } else
                    count++


                /*     points.add(change.position)*/
            }
        }
    ) {
        pathList.forEach {

            drawPath(
                path = it.value/*Path().apply {
                    it.forEachIndexed { i, point ->
                        if (i == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }*//*
                }*/,
                color = Color.Blue,
                style = Stroke(10f)
            )
        }

    }
}

@Composable
fun EditorToolbar(padding: PaddingValues, toolsWithState: Map<Tools, MutableState<Boolean>>) {
    toolsWithState[toolsWithState.keys.first()]!!.value = true

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(padding)) {
        var i = 0
        for (el in toolsWithState) {
            if (i != 0)
                Spacer(modifier = Modifier.width(7.dp))

            EditorButton(
                icon = el.key.imageVector,
                text = el.key.text,
                onClick = {
                    for (state in toolsWithState.values)
                        state.value = false

                    el.value.value = !el.value.value
                },
                selected = el.value,
                modifier = EditorButtonModifier(fontSize = 25.sp, buttonMinWidth = 70.dp, buttonMaxWidth = 100.dp)
            )

            if (i != toolsWithState.size - 1)
                Spacer(modifier = Modifier.width(7.dp))

            i++
        }
    }
}

@Composable
fun SmileyFaceCanvas(
    modifier: Modifier
) {
    Canvas(
        modifier = modifier.size(300.dp),
        onDraw = {
//            drawPath()


            // Head
//            drawCircle(
//                Brush.linearGradient(
//                    colors = listOf(greenLight700, green700)
//                ),
//                radius = size.width / 2,
//                center = center,
//                style = Stroke(width = size.width * 0.075f)
//            )
//
//            // Smile
//            val smilePadding = size.width * 0.15f
//            drawArc(
//                color = red700,
//                startAngle = 0f,
//                sweepAngle = 180f,
//                useCenter = true,
//                topLeft = Offset(smilePadding, smilePadding),
//                size = Size(size.width - (smilePadding * 2f), size.height - (smilePadding * 2f))
//            )
//
//            // Left eye
//            drawRect(
//                color = dark,
//                topLeft = Offset(size.width * 0.25f, size.height / 4),
//                size = Size(smilePadding, smilePadding)
//            )
//
//            // Right eye
//            drawRect(
//                color = dark,
//                topLeft = Offset((size.width * 0.75f) - smilePadding, size.height / 4),
//                size = Size(smilePadding, smilePadding)
//            )
        }
    )
}

@Composable
fun EditorButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    selected: MutableState<Boolean>,
    modifier: EditorButtonModifier = EditorButtonModifier(15.sp, 50.dp, 100.dp)
) {
//    var width = remember { mutableStateOf(modifier.buttonMinWidth) }
    Box(
//        onClick = onClick,
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(
                width = if (selected.value)
                    3.dp
                else
                    1.dp,
                shape = RoundedCornerShape(0),
                color = Color.Black
            )
            .width(if (selected.value) modifier.buttonMaxWidth else modifier.buttonMinWidth)
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

            if (selected.value)
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