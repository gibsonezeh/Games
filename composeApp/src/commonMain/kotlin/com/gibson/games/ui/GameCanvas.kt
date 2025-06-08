package com.gibson.games.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.gibson.games.core.GameState
import com.gibson.games.gamehub.subway.SubwayAssets

@Composable
fun GameCanvas(gameState: GameState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw road
        drawRect(
            color = SubwayAssets.RoadColor,
            topLeft = Offset(0f, size.height / 2),
            size = Size(size.width, size.height / 2)
        )

        // Draw player
        val playerScreenX = size.width / 2 + gameState.player.position.x
        val playerScreenY = size.height / 2 - gameState.player.position.y // Invert Y for screen coordinates
        drawRect(
            color = SubwayAssets.PlayerColor,
            topLeft = Offset(playerScreenX - gameState.player.size.x / 2, playerScreenY - gameState.player.size.y / 2),
            size = Size(gameState.player.size.x, gameState.player.size.y)
        )

        // Draw obstacles
        gameState.obstacles.forEach {
            val obstacleScreenX = size.width / 2 + it.position.x
            val obstacleScreenY = size.height / 2 - it.position.y // Invert Y for screen coordinates
            drawRect(
                color = SubwayAssets.ObstacleColor,
                topLeft = Offset(obstacleScreenX - it.size.x / 2, obstacleScreenY - it.size.y / 2),
                size = Size(it.size.x, it.size.y)
            )
        }
    }
}

