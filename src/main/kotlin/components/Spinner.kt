package components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@ExperimentalFoundationApi
@Composable
fun Spinner(
    data: List<Spinnable>,
    value: Spinnable?,
    onSelected: (selectedElement: Spinnable) -> Unit,
    padding: PaddingValues,
    textFontSize: TextUnit,
    width: Dp? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(padding)
    ) {
        OutlinedTextField(
            value = value?.toString() ?: "",
            textStyle = LocalTextStyle.current.copy(fontSize = textFontSize),
            onValueChange = { },
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .width(30.dp)
                        .aspectRatio(1f)
                )
            },
            modifier = Modifier
                .focusProperties { this.canFocus = false }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isPrimaryPressed)
                                expanded = !expanded
                        }
                    }
                }.then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            data.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onSelected(it)
                }) {
                    Text(text = it.toString(), fontSize = textFontSize)
                }
            }
        }
    }
}

interface Spinnable {
    override fun toString(): String
}