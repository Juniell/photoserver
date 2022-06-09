import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import fragments.photoChooser.DIR_MINIS
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileFilter
import java.io.InputStream
import javax.imageio.ImageIO

val imageExtensions = listOf("jpg", "jpeg", "png")
val imageFilter = FileFilter { file -> imageExtensions.contains(file.extension.toLowerCase(Locale.current)) }

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

fun createMini(imageFile: File, pathMinis: String = Settings.dirInput + File.separator + DIR_MINIS): File? {
    if (!imageFile.exists() || !imageFile.isImageFile())
        return null

    val imageFileMini = File(pathMinis + File.separator + imageFile.name)

    if (imageFileMini.exists())
        return imageFileMini

    val input: InputStream = imageFile.inputStream()
    val result: BufferedImage? = ImageIO.read(input)

    if (result != null) {
        val image: BufferedImage = scaleBitmapAspectRatio(result, 500, 500)
        val resSave = ImageIO.write(image, "JPG", imageFileMini.outputStream())

        return if (resSave)
            imageFileMini
        else
            null
    }
    return null
}

fun getImageFilesInDir(dir: File): MutableList<File> {
    val listImages = mutableListOf<File>()

    if (!dir.exists() || !dir.isDirectory)
        return listImages
    else {
        val filesInDir = dir.listFiles()

        if (!filesInDir.isNullOrEmpty())
            for (file in filesInDir) {
                if (file.isFile && file.isImageFile())
                    listImages.add(file)
            }
    }
    return listImages
}

fun File?.isImageFile() =
    imageExtensions.contains(this?.extension?.toLowerCase(Locale.current))

fun isImageFile(fileName: String?): Boolean {
    val fileExtension = fileName?.split(".")?.last()
    return imageExtensions.contains(fileExtension?.toLowerCase(Locale.current))
}