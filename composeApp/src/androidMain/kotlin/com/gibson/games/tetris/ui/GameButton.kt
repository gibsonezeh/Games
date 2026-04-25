package com.gibson.games.tetris.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val ButtonTopColor = androidx.compose.ui.graphics.Color(0xFFB76CFF)
private val ButtonBottomColor = androidx.compose.ui.graphics.Color(0xFF6200EE)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameButton(
    modifier: Modifier = Modifier,
    size: Dp,
    onClick: () -> Unit = {},
    autoInvokeWhenPressed: Boolean = false,
    content: @Composable (Modifier) -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val backgroundShape = RoundedCornerShape(size / 2)

    // 🔁 Hold-to-repeat behavior (for movement buttons)
    LaunchedEffect(isPressed, autoInvokeWhenPressed) {
        if (isPressed && autoInvokeWhenPressed) {
            delay(300)
            while (isPressed) {
                onClick()
                delay(60)
            }
        }
    }

    Box(
        modifier = modifier
            .shadow(5.dp, shape = backgroundShape)
            .size(size)
            .clip(backgroundShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ButtonTopColor,
                        ButtonBottomColor
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .pointerInput(autoInvokeWhenPressed) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content(Modifier.align(Alignment.Center))
    }
}
