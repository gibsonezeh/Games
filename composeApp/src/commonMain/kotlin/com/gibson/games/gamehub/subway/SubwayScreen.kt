package com.gibson.games.gamehub.subway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.gibson.games.core.SwipeDirection
import com.gibson.games.core.SwipeInputHandler
import com.gibson.games.engine.PlayerController
import com.gibson.games.engine.RoadGenerator
import com.gibson.games.model.Player

@Composable
fun SubwayScreen() {
    val player = remember { Player() }
    val playerController = remember { PlayerController(player) }
    val roadGenerator = remember { RoadGenerator() }

    // Register swipe input listener
    DisposableEffect(Unit) {
        SwipeInputHandler.registerListener { direction ->
            playerController.onSwipe(direction)
        }
        onDispose {
            SwipeInputHandler.unregisterListener()
        }
    }

    // Game loop (simplified)
    LaunchedEffect(Unit) {
        while (true) {
            playerController.update()
            roadGenerator.update()
            // Delay to simulate frame tick (~60fps)
            kotlinx.coroutines.delay(16)
        }
    }

    // Render loop
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the road
            val roadSegments = roadGenerator.getRoadSegments()
            roadSegments.forEach { segment ->
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(segment.x, segment.y),
                    size = segment.size
                )
            }

            // Draw the player
            drawCircle(
                color = Color.Cyan,
                center = Offset(player.position.x, player.position.y),
                radius = 40f
            )
        }
    }
}
