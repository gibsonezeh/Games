package com.gibson.games.zuma.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import com.gibson.games.zuma.*
import kotlinx.coroutines.delay

@Composable
fun ZumaGameScreen() {

    val engine = remember { ZumaGameEngine() }
    var state by remember { mutableStateOf(engine.createInitialState()) }

    // 🎮 Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            state = engine.update(state)
            delay(16) // ~60 FPS
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {

        val width = size.width
        val height = size.height

        // 🟡 Draw balls
        state.chain.balls.forEach { ball ->

            val x = ball.progress * width
            val y = height / 2

            drawCircle(
                color = ball.color.toColor(),
                radius = 20f,
                center = Offset(x, y)
            )
        }
    }
}
