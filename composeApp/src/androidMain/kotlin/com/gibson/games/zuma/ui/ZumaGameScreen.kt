package com.gibson.games.zuma.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gibson.games.zuma.ZumaGameEngine
import com.gibson.games.zuma.toColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ZumaGameScreen(
    onExit: () -> Unit
) {
    val engine = remember { ZumaGameEngine() }
    var state by remember { mutableStateOf(engine.createInitialState()) }

    LaunchedEffect(Unit) {
        while (isActive && !state.isGameOver) {
            state = engine.update(state)
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF111827),
                        Color(0xFF1F2937)
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            state.chain.balls.forEach { ball ->
                val x = ball.progress * width
                val y = height / 2f

                drawCircle(
                    color = ball.color.toColor(),
                    radius = 20f,
                    center = Offset(x, y)
                )
            }
        }

        TextButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Back", color = Color.White)
        }

        if (state.isGameOver) {
            Text(
                text = "Game Over",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
