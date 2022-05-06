import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// todo: Добавить возможность влиять на размер
// todo: Оставить только SpinnerAny

@Composable
fun Spinner(data: List<String>, selectedValue: String = "") {

    var selected: String by remember { mutableStateOf(selectedValue) }
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .defaultMinSize(minHeight = 55.dp)
//            .padding(start = 15.dp, end = 8.dp)
            .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8))
            .clickable {
                expanded = !expanded
            }
    ) {

        Text(
            text = selected,
            fontSize = 18.sp,
            modifier = Modifier.defaultMinSize(minWidth = 453.dp).padding(start = 15.dp)
        )

        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            data.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    selected = it
                }) {
                    Text(text = it)
                }
            }
        }
    }
}

@Composable
fun Spinner(
    data: List<Spinnable>,
    selectedValue: MutableState<Spinnable>,
    onSelected: () -> Unit,
    modifier: SpinnerModifier = SpinnerModifier(
        padding = PaddingValues(7.dp),
        width = 500.dp,
        height = 55.dp
    )
) {
    var expanded by remember { mutableStateOf(false) }
    val innPaddings = 15.dp

    Column(modifier = Modifier.wrapContentSize().padding(modifier.padding)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .width(modifier.width).height(modifier.height /*- innPaddings * 2*/)
                .clickable { expanded = !expanded }
                .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8))
        ) {
            Text(
                maxLines = 1,
                text = selectedValue.value.toString(),
                fontSize = modifier.textFontSize,
                modifier = Modifier.padding(start = 15.dp)
            )

            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.aspectRatio(1f).padding(all = 15.dp).fillMaxHeight()
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            data.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    selectedValue.value = it
                    onSelected()
                }) {
                    Text(text = it.toString(), fontSize = modifier.textFontSize)
                }
            }
        }
    }
}

//@Composable
//fun SpinnerAny(
//    data: List<Any>,
//    selectedValue: MutableState<Any>,
//    onSelected: () -> Unit,
//    modifier: SpinnerModifier = SpinnerModifier(
//        padding = PaddingValues(7.dp),
//        width = 500.dp,
//        height = 55.dp
//    )
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val innPaddings = 15.dp
//
//    Column(modifier = Modifier.wrapContentSize().padding(modifier.padding)) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier
//                .width(modifier.width).height(modifier.height - innPaddings * 2)
//                .clickable { expanded = !expanded }
//                .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8))
//        ) {
//            Text(
//                maxLines = 1,
//                text = selectedValue.value.toString(),
//                fontSize = modifier.textFontSize,
//                modifier = Modifier.padding(start = 15.dp)
//            )
//
//            Icon(
//                imageVector = Icons.Filled.ArrowDropDown,
//                contentDescription = null,
//                modifier = Modifier.aspectRatio(1f).padding(all = 15.dp).fillMaxHeight()
//            )
//        }
//
//        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            data.forEach {
//                DropdownMenuItem(onClick = {
//                    expanded = false
//                    selectedValue.value = it
//                    onSelected()
//                }) {
//                    Text(text = it.toString(), fontSize = modifier.textFontSize)
//                }
//            }
//        }
//    }
//}


@Composable
@Preview
fun SpinnerPreview() = Spinner(listOf("Первый, Второй, Третий"))

data class SpinnerModifier(
    val padding: PaddingValues,
    val width: Dp,
    val height: Dp,
    val textFontSize: TextUnit = 18.sp,
)

interface Spinnable {
    override fun toString(): String
}