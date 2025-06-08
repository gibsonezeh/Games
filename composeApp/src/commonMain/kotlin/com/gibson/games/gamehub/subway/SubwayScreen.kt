package com.gibson.games.gamehub.subway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectSwipeGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gibson.games.core.SwipeDirection
import com.gibson.games.core.Vector2
import com.gibson.games.platform.input.AndroidSwipeDetector // Assuming this is the common interface

@Composable
fun SubwayScreen() {
    val scope = rememberCoroutineScope()
    val subwayLogic = remember { SubwayLogic(scope) }
    val gameState = subwayLogic.gameState.value

    val swipeDetector = remember { AndroidSwipeDetector() }

    Box(modifier = Modifier.fillMaxSize().background(SubwayAssets.SkyColor)) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            swipeDetector.detectSwipe(this) { direction ->
                subwayLogic.handleSwipe(direction)
            }
        }) {
            // Draw road
            drawRect(
                color = SubwayAssets.RoadColor,
                topLeft = Offset(0f, size.height / 2),
                size = androidx.compose.ui.geometry.Size(size.width, size.height / 2)
            )

            // Draw player
            val playerScreenX = size.width / 2 + gameState.player.position.x
            val playerScreenY = size.height / 2 - gameState.player.position.y // Invert Y for screen coordinates
            drawRect(
                color = SubwayAssets.PlayerColor,
                topLeft = Offset(playerScreenX - gameState.player.size.x / 2, playerScreenY - gameState.player.size.y / 2),
                size = androidx.compose.ui.geometry.Size(gameState.player.size.x, gameState.player.size.y)
            )

            // Draw obstacles
            gameState.obstacles.forEach {
                val obstacleScreenX = size.width / 2 + it.position.x
                val obstacleScreenY = size.height / 2 - it.position.y // Invert Y for screen coordinates
                drawRect(
                    color = SubwayAssets.ObstacleColor,
                    topLeft = Offset(obstacleScreenX - it.size.x / 2, obstacleScreenY - it.size.y / 2),
                    size = androidx.compose.ui.geometry.Size(it.size.x, it.size.y)
                )
            }
        }

        Column {
            Text(text = "Score: ${gameState.score}", color = Color.Black)
            if (gameState.isGameOver) {
                Text(text = "Game Over!", color = Color.Red)
            }
        }
    }
}

