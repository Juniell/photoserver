package fragments.photoEditor.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import components.LazyGrid
import fragments.photoEditor.SliderWithName
import fragments.photoEditor.SlidersSizeAngle
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.KamelImage
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.launch
import loadImageBitmap
import java.io.File

const val TEXT_FONT_SIZE = 70f
const val SYMBOLS_LIMIT = 20

private val kamelConfig = KamelConfig {
    takeFrom(KamelConfig.Default)
    imageBitmapCacheSize = 1000
}

private val filters = listOf(
    null,                                                                                                  // 0
    ColorFilter.tint(Color(0f, 0f, 0f), BlendMode.Color),                                   // 1
    ColorFilter.tint(Color(0f, 0f, 0f), BlendMode.Softlight),                               // 2
    ColorFilter.tint(Color(0.35546875f, 0f, 0f), BlendMode.Color),                          // 3
    ColorFilter.tint(Color(0.171875f, 0f, 0f), BlendMode.Difference),                       // 4
    ColorFilter.tint(Color(0.34375f, 0.4163424f, 0f), BlendMode.Hardlight),                 // 5
    ColorFilter.tint(Color(0.34375f, 0.22957198f, 0f), BlendMode.Color),                    // 6
    ColorFilter.tint(Color(1.0f, 0.77042794f, 0.49416342f), BlendMode.ColorBurn),           // 7
    ColorFilter.tint(Color(1.0f, 0.8832685f, 0.5953307f), BlendMode.ColorBurn),             // 8
    ColorFilter.tint(Color(1.0f, 0.7120622f, 0.16342413f), BlendMode.Darken),               // 9
    ColorFilter.tint(Color(0.02734375f, 0.031128407f, 0.031128407f), BlendMode.Difference), // 10
    ColorFilter.tint(Color(0.5546875f, 0.5019455f, 0.20233463f), BlendMode.Hardlight),      // 11
    ColorFilter.tint(Color(0.2734375f, 0.16342413f, 0.14785992f), BlendMode.Lighten),       // 12
    ColorFilter.tint(Color(0.76171875f, 0.85214007f, 0.4241245f), BlendMode.Modulate),      // 13
    ColorFilter.tint(Color(0.7109375f, 0.59143966f, 0.41245136f), BlendMode.Overlay),       // 14
    ColorFilter.tint(Color(0.609375f, 0.48638132f, 0.41245136f), BlendMode.Overlay),        // 15
    ColorFilter.tint(Color(0.2578125f, 0.20622568f, 0.06614786f), BlendMode.Plus),          // 16
    ColorFilter.tint(Color(0.6171875f, 0.20622568f, 0.06614786f), BlendMode.Softlight),     // 17
    ColorFilter.tint(Color(0.28125f, 0.36186767f, 0.17898831f), BlendMode.Softlight),       // 18
    ColorFilter.tint(Color(0.78125f, 0.78988326f, 1.0f), BlendMode.Saturation),             // 19
    ColorFilter.tint(Color(0.19921875f, 0.046692606f, 0.13618676f), BlendMode.Screen),      // 20
    ColorFilter.tint(Color(0.375f, 0.3696498f, 0.36575872f), BlendMode.Hue),                // 21
    ColorFilter.tint(Color(0.51953125f, 0.3696498f, 0.36575872f), BlendMode.Hue),           // 22
    ColorFilter.tint(Color(0.51953125f, 0.7470817f, 0.36575872f), BlendMode.Hue),           // 23
    ColorFilter.tint(Color(0.37109375f, 0.3696498f, 0.36575872f), BlendMode.Hue)            // 24
)

private val fonts = listOf(
    FontFamily(Font(resource = "font/caveat_medium.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/comfortaa_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/cormorant_garamond_italic.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/eb_garamond_semi_bold_italic.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/lobster_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/marck_script_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/pacifico_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/pangolin_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/press_start_2p_regular.ttf", style = FontStyle.Normal)),
    FontFamily(Font(resource = "font/rubik_glitch_regular.ttf", style = FontStyle.Normal)),
)

@Composable
fun Tools(
    modifier: Modifier,
    selectedTools: Tools,
    selectedColor: Color,
    photoMini: File,
    stickerPath: String,
    onColorChange: (newColor: Color) -> Unit,
    onChooseFilter: (selectedFilter: ColorFilter?) -> Unit,
    onBrushSizeChange: (newSize: Float) -> Unit,
    onStickerClick: (sticker: File) -> Unit,
    onCreatingTextComplete: (text: String, color: Color, font: FontFamily, scale: Float, angle: Float) -> Unit
) {
    val stickers = File(stickerPath).listFiles()?.filter { it.isFile } ?: listOf()
    var color by remember { mutableStateOf(selectedColor) }
    var customColor by remember { mutableStateOf(Color.Magenta) }

    Box(modifier = modifier) {
        when (selectedTools) {
            Tools.BRUSH -> {
                Column {
                    ColorPicker(
                        color = color,
                        colorCustom = customColor,
                        onColorChange = {
                            color = it
                            onColorChange(it)
                        },
                        onCustomColorChange = {
                            customColor = it
                        }
                    )
                    BrushSizePicker(onBrushSizeChange)
                }
            }

            Tools.STICKER -> StickerPicker(stickers, onStickerClick)

            Tools.FIlTER -> FilterPicker(photoMini, onChooseFilter)

            Tools.TEXT -> TextCreating(
                color = color,
                colorCustom = customColor,
                onCreatingTextComplete = onCreatingTextComplete,
                onColorChange = {
                    color = it
                    onColorChange(it)
                },
                onCustomColorChange = { customColor = it }
            )

            else -> Text(text = "")
        }
    }
}

@Composable
private fun ColorPicker(
    color: Color,
    colorCustom: Color,
    onColorChange: (newColor: Color) -> Unit,
    onCustomColorChange: (newColor: Color) -> Unit,
    alphaEnabled: Boolean = true
) {
    var selectedColor by remember { mutableStateOf(color) }
    var customColor by remember { mutableStateOf(colorCustom) }
    var alpha by remember { mutableStateOf(1f) }
    val colors = listOf(Color.Black, Color.White, Color.Blue, Color.Green, Color.Yellow, Color.Red, customColor)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyGrid(items = colors, rowSize = 7, modifier = Modifier./*width(310.dp).*/padding(10.dp)) {
            Box(modifier = Modifier
//                .clip(CircleShape)
                .aspectRatio(1f)
                .padding(4.dp)
                .border(
                    width = if (selectedColor == it) 3.dp else 1.dp,
                    color = Color.Black
                )
//                .border(width = 0.dp, shape = CircleShape, color = Color.Black)
//                .background(it, shape = CircleShape)
                .background(it)
                .clickable {
                    selectedColor = Color(it.red, it.green, it.blue, alpha)
                    onColorChange(selectedColor)
                }
            )
        }

        HarmonyColorPicker(
            modifier = Modifier.height(250.dp), //todo
            harmonyMode = ColorHarmonyMode.NONE,
            /*modifier = Modifier.fillMaxWidth(85/100f),*/
            color = customColor,
            onColorChanged = {
                val newColor = it.toColor()
                customColor = Color(newColor.red, newColor.green, newColor.blue, alpha)
                selectedColor = customColor
                onColorChange(selectedColor)
                onCustomColorChange(selectedColor)
            })

        if (alphaEnabled)
            Slider(
                value = alpha,
                valueRange = 0.2f..1f,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 5.dp)/*.fillMaxWidth(85/100f)*/,
                onValueChange = {
                    alpha = it
                    customColor = Color(customColor.red, customColor.green, customColor.blue, alpha)
                    selectedColor = Color(selectedColor.red, selectedColor.green, selectedColor.blue, alpha)
                    onColorChange(selectedColor)
                }
            )
    }
}

@Composable
private fun BrushSizePicker(onSizeChange: (newSize: Float) -> Unit) {
//    var size by remember { mutableStateOf(10f) }

    SliderWithName(
        name = "Размер кисти",
        value = 10f,
        valueRange = 3f..30f,
        onValueChange = {
//            size = it
            onSizeChange(it)
        }
    )
}

@Composable
private fun StickerPicker(stickers: List<File>, onStickerClick: (sticker: File) -> Unit) {
    if (stickers.isEmpty())
        Text("Стикеры не найдены.")
    else
        LazyGrid(items = stickers, rowSize = 3, modifier = Modifier.fillMaxWidth(2f).padding(10.dp)) {
            Image(
                painter = BitmapPainter(loadImageBitmap(it)),
                contentDescription = "Sticker",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(300.dp)                 //todo:  разобраться с размером по-другому
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .padding(7.dp)
                    .clickable { onStickerClick(it) }
            )
        }
}

@Composable
private fun FilterPicker(photo: File, onChooseFilter: (selectedFilter: ColorFilter?) -> Unit) {
    LazyGrid(items = filters, rowSize = 2, modifier = Modifier.padding(5.dp)) {
        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
            KamelImage(
                resource = lazyPainterResource(data = photo),
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
                    .padding(5.dp).clickable { onChooseFilter(it) }.aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun TextCreating(
    color: Color,
    colorCustom: Color,
    onColorChange: (newColor: Color) -> Unit,
    onCustomColorChange: (newColor: Color) -> Unit,
    onCreatingTextComplete: (text: String, color: Color, font: FontFamily, scale: Float, angle: Float) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(color) }
    var textAngle by remember { mutableStateOf(0f) }
    var textFont by remember { mutableStateOf<FontFamily>(FontFamily.Default) }
    var textScale by remember { mutableStateOf(1f) }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxHeight(),
        snackbarHost = {
            SnackbarHost(it) {
                Snackbar(modifier = Modifier.wrapContentSize()) {
                    Text(text = it.message)
                }
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ColorPicker(
                    alphaEnabled = false,
                    color = textColor,
                    colorCustom = colorCustom,
                    onColorChange = { newColor ->
                        textColor = newColor
                        onColorChange(newColor)
                    },
                    onCustomColorChange = onCustomColorChange
                )

                OutlinedTextField(
                    value = text,
                    maxLines = 1,
                    singleLine = true,
                    onValueChange = {
                        if (it.length <= SYMBOLS_LIMIT)
                            text = it
                    },
                    label = { Text("Текст") },
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp, 0.dp, 0.dp),
                )

                Text(
                    text = "${text.length}/$SYMBOLS_LIMIT",
                    textAlign = TextAlign.End,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp)
                )

                TextFontPicker(onFontClick = {
                    textFont = it
                })

                SlidersSizeAngle(
                    startScale = textScale,
                    startAngle = textAngle,
                    onScaleChange = { textScale = it },
                    onAngleChange = { textAngle = it }
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(15.dp, 2.dp)
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .clipToBounds()
                ) {
                    Text(
                        text = text.ifEmpty { "Текст" },
                        color = textColor,
                        fontSize = TEXT_FONT_SIZE.sp,
                        fontFamily = textFont,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = textAngle
                                scaleX = textScale
                                scaleY = textScale
                            }
                    )
                }
            }

            Button(
                onClick = {
                    if (text.isEmpty()) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                "Текст не может быть пустым",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else
                        onCreatingTextComplete(text, textColor, textFont, textScale, textAngle)
                }
            ) {
                Text("Добавить текст")
            }
        }
    }
}

@Composable
private fun TextFontPicker(onFontClick: (font: FontFamily) -> Unit) {
    var selected by remember { mutableStateOf(fonts.first()) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp, 5.dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        scrollState.scrollBy(-delta)
                    }
                },
            )
    ) {
        items(items = fonts) { font ->
            Text(
                text = "Аа",
                textAlign = TextAlign.Center,
                fontFamily = font,
                lineHeight = 44.sp,
                fontSize = 22.sp,
                modifier = Modifier
                    .width(70.dp)
                    .aspectRatio(1f)
//                    .padding(5.dp)
                    .clip(CircleShape)
                    .clickable {
                        selected = font
                        onFontClick(font)
                    }
                    .border(1.dp, Color.DarkGray, shape = CircleShape)
                    .background(if (selected == font) Color.LightGray else Color.White, shape = CircleShape)
            )
        }
    }
}

enum class Tools(val text: String, val imageVector: ImageVector) {
    BRUSH("Кисть", Icons.Outlined.Brush),
    TEXT("Текст", Icons.Outlined.Title),
    STICKER("Стикеры", Icons.Outlined.InsertPhoto),
    SMILE("Смайлики", Icons.Outlined.SentimentSatisfiedAlt),
    FIlTER("Фильтры", Icons.Outlined.PhotoFilter)
    //    ERASER("Ластик", ),
}