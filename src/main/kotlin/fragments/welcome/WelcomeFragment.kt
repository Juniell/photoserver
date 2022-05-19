import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//private const val startText = "Не хотите ждать, пока фотограф выложит обработанные фото?" +
//        "Вы можете получить их прямо сейчас! " +
//        "Просто нажмите на кнопку \"Начать\", выберете фото и отредактируйте." +
//        "Вы сможете получить своё фото в VK или Telegram."

@Composable
fun WelcomeFragment(textWelcome: String, onButtonClick: () -> Unit) {
    MaterialTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = textWelcome,
                fontSize = 60.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(1200.dp)
            )

            Button(
                onClick = onButtonClick,
                modifier = Modifier.padding(vertical = 100.dp)
            ) {
                Text(
                    text = "Начать",
                    textAlign = TextAlign.Center,
                    fontSize = 90.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        }
    }
}
