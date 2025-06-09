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

            // === Add player token positions ===
            fun drawTokens(color: Color, topLeftX: Float, topLeftY: Float) {
                val radius = squareSize * 0.6f
                val spacing = squareSize * 1.5f

                for (i in 0..1) {
                    for (j in 0..1) {
                        val centerX = topLeftX + j * spacing + squareSize
                        val centerY = topLeftY + i * spacing + squareSize
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                            style = Fill
                        )
                        drawCircle( // optional black outline
                            color = Color.Black,
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            drawTokens(green, topLeftX = 0f, topLeftY = 0f) // Green Home
            drawTokens(red, topLeftX = squareSize * 9, topLeftY = 0f) // Red Home
            drawTokens(yellow, topLeftX = 0f, topLeftY = squareSize * 9) // Yellow Home
            drawTokens(blue, topLeftX = squareSize * 9, topLeftY = squareSize * 9) // Blue Home
        }
    }
}
