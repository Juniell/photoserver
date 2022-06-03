package fragments.result

import InfoSettings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import loadImageBitmap
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.Printable.NO_SUCH_PAGE
import java.awt.print.Printable.PAGE_EXISTS
import java.io.File
import javax.imageio.ImageIO
import javax.print.DocFlavor
import javax.print.PrintException
import javax.print.PrintService
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.MediaPrintableArea
import javax.print.attribute.standard.MediaSize
import javax.print.attribute.standard.OrientationRequested
import kotlin.math.min
import kotlin.math.roundToInt


@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun ResultFragment(
    photo: File,
    printService: PrintService,
    settings: InfoSettings,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit
) {
    val idPhoto = photo.nameWithoutExtension

    val qrTgm = generateQR(idPhoto, Social.TELEGRAM, settings.telegramBotName!!, settings.vkGroupId.toString())
    var qrVk = File("qr_vk.png")

    if (!qrVk.exists())
        qrVk = generateQR(idPhoto, Social.VK, settings.telegramBotName, settings.vkGroupId.toString())

    var qr by remember { mutableStateOf<File?>(qrVk) }

    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(
            initialPositionPercentage = 7f / 4f,
            moveEnabled = false
        )
    ) {
        first(700.dp) {
            Column(modifier = Modifier.fillMaxHeight().padding(10.dp)) {
                Text(
                    text = "Ваше фото",
                    fontSize = 50.sp
                )

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = loadImageBitmap(photo),
                        contentDescription = null
                    )
                }
            }

        }
        second(400.dp) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp, 0.dp).fillMaxWidth()
                ) {
                    Text(
                        text = "Id вашей фотографии:",
                        textAlign = TextAlign.Start,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    Text(
                        text = idPhoto,
                        textAlign = TextAlign.Center,
                        fontSize = 70.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    Text(
                        text = "Получить в социальных сетях:",
                        textAlign = TextAlign.Start,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    Text(
                        text = "Вы можете получить фото в течение ${settings.photoLifeTime} суток",
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)) {
                        val icons = mapOf(
                            useResource("icon/logo_vk.png") { loadImageBitmap(it) } to Social.VK,
                            useResource("icon/logo_telegram.png") { loadImageBitmap(it) } to Social.TELEGRAM,
                            useResource("icon/logo_email.png") { loadImageBitmap(it) } to Social.EMAIL,
                        )

                        icons.forEach { (icon, social) ->
                            Image(
                                painter = BitmapPainter(icon),
                                contentDescription = null,
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .padding(7.dp)
                                    .width(80.dp)
                                    .aspectRatio(1f)
                                    .clickable {
                                        qr = when (social) {
                                            Social.VK -> qrVk
                                            Social.TELEGRAM -> qrTgm
                                            Social.EMAIL -> null
                                        }
                                    }
                            )
                        }
                    }

                    if (qr != null)
                        Image(
                            painter = BitmapPainter(loadImageBitmap(qr!!)),
                            contentDescription = null,
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.padding(7.dp).width(200.dp).aspectRatio(1f)
                        )

                    //todo количество копий
                    Button(
                        onClick = {
                            printPhoto(loadImageBitmap(photo).toAwtImage(), printService)
                        }
                    ) {
                        Text("Печать")
                    }
                }


                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(7.dp).fillMaxWidth()
                ) {
                    Button(onClick = onBackButtonClick) {
                        Text("Назад", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                    }

                    Button(onClick = onNextButtonClick) {
                        Text("В начало", fontSize = 30.sp, modifier = Modifier.padding(2.dp))
                    }
                }
            }
        }
    }
}

fun printPhoto(image: BufferedImage, printService: PrintService) {
    val job = printService.createPrintJob()

    val printAttributes = HashPrintRequestAttributeSet().apply {
        if (image.width >= image.height)
            add(OrientationRequested.LANDSCAPE)
        else
            add(OrientationRequested.PORTRAIT)

        val media = MediaSize.findMedia(10F, 15F, MediaSize.INCH)   //todo: брать из настроек
        val mediaSize = MediaSize.getMediaSizeForName(media)
        val size = mediaSize.getSize(MediaSize.INCH)

        add(media)
        add(MediaPrintableArea(0f, 0f, size[0], size[1], MediaPrintableArea.INCH))
    }

    val doc = SimpleDoc(ImagePrintable(image), DocFlavor.SERVICE_FORMATTED.PRINTABLE, null)

    try {
        job.print(doc, printAttributes)
    } catch (e: PrintException) {
        e.printStackTrace()
    }
}

fun generateQR(photoId: String, social: Social, tgmName: String, vkId: String): File {
    var matrix: BitMatrix? = null
    val qrCodeContent = if (social == Social.TELEGRAM)
        "tg://resolve?domain=$tgmName&start=$photoId"
    else
        "vk.me/club$vkId"

    try {
        matrix = MultiFormatWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, 400, 400)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val im = MatrixToImageWriter.toBufferedImage(matrix)

    val qrName = if (social == Social.VK) "qr_vk.png" else "qr_tgm.png"
    val file = File(qrName)

    ImageIO.write(im, "PNG", file.outputStream())

    return file
}

enum class Social {
    VK,
    TELEGRAM,
    EMAIL
}

class ImagePrintable(private val image: BufferedImage) : Printable {
    override fun print(g: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
        g as Graphics2D
        g.translate(pageFormat.imageableX.toInt(), pageFormat.imageableY.toInt())
        if (pageIndex == 0) {
            val pageWidth = pageFormat.imageableWidth
            val pageHeight = pageFormat.imageableHeight
            val imageWidth = image.width
            val imageHeight = image.height

            val scaleX = pageWidth / imageWidth
            val scaleY = pageHeight / imageHeight
            val scaleFactor = min(scaleX, scaleY)
            g.scale(scaleFactor, scaleFactor)
            val dx = (pageWidth - imageWidth*scaleFactor) / 2
            val dy = (pageHeight - imageHeight*scaleFactor) / 2

            g.drawImage(image, (dx / scaleFactor).roundToInt(), (dy / scaleFactor).roundToInt(), null)

            return PAGE_EXISTS
        }
        return NO_SUCH_PAGE
    }
}