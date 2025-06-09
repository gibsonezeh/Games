package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LudoGameScreen(onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size.minDimension
            val squareSize = boardSize / 15f

            // Define colors
            val red = Color.Red
            val green = Color(0xFF388E3C)
            val blue = Color(0xFF1976D2)
            val yellow = Color(0xFFFFEB3B)

            // Draw base squares (home areas)
            drawRect(green, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(red, topLeft = androidx.compose.ui.geometry.Offset(squareSize * 9, 0f), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(yellow, topLeft = androidx.compose.ui.geometry.Offset(0f, squareSize * 9), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(blue, topLeft = androidx.compose.ui.geometry.Offset(squareSize * 9, squareSize * 9), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))

            // Draw center goal area
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(squareSize * 6, squareSize * 6)
                    lineTo(squareSize * 9, squareSize * 6)
                    lineTo(squareSize * 9, squareSize * 9)
                    lineTo(squareSize * 6, squareSize * 9)
                    close()
                },
                color = Color.White
            )

            drawLine(red, start = androidx.compose.ui.geometry.Offset(squareSize * 6, squareSize * 6), end = androidx.compose.ui.geometry.Offset(squareSize * 9, squareSize * 9), strokeWidth = 4f)
            drawLine(green, start = androidx.compose.ui.geometry.Offset(squareSize * 9, squareSize * 6), end = androidx.compose.ui.geometry.Offset(squareSize * 6, squareSize * 9), strokeWidth = 4f)

            // TODO: Add player positions (circles), arrow paths, and actual player images if needed
        }
    }
}
