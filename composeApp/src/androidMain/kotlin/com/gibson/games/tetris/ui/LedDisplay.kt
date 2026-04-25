package com.gibson.games.tetris.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val BrickMatrix = Color(0xFF87936F)
private val BrickSpirit = Color(0xFF1D2418)

@Composable
fun LedClock(
    modifier: Modifier = Modifier
) {
    val animateValue by rememberInfiniteTransition(label = "clock")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "clock_blink"
        )

    var clock by remember { mutableStateOf(0 to 0) }

    LaunchedEffect(animateValue.roundToInt()) {
        val dateFormat = SimpleDateFormat("H,m", Locale.getDefault())
        val (hour, minute) = dateFormat.format(Date()).split(",")
        clock = hour.toInt() to minute.toInt()
    }

    Row(modifier) {
        LedNumber(
            num = clock.first,
            digits = 2,
            fillZero = true
        )

        Box(
            modifier = Modifier
                .width(6.dp)
                .padding(end = 1.dp)
        ) {
            LedComma(BrickMatrix)

            if (animateValue.roundToInt() == 1) {
                LedComma(BrickSpirit)
            }
        }

        LedNumber(
            num = clock.second,
            digits = 2,
            fillZero = true
        )
    }
}

@Composable
private fun LedComma(
    color: Color
) {
    Text(
        text = ":",
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.End,
        color = color,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LedNumber(
    modifier: Modifier = Modifier,
    num: Int,
    digits: Int,
    fillZero: Boolean = false
) {
    val textSize = 16.sp
    val textWidth = 8.dp

    Box(modifier) {
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            repeat(digits) {
                Text(
                    text = "8",
                    fontSize = textSize,
                    color = BrickMatrix,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(textWidth),
                    textAlign = TextAlign.End
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val value = if (fillZero) {
                num.toString().padStart(digits, '0')
            } else {
                num.toString()
            }

            value.takeLast(digits).forEach { char ->
                Text(
                    text = char.toString(),
                    fontSize = textSize,
                    color = BrickSpirit,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(textWidth),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
