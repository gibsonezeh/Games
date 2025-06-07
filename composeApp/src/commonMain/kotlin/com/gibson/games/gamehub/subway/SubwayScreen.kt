package com.gibson.games.gamehub.subway

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
import com.gibson.games.ui.GameCanvas
import kotlinx.coroutines.delay

@Composable
fun SubwayScreen() {
    val player = remember { Player(position = Vector2(400f, 1600f)) }
    val playerController = remember { PlayerController(player) }
    val obstacleManager = remember { ObstacleManager() }
    val collisionManager = remember { CollisionManager() }

    var gameOver by remember { mutableStateOf(false) }

    // Game loop
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
                    val (dx, _) = dragAmount

                    if (dy < -30) playerController.onSwipeUp()
                    if (dy > 30) playerController.onSwipeDown()
                    if (dx < -30) playerController.onSwipeLeft()
                    if (dx > 30) playerController.onSwipeRight()
                }
            }
    ) {
        GameCanvas(
            player = player,
            obstacles = obstacleManager.getObstacles()
        )

        if (gameOver) {
            // Optional: Show "Game Over" overlay or button to restart
        }
    }
}
