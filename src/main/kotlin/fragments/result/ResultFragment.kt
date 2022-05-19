package fragments.result

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fragments.photoEditor.BackStack
import fragments.photoEditor.Layers
import loadImageBitmap
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.io.File

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun ResultFragment(
    photo: File,
    colorFilter: ColorFilter?,
    backStack: BackStack,
    onBackButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit
) {

    val idPhoto = 54

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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .paint(BitmapPainter(loadImageBitmap(photo)), colorFilter = colorFilter)
                            .clipToBounds()
                    ) {
                        Layers(
                            modifierBrush = Modifier.matchParentSize(),
                            layers = backStack.currLayers(),
                            editingEnabled = false
                        )
                    }
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
                        text = idPhoto.toString(),
                        textAlign = TextAlign.Center,
                        fontSize = 70.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    Text(
                        text = "Поделиться в социальных сетях:",
                        textAlign = TextAlign.Start,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth()
                    )

                    val path = File(".").path + File.separator + "src" + File.separator + "main" + File.separator +
                            "resources" + File.separator + "icon" + File.separator

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)) {
                        val icons = listOf(
                            File(path + "logo_vk.png"),
                            File(path + "logo_telegram.png"),
                            File(path + "logo_email.png")
                        )

                        icons.forEach {
                            Image(
                                painter = BitmapPainter(loadImageBitmap(it)),
                                contentDescription = null,
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.padding(7.dp).width(80.dp).aspectRatio(1f)
                            )
                        }
                    }

                    val qr = File(path + "qr.png")

                    Image(
                        painter = BitmapPainter(loadImageBitmap(qr)),
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