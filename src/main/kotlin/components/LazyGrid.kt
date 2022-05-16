package components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> LazyGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    rowSize: Int = 1,
    innerPadding: PaddingValues = PaddingValues(5.dp),//todo
    itemContent: @Composable BoxScope.(T) -> Unit
) {
    val rows = items.chunked(rowSize)
    LazyColumn(modifier = modifier/*.padding(padding)*//*, contentPadding = innerPadding.*/) {
        items(items = rows) { row ->
            Row(modifier = Modifier.fillParentMaxWidth()) {
                for ((index, item) in row.withIndex()) {
                    Box(
                        Modifier
                            .fillMaxWidth(1f / (rowSize - index /*+ 2*/))
                            .aspectRatio(1f)
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}