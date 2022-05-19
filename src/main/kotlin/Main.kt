import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fragments.photoChooser.PhotoChooserFragment
import fragments.photoEditor.BackStack
import fragments.photoEditor.PhotoEditorFragment
import fragments.result.ResultFragment
import fragments.settings.SettingFragment
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import java.io.File

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
fun main() = application {
    var fragment by remember { mutableStateOf(Fragments.SETTINGS) }
    var settings by remember { mutableStateOf(InfoSettings(null, null, null, null, null)) }
    var selectedPhoto by remember { mutableStateOf(File("")) }
    var resultColorFilter by remember { mutableStateOf<ColorFilter?>(null) }
    var resultBackStack by remember { mutableStateOf<BackStack?>(null) }
    var renewEditor by remember { mutableStateOf(false) }

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
                    PhotoEditorFragment(
                        selectedPhoto,
                        settings.dirStickers!!,
                        onBackButtonClick = {
                            fragment = Fragments.PHOTO_CHOOSER
                            renewEditor = false
                        },
                        onNextButtonClick = { colorFilter, backStack ->
                            resultColorFilter = colorFilter
                            resultBackStack = backStack
                            fragment = Fragments.RESULT
                        },
                        renew = renewEditor
                    )
                }

                Fragments.RESULT -> {
                    ResultFragment(
                        selectedPhoto,
                        resultColorFilter,
                        resultBackStack!!,
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

enum class Fragments {
    SETTINGS,
    WELCOME,
    PHOTO_CHOOSER,
    PHOTO_EDITOR,
    RESULT
}