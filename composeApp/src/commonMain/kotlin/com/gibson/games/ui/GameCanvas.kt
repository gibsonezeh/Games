package com.gibson.games.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.gibson.games.model.GameEntity

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    player: GameEntity,
    obstacles: List<GameEntity>,
    backgroundColor: Color = Color.Black,
    playerColor: Color = Color.Green,
    obstacleColor: Color = Color.Red
) {
    Canvas(modifier = modifier) {
        drawRect(
            color = backgroundColor,
            topLeft = Offset.Zero,
            size = size
        )

        // Draw player
        drawRect(
            color = playerColor,
            topLeft = Offset(player.position.x, player.position.y),
            size = Size(player.width, player.height)
        )

        // Draw obstacles
        obstacles.forEach { obstacle ->
            drawRect(
                color = obstacleColor,
                topLeft = Offset(obstacle.position.x, obstacle.position.y),
                size = Size(obstacle.width, obstacle.height)
            )
        }
    }
}
