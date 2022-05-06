package components

import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    state: MutableState<Int>,
    range: IntRange = 0..2,
    spacing: Dp = 0.dp,
    animationHeight: Dp = 50.dp,
    width: Dp = 50.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    onStateChanged: (Int) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val halvedNumbersColumnHeight = animationHeight / 2
    val halvedNumbersColumnHeightPx = with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

    fun animatedStateValue(offset: Float): Int = state.value - (offset / halvedNumbersColumnHeightPx).toInt()

    val animatedOffset = remember { Animatable(0f) }.apply {
        val offsetRange = remember(state.value, range) {
            val value = state.value
            val first = -(range.last - value) * halvedNumbersColumnHeightPx
            val last = -(range.first - value) * halvedNumbersColumnHeightPx
            first..last
        }
        updateBounds(offsetRange.start, offsetRange.endInclusive)
    }
    val coercedAnimatedOffset = animatedOffset.value % halvedNumbersColumnHeightPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)

    Column(
        modifier = modifier
            .wrapContentSize()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halvedNumbersColumnHeightPx
                                val coercedAnchors =
                                    listOf(-halvedNumbersColumnHeightPx, 0f, halvedNumbersColumnHeightPx)
                                val coercedPoint = coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base = halvedNumbersColumnHeightPx * (target / halvedNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        state.value = animatedStateValue(endValue)
                        onStateChanged(state.value)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
    ) {
        val arrowColor = MaterialTheme.colors.onSecondary.copy(alpha = ContentAlpha.disabled)

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowUp,
            tint = arrowColor,
            contentDescription = "",
            modifier = Modifier.width(width).align(Alignment.CenterHorizontally).aspectRatio(1f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { if (state.value != range.first) state.value-- }
        )

        Spacer(modifier = Modifier.height(spacing))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
                .height(animationHeight)
        ) {
            val baseLabelModifier = Modifier.align(Alignment.Center)
            val textFormat = "%02d:00"
            ProvideTextStyle(textStyle) {
                Label(
                    text = String.format(textFormat, animatedStateValue - 1),
                    modifier = baseLabelModifier
                        .offset(y = -halvedNumbersColumnHeight)
                        .alpha(coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                )
                Label(
                    text = String.format(textFormat, animatedStateValue),
                    modifier = baseLabelModifier
                        .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx)
                )
                Label(
                    text = String.format(textFormat, animatedStateValue + 1),
                    modifier = baseLabelModifier
                        .offset(y = halvedNumbersColumnHeight)
                        .alpha(-coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            tint = arrowColor,
            contentDescription = "",
            modifier = Modifier.width(width).align(Alignment.CenterHorizontally).aspectRatio(1f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { if (state.value != range.last) state.value++ }
        )
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                // FIXME: Empty to disable text selection
            })
        }
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)

    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}

@Preview
@Composable
fun PreviewNumberPicker() {
    Box(modifier = Modifier.fillMaxSize()) {
        NumberPicker(
            state = remember { mutableStateOf(9) },
            range = 0..10,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}