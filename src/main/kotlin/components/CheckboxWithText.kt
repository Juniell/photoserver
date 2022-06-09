package components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@ExperimentalMaterialApi
@Composable
fun CheckboxWithText(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    text: String,
    fontSize: TextUnit,
    spaceBetween: Dp,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
            Checkbox(
                enabled = enabled,
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
        Text(
            text = text,
            fontSize = fontSize
        )
    }
}