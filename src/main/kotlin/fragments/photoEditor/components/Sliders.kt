package fragments.photoEditor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SlidersSizeAngle(
    modifier: Modifier = Modifier,
    startScale: Float,
    startAngle: Float,
    onScaleChange: (newScale: Float) -> Unit,
    onAngleChange: (newAngle: Float) -> Unit,
    onChangeFinished: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth())
    {
        SliderWithName(
            name = "Поворот",
            value = startAngle,
            valueRange = -180f..180f,
            onValueChange = { onAngleChange(it) },
            onValueChangeFinished = { onChangeFinished() }
        )

        SliderWithName(
            name = "Размер ",
            value = startScale,
            valueRange = 0.3f..2f,
            onValueChange = { onScaleChange(it) },
            onValueChangeFinished = { onChangeFinished() }
        )
    }
}

@Composable
fun SliderWithName(
    name: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (newValue: Float) -> Unit,
    onValueChangeFinished: (Value: Float) -> Unit = {}
) {
    var sliderValue by remember { mutableStateOf(value) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.wrapContentSize()
            .padding(5.dp)
    ) {
        Text(name)

        Slider(
            value = sliderValue,
            valueRange = valueRange,
            onValueChange = {
                sliderValue = it
                onValueChange(it)
            },
            onValueChangeFinished = {
                onValueChangeFinished(sliderValue)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}