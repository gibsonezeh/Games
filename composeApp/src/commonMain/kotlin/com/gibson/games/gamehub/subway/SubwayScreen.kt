package com.gibson.games.gamehub.subway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.gibson.games.platform.input.detectSwipe // Import the top-level extension function

@Composable
fun SubwayScreen() {
    val scope = rememberCoroutineScope()
    val subwayLogic = remember { SubwayLogic(scope) }
    val gameState = subwayLogic.gameState.value

    Box(modifier = Modifier.fillMaxSize().background(SubwayAssets.SkyColor)) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectSwipe { direction -> // Call the extension function directly
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


