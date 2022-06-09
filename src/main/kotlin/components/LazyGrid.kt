package components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> LazyGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    rowSize: Int = 1,
    itemContent: @Composable BoxScope.(T) -> Unit
) {
    val rows = items.chunked(rowSize)
    LazyColumn(modifier = modifier) {
        items(items = rows) { row ->
            Row(modifier = Modifier.fillParentMaxWidth()) {
                for ((index, item) in row.withIndex()) {
                    Box(
                        Modifier
                            .fillMaxWidth(1f / (rowSize - index))
                            .aspectRatio(1f)
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}