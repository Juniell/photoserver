package fragments.result

import Email
import Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import components.NumberPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import java.util.*
import javax.imageio.ImageIO
import javax.print.DocFlavor
import javax.print.PrintException
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.*
import kotlin.math.min


@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun ResultFragment(
    photo: File,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit
) {
    val idPhoto = photo.nameWithoutExtension

    val qrTgm = if (Settings.botNeed)
        generateQR(idPhoto, Social.TELEGRAM, Settings.telegramBotName)
    else
        null

    val qrVk by remember {
        mutableStateOf(
            if (Settings.botNeed) {
                if (!File("qr_vk.png").exists() || Settings.vkGroupId != Settings.oldVkGroupId)
                    generateAndSaveQr(idPhoto, Social.VK, Settings.vkGroupId.toString())
                else
                    loadImageBitmap(File("qr_vk.png"))
            } else
                null
        )
    }

    var qr by remember { mutableStateOf(qrVk) }
    var selectedSocial by remember { mutableStateOf(if (Settings.emailNeed && !Settings.botNeed) Social.EMAIL else Social.VK) }
    var emailToAddr by remember { mutableStateOf("") }
    var emailToAddrError by remember { mutableStateOf(false) }
    var sendMailButtonText by remember { mutableStateOf("Отправить") }
    var printButtonEnabled by remember { mutableStateOf(true) }
    var copiesMax by remember { mutableStateOf(Settings.photoCopiesNum) }
    val coroutineScope = rememberCoroutineScope()

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
                    modifier = Modifier.padding(10.dp, 0.dp).fillMaxWidth().wrapContentHeight()
                ) {
                    if (Settings.botNeed) {
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
                    }

                    if (Settings.botNeed || Settings.emailNeed)
                        Text(
                            text = if (Settings.botNeed) "Получить в социальных сетях:" else "Получить по почте",
                            textAlign = TextAlign.Start,
                            fontSize = 40.sp,
                            modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                        )

                    if (Settings.botNeed) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icons = mapOf(
                                useResource("icon/logo_vk.png") { loadImageBitmap(it) } to Social.VK,
                                useResource("icon/logo_telegram.png") { loadImageBitmap(it) } to Social.TELEGRAM,
                                useResource("icon/logo_email.png") { loadImageBitmap(it) } to Social.EMAIL,
                            )

                            icons.forEach { (icon, social) ->
                                if (social == Social.EMAIL && !Settings.emailNeed) {
                                    // пусто, чтобы не показывать иконку Email
                                } else
                                    Image(
                                        painter = BitmapPainter(icon),
                                        contentDescription = null,
                                        contentScale = ContentScale.Inside,
                                        modifier = Modifier
                                            .padding(7.dp)
                                            .size(if (selectedSocial == social) 120.dp else 80.dp)
                                            .clickable {
                                                qr = when (social) {
                                                    Social.VK -> qrVk
                                                    Social.TELEGRAM -> qrTgm
                                                    Social.EMAIL -> null
                                                }
                                                selectedSocial = social
                                            }
                                    )
                            }
                        }

                        Text(
                            text = "Вы можете получить фото в течение ${Settings.photoLifeTime} суток",
                            textAlign = TextAlign.Start,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(0.dp, 5.dp)
                                .fillMaxWidth()
                        )

                        if (qr != null)
                            Image(
                                bitmap = qr!!,
                                contentDescription = null,
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .aspectRatio(1f)
                            )
                    }


                    if (selectedSocial == Social.EMAIL) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var emailSuccess by remember { mutableStateOf<Boolean?>(null) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = emailToAddr,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                                    onValueChange = {
                                        emailToAddr = it
                                        emailToAddrError = !emailToAddr.matches(Regex("""(\w|\d)+@\w+\.\w+"""))
                                        emailSuccess = null
                                    },
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )

                                Button(
                                    enabled = !emailToAddrError && sendMailButtonText == "Отправить",
                                    onClick = {
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                Email.sendEmail(
                                                    to = emailToAddr,
                                                    photo = photo,
                                                    onSuccess = { emailSuccess = true },
                                                    onFail = { emailSuccess = false }
                                                )
                                            }
                                        }

                                        coroutineScope.launch {
                                            sendMailButtonText = "Подождите"
                                            for (i in 5 downTo 1)
                                                delay(1000)

                                            sendMailButtonText = "Отправить"
                                        }
                                    }
                                ) {
                                    Text(sendMailButtonText)
                                }
                            }

                            if (emailSuccess != null)
                                Text(
                                    text = if (emailSuccess!!)
                                        "Фото успешно отправлено на указанную почту"
                                    else
                                        "К сожалению, не удалось отправить фото на указанную почту",
                                    fontSize = 20.sp,
                                )
                        }
                    }
                }

                Column {
                    if (Settings.printNeed) {
                        val copiesSelect = remember { mutableStateOf(1) }

                        Text(
                            text = "Распечатать фото",
                            fontSize = 40.sp,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (copiesMax > 1) Arrangement.SpaceAround else Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
                        ) {

                            if (copiesMax > 1) {
                                Text(
                                    text = "Количество копий:",
                                    fontSize = 25.sp
                                )

                                NumberPicker(
                                    state = copiesSelect,
                                    range = 1..copiesMax,
                                    timeFormat = false,
                                    animationHeight = 40.dp,
                                    width = 40.dp,
                                    onStateChanged = { copiesSelect.value = it },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 25.sp)
                                )
                            }

                            Button(
                                enabled = (copiesMax > 0) && printButtonEnabled,
                                onClick = {
                                    coroutineScope.launch {
                                        printPhoto(loadImageBitmap(photo).toAwtImage(), copiesSelect.value)
                                        copiesMax -= copiesSelect.value
                                        copiesSelect.value = 1
                                    }
                                    coroutineScope.launch {
                                        printButtonEnabled = false
                                        for (i in 5 downTo 1)
                                            delay(1000)
                                        printButtonEnabled = true
                                    }
                                },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text(
                                    text = if (printButtonEnabled) "Печать" else "Подождите",
                                    fontSize = 24.sp
                                )
                            }
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
}

fun generateAndSaveQr(photoId: String, social: Social, socialName: String): ImageBitmap {
    val image = generateQR(photoId, social, socialName)
    val qrName = if (social == Social.VK) "qr_vk.png" else "qr_tgm.png"
    val file = File(qrName)

    ImageIO.write(image.toAwtImage(), "PNG", file.outputStream())

    return image
}

fun generateQR(photoId: String, social: Social, socialName: String): ImageBitmap {
    var matrix: BitMatrix? = null
    val qrCodeContent = if (social == Social.TELEGRAM)
        "tg://resolve?domain=$socialName&start=$photoId"
    else
        "vk.me/club$socialName"

    val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
    hints[EncodeHintType.MARGIN] = 1

    try {
        matrix = MultiFormatWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, 400, 400, hints)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return MatrixToImageWriter.toBufferedImage(matrix).toComposeImageBitmap()
}

fun printPhoto(image: BufferedImage, copiesNum: Int) {
    if (Settings.printer == null || Settings.paper == null)
        return

    val job = Settings.printer!!.createPrintJob()

    val printAttributes = HashPrintRequestAttributeSet().apply {
        if (image.width >= image.height)
            add(OrientationRequested.LANDSCAPE)
        else
            add(OrientationRequested.PORTRAIT)

        val mediaSize = MediaSize.getMediaSizeForName(Settings.paper)
        val size = mediaSize.getSize(MediaSize.MM)

        add(Settings.paper)

        var mediaPrintableArea = MediaPrintableArea(0f, 0f, size[0], size[1], MediaPrintableArea.MM)
        val array = (Settings.printer!!
            .getSupportedAttributeValues(MediaPrintableArea::class.java, null, this)) as Array<*>

        for (el in array)
            if (el is MediaPrintableArea) {
                mediaPrintableArea = el
                break
            }

        add(mediaPrintableArea)
        add(Copies(copiesNum))
    }

    val doc = SimpleDoc(ImagePrintable(image), DocFlavor.SERVICE_FORMATTED.PRINTABLE, null)

    try {
        job.print(doc, printAttributes)
    } catch (e: PrintException) {
        e.printStackTrace()
    }

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
            g.drawImage(image, 0, 0, null)

            return PAGE_EXISTS
        }
        return NO_SUCH_PAGE
    }
}

enum class Social {
    VK,
    TELEGRAM,
    EMAIL
}