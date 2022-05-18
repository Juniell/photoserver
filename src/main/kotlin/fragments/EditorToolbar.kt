package fragments

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorToolbar(
    padding: PaddingValues,
    selectedTools: Tools,
    onClick: (newTools: Tools) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(padding)) {
        val tools = Tools.values()

        for ((i, el) in tools.withIndex()) {
            if (i != 0)
                Spacer(modifier = Modifier.width(7.dp))

            EditorButton(
                icon = el.imageVector,
                text = el.text,
                onClick = { onClick(el) },
                selected = selectedTools == el,
                modifier = EditorButtonModifier(fontSize = 25.sp, buttonMinWidth = 70.dp, buttonMaxWidth = 100.dp)
            )

            if (i != tools.size - 1)
                Spacer(modifier = Modifier.width(7.dp))
        }
    }
}


@Composable
private fun EditorButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: EditorButtonModifier = EditorButtonModifier(15.sp, 50.dp, 100.dp)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(
                width = if (selected) 3.dp else 1.dp,
                shape = RoundedCornerShape(0),
                color = Color.Black
            )
            .width(if (selected) modifier.buttonMaxWidth else modifier.buttonMinWidth)
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .wrapContentHeight(align = Alignment.CenterVertically)
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.fillMaxSize(3f / 5))

            if (selected)
                Text(text = text, fontSize = modifier.fontSize)
        }
    }
}

class EditorButtonModifier(
    val fontSize: TextUnit,
    val buttonMinWidth: Dp,
    val buttonMaxWidth: Dp
)