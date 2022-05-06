import fragments.SettingFragment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fragments.PhotoChooserFragment
import fragments.PhotoEditorFragment
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import java.io.File

@ExperimentalSplitPaneApi
fun main() = application {
    var fragment by remember { mutableStateOf(Fragments.SETTINGS) }
    var settings by remember { mutableStateOf(InfoSettings(null, null, null, null, null)) }
    var selectedPhoto by remember { mutableStateOf(File("")) }

    if (fragment == Fragments.SETTINGS)
        Window(
            title = "Settings",
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            onCloseRequest = ::exitApplication
        ) {
            SettingFragment(onNextButtonClick = { newSettings ->
                settings = newSettings
                fragment = Fragments.WELCOME
            })
        }
    else {
        Window(
            title = "PhotoServer",
            state = rememberWindowState(WindowPlacement.Maximized/*Fullscreen*/),
            onCloseRequest = ::exitApplication
        ) {
            when (fragment) {
                Fragments.WELCOME ->
                    WelcomeFragment(
                        textWelcome = settings.textWelcome!!,
                        onButtonClick = { fragment = Fragments.PHOTO_CHOOSER })

                Fragments.PHOTO_CHOOSER ->
                    PhotoChooserFragment(
                        settings = settings,
                        onBackButtonClick = { fragment = Fragments.WELCOME },
                        onNextButtonClick = { photo ->
                            selectedPhoto = photo
                            fragment = Fragments.PHOTO_EDITOR
                        }
                    )

                Fragments.PHOTO_EDITOR -> {
                    PhotoEditorFragment(selectedPhoto, settings.dirStickers!!)
                }

                else -> {}
            }
        }
    }
}

enum class Fragments {
    SETTINGS,
    WELCOME,
    PHOTO_CHOOSER,
    PHOTO_EDITOR
}