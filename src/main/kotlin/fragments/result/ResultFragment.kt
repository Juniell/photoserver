package fragments.result

import BotServer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
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
import java.io.File
import javax.imageio.ImageIO
import androidx.compose.ui.res.loadImageBitmap


@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun ResultFragment(
    photoPath: String,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit
) {
    val photo = File(photoPath)
    val idPhoto = photo.nameWithoutExtension

    BotServer().sendPhoto(photo)

    val qrTgm = generateQR(idPhoto, Social.TELEGRAM)
    var qrVk = File("qr_vk.png")

    if (!qrVk.exists())
        qrVk = generateQR(idPhoto, Social.VK)

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

// todo: Добавить в настройках указание группы вк и чата телеграма
fun generateQR(photoId: String, social: Social): File {
    var matrix: BitMatrix? = null
    val qrCodeContent = if (social == Social.TELEGRAM)
        "tg://resolve?domain=PhotoServerBot&start=$photoId"
    else
        "vk.me/club94181787"

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