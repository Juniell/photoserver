import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fragments.photoChooser.PhotoChooserFragment
import fragments.photoEditor.PhotoEditorFragment
import fragments.result.ResultFragment
import fragments.settings.SettingFragment
import fragments.welcome.WelcomeFragment
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import java.io.File

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
fun main() = application {
    var fragment by remember { mutableStateOf(Fragments.SETTINGS) }
    var renewEditor by remember { mutableStateOf(false) }
    var resultPhoto by remember { mutableStateOf<File?>(null) }

    if (fragment == Fragments.SETTINGS) {
        Settings.oldVkGroupId = Settings.vkGroupId
        Settings.oldFtpUserLogin = Settings.ftpUserLogin
        Settings.oldFtpUserPassword = Settings.ftpUserPassword

        Window(
            title = "Settings",
            state = rememberWindowState(width = 1000.dp, height = 750.dp),
            onCloseRequest = ::exitApplication
        ) {
            SettingFragment(
                onNextButtonClick = {
                    // start or stop ktor server for mirrors
                    when {
                        Settings.mirrorNeed && !MirrorsServer.isWorks() -> MirrorsServer.start()
                        !Settings.mirrorNeed && MirrorsServer.isWorks() -> MirrorsServer.stop()
                    }
                    // start or stop ftp server for cameras
                    when {
                        Settings.camerasNeed && !FtpServer.isWorks() -> FtpServer.start()
                        !Settings.camerasNeed && FtpServer.isWorks() -> FtpServer.stop()
                    }

                    fragment = Fragments.WELCOME
                }
            )
        }
    }
    else {
        Window(
            title = "PhotoServer",
            state = rememberWindowState(WindowPlacement.Maximized/*Fullscreen*/),
            onCloseRequest = ::exitApplication,
            onPreviewKeyEvent = {
                if (it.key == Key.Escape) {
                    fragment = Fragments.SETTINGS
                    true
                } else
                    false
            }
        ) {

            when (fragment) {
                Fragments.WELCOME ->
                    WelcomeFragment(
                        onButtonClick = { fragment = Fragments.PHOTO_CHOOSER })

                Fragments.PHOTO_CHOOSER ->
                    PhotoChooserFragment(
                        onBackButtonClick = { fragment = Fragments.WELCOME },
                        onNextButtonClick = {
                            fragment = Fragments.PHOTO_EDITOR
                        }
                    )

                Fragments.PHOTO_EDITOR -> {
                    PhotoEditorFragment(
                        onBackButtonClick = {
                            fragment = Fragments.PHOTO_CHOOSER
                            renewEditor = false
                        },
                        onNextButtonClick = { resPhoto ->
                            resultPhoto = resPhoto
                            if (Settings.botNeed)
                                sendPhoto(resPhoto)
                            fragment = Fragments.RESULT
                        },
                        renew = renewEditor
                    )
                }

                Fragments.RESULT -> {
                    ResultFragment(
                        photo = resultPhoto!!,
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
        }
    }
}

private fun sendPhoto(photo: File) {
    BotClient.apply {
        if (!checkUrlInit())
            initApi(Settings.botServerAddress)

        if (!checkDirInit())
            initOutputDir(Settings.dirOutput)

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