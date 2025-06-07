package com.gibson.games.gamehub.subway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.gibson.games.core.Vector2
import com.gibson.games.engine.CollisionManager
import com.gibson.games.engine.ObstacleManager
import com.gibson.games.engine.PlayerController
import com.gibson.games.model.Player
import kotlinx.coroutines.delay

@Composable
fun SubwayScreen() {
    val player = remember { Player(position = Vector2(400f, 1600f)) }
    val playerController = remember { PlayerController(player) }
    val obstacleManager = remember { ObstacleManager() }
    val collisionManager = remember { CollisionManager() }

    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!gameOver) {
            obstacleManager.update()
            gameOver = collisionManager.checkCollisions(player, obstacleManager.getObstacles())
            delay(16L) // ~60 FPS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val (_, dy) = dragAmount
                    val (_, y0) = change.position

                    if (dy < -30) playerController.onSwipeUp()
                    if (dy > 30) playerController.onSwipeDown()

                    val (dx, _) = dragAmount
                    if (dx < -30) playerController.onSwipeLeft()
                    if (dx > 30) playerController.onSwipeRight()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw player
            drawRect(
                color = Color.Green,
                topLeft = androidx.compose.ui.geometry.Offset(player.position.x, player.position.y),
                size = androidx.compose.ui.geometry.Size(80f, 80f)
            )

            // Draw obstacles
            obstacleManager.getObstacles().forEach { obstacle ->
                drawRect(
                    color = Color.Red,
                    topLeft = androidx.compose.ui.geometry.Offset(obstacle.position.x, obstacle.position.y),
                    size = androidx.compose.ui.geometry.Size(obstacle.width, obstacle.height)
                )
            }
        }
    }

    if (gameOver) {
        // Optional: You can render a "Game Over" overlay
    }
}
