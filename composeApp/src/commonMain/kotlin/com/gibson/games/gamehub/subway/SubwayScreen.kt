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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.gibson.games.core.SwipeDirection

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
                    val (dx, dy) = dragAmount
                    if (dy < -30) playerController.onSwipe(SwipeDirection.UP)
                    if (dy > 30) playerController.onSwipe(SwipeDirection.DOWN)
                    if (dx < -30) playerController.onSwipe(SwipeDirection.LEFT)
                    if (dx > 30) playerController.onSwipe(SwipeDirection.RIGHT)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw player
            drawRect(
                color = Color.Green,
                topLeft = Offset(player.position.x, player.position.y),
                size = Size(player.width, player.height)
            )

            // Draw obstacles
            obstacleManager.getObstacles().forEach { obstacle ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(obstacle.position.x, obstacle.position.y),
                    size = Size(obstacle.width, obstacle.height)
                )
            }
        }
    }

    if (gameOver) {
        // TODO: Add Game Over overlay
    }
}
