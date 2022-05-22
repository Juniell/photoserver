package fragments.photoEditor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import java.io.File

open class Layer(
    open val name: String,
    open var offset: MutableState<Offset>,
    open var angle: MutableState<Float>,
    open var scale: MutableState<Float>,
)

class BrushLayer(
    val path: Path,
    val color: Color,
    val brushSize: Float,
    override val name: String = "Линия",
) : Layer(name, mutableStateOf(Offset.Zero), mutableStateOf(0f), mutableStateOf(1f))

class ImageLayer(
    val image: File,
    override var scale: MutableState<Float>,
    override var angle: MutableState<Float>,
    override var offset: MutableState<Offset>,
    override val name: String = "Стикер"
) : Layer(name, offset, angle, scale)

class TextLayer(
    val text: String,
    val color: Color,
    val fontFamily: FontFamily,
    override var scale: MutableState<Float>,
    override var angle: MutableState<Float>,
    override var offset: MutableState<Offset>,
    override val name: String = "Текст"
) : Layer(name, offset, angle, scale)

