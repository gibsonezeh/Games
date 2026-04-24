package com.gibson.games.carrom.ui

import androidx.compose.foundation.Canvas
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.gibson.games.carrom.*
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun CarromGameScreen(
    onExit: () -> Unit
) {
    BackHandler {
        onExit()
    }

    val engine = remember { CarromGameEngine() }
    var state by remember { mutableStateOf(engine.createInitialState()) }

    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    // 🎮 Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            state = engine.update(state)
            delay(16)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.isMoving) {

                if (!state.isMoving) {
                    detectDragGestures(
                        onDragStart = {
                            dragStart = it
                            dragCurrent = it
                        },
                        onDrag = { change, _ ->
                            dragCurrent = change.position
                        },
                        onDragEnd = {

                            val start = dragStart
                            val end = dragCurrent

                            if (start != null && end != null) {

                                val dx = start.x - end.x
                                val dy = start.y - end.y
                                val distance = sqrt(dx * dx + dy * dy)

                                val power = distance * 0.2f

                                state = engine.shootStriker(
                                    state,
                                    aimX = start.x,
                                    aimY = start.y,
                                    power = power
                                )
                            }

                            dragStart = null
                            dragCurrent = null
                        }
                    )
                }
            }
    ) {

        val scaleX = size.width / state.boardWidth
        val scaleY = size.height / state.boardHeight

        fun toScreen(x: Float, y: Float): Offset {
            return Offset(x * scaleX, y * scaleY)
        }

        // 🪵 Board background
        drawRect(Color(0xFFD7A86E))

        // 🎯 Pockets
        val pocketRadius = 40f
        val pockets = listOf(
            Offset(0f, 0f),
            Offset(size.width, 0f),
            Offset(0f, size.height),
            Offset(size.width, size.height)
        )

        pockets.forEach {
            drawCircle(
                color = Color.Black,
                radius = pocketRadius,
                center = it
            )
        }

        // 🔴 Center circle
        drawCircle(
            color = Color.Red.copy(alpha = 0.3f),
            radius = 80f,
            center = center
        )

        // ⚫ Draw discs
        state.discs.forEach { disc ->
            if (!disc.pocketed) {

                val pos = toScreen(disc.x, disc.y)

                val color = when (disc.type) {
                    DiscType.BLACK -> Color.Black
                    DiscType.WHITE -> Color.White
                    DiscType.QUEEN -> Color.Red
                    DiscType.STRIKER -> Color(0xFF2196F3)
                }

                drawCircle(
                    color = color,
                    radius = disc.radius * scaleX,
                    center = pos
                )
            }
        }

        // 🎯 Aim line
        if (dragStart != null && dragCurrent != null) {

            drawLine(
                color = Color.Yellow,
                start = dragStart!!,
                end = dragCurrent!!,
                strokeWidth = 6f
            )
        }
    }
}
