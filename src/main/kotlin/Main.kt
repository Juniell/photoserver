import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fragments.photoChooser.PhotoChooserFragment
import fragments.photoEditor.PhotoEditorFragment
import fragments.result.ResultFragment
import fragments.settings.Printer
import fragments.settings.SettingFragment
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import java.io.File
import javax.print.attribute.standard.MediaSizeName

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
fun main() = application {
    var fragment by remember { mutableStateOf(Fragments.SETTINGS) }
    var settings by remember { mutableStateOf<InfoSettings?>(null) }
    var selectedPhoto by remember { mutableStateOf(File("")) }
    var renewEditor by remember { mutableStateOf(false) }
    var resultPhoto by remember { mutableStateOf<File?>(null) }
    var selectedPrinter by remember { mutableStateOf<Printer?>(null) }
    var vkChange by remember { mutableStateOf(false) }
    var paperSize by remember { mutableStateOf<MediaSizeName?>(null) }


    if (fragment == Fragments.SETTINGS)
        Window(
            title = "Settings",
            state = rememberWindowState(width = 1000.dp, height = 750.dp),
            onCloseRequest = ::exitApplication
        ) {
            SettingFragment(onNextButtonClick = { newSettings, printer, paperSizeName, vkGroupChange ->
                settings = newSettings
                selectedPrinter = printer
                paperSize = paperSizeName
                vkChange = vkGroupChange
                fragment = Fragments.WELCOME
            })
        }
    else {
        Window(
            title = "PhotoServer",
            state = rememberWindowState(WindowPlacement.Maximized/*Fullscreen*/),
            onCloseRequest = ::exitApplication
        ) {
//            Box(
//                modifier = Modifier.paint(
//                    BitmapPainter(loadImageBitmap(File("background1.jpg"))),
//                    contentScale = ContentScale.FillBounds
//                )
//            ) {
            when (fragment) {
                Fragments.WELCOME ->
                    WelcomeFragment(
                        textWelcome = settings!!.textWelcome!!,
                        onButtonClick = { fragment = Fragments.PHOTO_CHOOSER })

                Fragments.PHOTO_CHOOSER ->
                    PhotoChooserFragment(
                        settings = settings!!,
                        onBackButtonClick = { fragment = Fragments.WELCOME },
                        onNextButtonClick = { photo ->
                            selectedPhoto = photo
                            fragment = Fragments.PHOTO_EDITOR
                        }
                    )

                Fragments.PHOTO_EDITOR -> {
                    PhotoEditorFragment(
                        photo = selectedPhoto,
                        dirOutput = settings!!.dirOutput!!,
                        stickerPath = settings!!.dirStickers!!,
                        paperSize = paperSize!!,
                        onBackButtonClick = {
                            fragment = Fragments.PHOTO_CHOOSER
                            renewEditor = false
                        },
                        onNextButtonClick = { resPhoto ->
                            resultPhoto = resPhoto
                            sendPhoto(resPhoto, settings!!.botServerAddress!!, settings!!.dirOutput!!)
                            fragment = Fragments.RESULT
                        },
                        renew = renewEditor
                    )
                }

                Fragments.RESULT -> {
                    ResultFragment(
                        photo = resultPhoto!!,
                        printService = selectedPrinter!!.printService,
                        paperSize = paperSize!!,
                        settings = settings!!,
                        vkGroupChange = vkChange,
                        onBackButtonClick = {
                            fragment = Fragments.PHOTO_EDITOR
                            renewEditor = true
                        },
                        onNextButtonClick = {
                            fragment = Fragments.WELCOME
                            renewEditor = false
                        }
                    )
                }

                else -> {}
            }
//            }
        }
    }
}

fun sendPhoto(photo: File, botAddress: String, dirOutput: String) {
    BotServer.apply {
        if (!checkUrlInit())
            initApi(botAddress)

        if (!checkDirInit())
            initOutputDir(dirOutput)

        sendPhoto(photo)
    }
}


enum class Fragments {
    SETTINGS,
    WELCOME,
    PHOTO_CHOOSER,
    PHOTO_EDITOR,
    RESULT
}