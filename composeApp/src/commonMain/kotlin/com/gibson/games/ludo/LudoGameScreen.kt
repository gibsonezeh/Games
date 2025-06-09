// commonMain/com/gibson/games/ludo/LudoGameScreen.kt

package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LudoGameScreen(onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val boardSize = size.minDimension
            val cellSize = boardSize / 15f

            // Draw grid
            for (i in 0..15) {
                drawLine(
                    color = Color.Black,
                    start = androidx.compose.ui.geometry.Offset(i * cellSize, 0f),
                    end = androidx.compose.ui.geometry.Offset(i * cellSize, boardSize),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.Black,
                    start = androidx.compose.ui.geometry.Offset(0f, i * cellSize),
                    end = androidx.compose.ui.geometry.Offset(boardSize, i * cellSize),
                    strokeWidth = 1f
                )
            }

            // Home zones
            val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)
            val positions = listOf(
                Rect(0f, 0f, cellSize * 6, cellSize * 6),                 // Red (Top-left)
                Rect(cellSize * 9, 0f, cellSize * 15, cellSize * 6),      // Green (Top-right)
                Rect(0f, cellSize * 9, cellSize * 6, cellSize * 15),      // Yellow (Bottom-left)
                Rect(cellSize * 9, cellSize * 9, cellSize * 15, cellSize * 15) // Blue (Bottom-right)
            )

            colors.zip(positions).forEach { (color, rect) ->
                drawRect(color, topLeft = rect.topLeft, size = rect.size)
                drawRect(Color.Black, topLeft = rect.topLeft, size = rect.size, style = Stroke(2f))
            }
        }
    }
}
