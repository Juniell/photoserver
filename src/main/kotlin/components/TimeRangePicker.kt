package components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TimeRangePicker(
    timeFirst: MutableState<Int>,
    timeSecond: MutableState<Int>,
    spacing: Dp = 8.dp,
    animationHeight: Dp = 100.dp,
    textStyle: TextStyle
) {
    val range = 0..24
    Row(
        modifier = Modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        NumberPicker(
            state = timeFirst,
            range = range,
            onStateChanged = { timeFirst.value = it },
            spacing = spacing,
            animationHeight = animationHeight,
            textStyle = textStyle,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Text(
            text = " - ",
            fontSize = textStyle.fontSize,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        NumberPicker(
            state = timeSecond,
            range = range,
            onStateChanged = { timeSecond.value = it },
            spacing = spacing,
            animationHeight = animationHeight,
            textStyle = textStyle,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}