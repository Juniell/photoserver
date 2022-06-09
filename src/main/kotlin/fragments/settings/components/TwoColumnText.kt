package fragments.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun TwoColumnText(
    listPairsOfText: List<Pair<String, String>>,
    fontSize: TextUnit,
    bottomPaddingItem: Dp,
    spaceBetweenColumn: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spaceBetweenColumn),
        modifier = modifier.wrapContentSize()
    ) {
        ColumnText(
            listText = listPairsOfText.map { it.first },
            fontSize = fontSize,
            bottomPaddingItem = bottomPaddingItem
        )
        ColumnText(
            listText = listPairsOfText.map { it.second },
            fontSize = fontSize,
            bottomPaddingItem = bottomPaddingItem
        )
    }
}


@Composable
fun ColumnText(
    listText: List<String>,
    fontSize: TextUnit,
    bottomPaddingItem: Dp
) {
    Column(modifier = Modifier.wrapContentSize())
    {
        listText.forEach {
            Text(
                text = it,
                fontSize = fontSize,
                modifier = Modifier.padding(bottom = bottomPaddingItem).wrapContentSize()
            )
        }
    }
}
