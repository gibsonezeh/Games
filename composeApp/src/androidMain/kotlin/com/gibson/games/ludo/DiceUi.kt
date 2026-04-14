package com.gibson.games.ludo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class CenterDiceAnimState {
    IDLE,
    SPLIT,
    RETURN
}

@Composable
fun CenterDiceRoller(
    die1Value: Int,
    die2Value: Int,
    animationState: CenterDiceAnimState,
    isRolling: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = animationState,
        label = "center_dice_transition"
    )

    val die1OffsetX = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die1OffsetX"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> -16f
            CenterDiceAnimState.SPLIT -> -26f
            CenterDiceAnimState.RETURN -> -16f
        }
    }

    val die1OffsetY = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die1OffsetY"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 0f
            CenterDiceAnimState.SPLIT -> -6f
            CenterDiceAnimState.RETURN -> 0f
        }
    }

    val die2OffsetX = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die2OffsetX"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 16f
            CenterDiceAnimState.SPLIT -> 26f
            CenterDiceAnimState.RETURN -> 16f
        }
    }

    val die2OffsetY = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die2OffsetY"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 0f
            CenterDiceAnimState.SPLIT -> 6f
            CenterDiceAnimState.RETURN -> 0f
        }
    }

    val die1Rotation = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die1Rotation"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 0f
            CenterDiceAnimState.SPLIT -> -12f
            CenterDiceAnimState.RETURN -> 0f
        }
    }

    val die2Rotation = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "die2Rotation"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 0f
            CenterDiceAnimState.SPLIT -> 12f
            CenterDiceAnimState.RETURN -> 0f
        }
    }

    val diceScale = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
        label = "diceScale"
    ) { state ->
        when (state) {
            CenterDiceAnimState.IDLE -> 1f
            CenterDiceAnimState.SPLIT -> 1.02f
            CenterDiceAnimState.RETURN -> 1f
        }
    }

    Box(
        modifier = modifier.clickable(
            enabled = !isRolling,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = die1OffsetX.value.roundToInt(),
                        y = die1OffsetY.value.roundToInt()
                    )
                }
                .rotate(die1Rotation.value)
                .scale(diceScale.value)
        ) {
            DiceFace(
                value = die1Value,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = die2OffsetX.value.roundToInt(),
                        y = die2OffsetY.value.roundToInt()
                    )
                }
                .rotate(die2Rotation.value)
                .scale(diceScale.value)
        ) {
            DiceFace(
                value = die2Value,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DiceFace(
    value: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFFFDFDFD), RoundedCornerShape(10.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
    ) {
        val safeValue = value.coerceIn(1, 6)
        val pipRadius = size.minDimension * 0.08f

        val left = size.width * 0.25f
        val centerX = size.width * 0.5f
        val right = size.width * 0.75f

        val top = size.height * 0.25f
        val centerY = size.height * 0.5f
        val bottom = size.height * 0.75f

        fun pip(x: Float, y: Float) {
            drawCircle(
                color = Color.Black,
                radius = pipRadius,
                center = Offset(x, y)
            )
        }

        when (safeValue) {
            1 -> pip(centerX, centerY)
            2 -> {
                pip(left, top)
                pip(right, bottom)
            }
            3 -> {
                pip(left, top)
                pip(centerX, centerY)
                pip(right, bottom)
            }
            4 -> {
                pip(left, top)
                pip(right, top)
                pip(left, bottom)
                pip(right, bottom)
            }
            5 -> {
                pip(left, top)
                pip(right, top)
                pip(centerX, centerY)
                pip(left, bottom)
                pip(right, bottom)
            }
            6 -> {
                pip(left, top)
                pip(right, top)
                pip(left, centerY)
                pip(right, centerY)
                pip(left, bottom)
                pip(right, bottom)
            }
        }
    }
}

@Composable
fun DiceCard(
    title: String,
    value: String,
    isRolling: Boolean,
    isTotal: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable(
                enabled = isEnabled && !isRolling,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFF10B981)
                isTotal -> Color(0xFF3B82F6)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected || isTotal) Color.White else Color.Gray
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected || isTotal) Color.White else Color.Black
            )
        }
    }
}
