import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.awt.image.BufferedImage
import java.io.File

fun scaleBitmapAspectRatio(
    bitmap: BufferedImage,
    width: Int,
    height: Int
): BufferedImage {
    val boundW: Float = width.toFloat()
    val boundH: Float = height.toFloat()

    val ratioX: Float = boundW / bitmap.width
    val ratioY: Float = boundH / bitmap.height
    val ratio: Float = if (ratioX < ratioY) ratioX else ratioY

    val resultH = (bitmap.height * ratio).toInt()
    val resultW = (bitmap.width * ratio).toInt()

    val result = BufferedImage(resultW, resultH, BufferedImage.TYPE_INT_RGB)
    val graphics = result.createGraphics()
    graphics.drawImage(bitmap, 0, 0, resultW, resultH, null)
    graphics.dispose()

    return result
}

fun loadImageBitmap(file: File): ImageBitmap =
    file.inputStream().buffered().use(::loadImageBitmap)