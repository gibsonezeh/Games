package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

            // Draw home squares
            drawRect(green, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(red, topLeft = Offset(squareSize * 9, 0f), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(yellow, topLeft = Offset(0f, squareSize * 9), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))
            drawRect(blue, topLeft = Offset(squareSize * 9, squareSize * 9), size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6))

            // Center square (goal)
            drawRect(Color.White, topLeft = Offset(squareSize * 6, squareSize * 6), size = androidx.compose.ui.geometry.Size(squareSize * 3, squareSize * 3))

            // Center triangle paths
            val trianglePath = Path().apply {
                moveTo(squareSize * 6, squareSize * 6)
                lineTo(squareSize * 7.5f, squareSize * 7.5f)
                lineTo(squareSize * 9, squareSize * 6)
                close()
            }
            drawPath(trianglePath, red)

            trianglePath.reset()
            trianglePath.moveTo(squareSize * 9, squareSize * 6)
            trianglePath.lineTo(squareSize * 7.5f, squareSize * 7.5f)
            trianglePath.lineTo(squareSize * 9, squareSize * 9)
            trianglePath.close()
            drawPath(trianglePath, blue)

            trianglePath.reset()
            trianglePath.moveTo(squareSize * 9, squareSize * 9)
            trianglePath.lineTo(squareSize * 7.5f, squareSize * 7.5f)
            trianglePath.lineTo(squareSize * 6, squareSize * 9)
            trianglePath.close()
            drawPath(trianglePath, yellow)

            trianglePath.reset()
            trianglePath.moveTo(squareSize * 6, squareSize * 9)
            trianglePath.lineTo(squareSize * 7.5f, squareSize * 7.5f)
            trianglePath.lineTo(squareSize * 6, squareSize * 6)
            trianglePath.close()
            drawPath(trianglePath, green)

            // Function to draw player tokens (4 per home)
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
                            center = Offset(centerX, centerY),
                            style = Fill
                        )
                        drawCircle( // outline
                            color = Color.Black,
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // Draw tokens
            drawTokens(green, 0f, 0f)
            drawTokens(red, squareSize * 9, 0f)
            drawTokens(yellow, 0f, squareSize * 9)
            drawTokens(blue, squareSize * 9, squareSize * 9)

            // Draw path squares (main track)
            fun drawPathRow(startX: Int, startY: Int, dx: Int, dy: Int, count: Int, colorIndex: Int? = null) {
                repeat(count) {
                    val x = (startX + dx * it) * squareSize
                    val y = (startY + dy * it) * squareSize
                    val pathColor = when (colorIndex) {
                        0 -> green
                        1 -> red
                        2 -> yellow
                        3 -> blue
                        else -> Color.LightGray
                    }
                    drawRect(
                        color = pathColor,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Green side
            drawPathRow(6, 0, 0, 1, 5)
            drawPathRow(6, 6, -1, 0, 5)
            drawPathRow(1, 6, 0, 1, 1, 0)

            // Red side
            drawPathRow(8, 0, 0, 1, 5)
            drawPathRow(9, 6, 1, 0, 5)
            drawPathRow(13, 6, 0, 1, 1, 1)

            // Yellow side
            drawPathRow(6, 14, 0, -1, 5)
            drawPathRow(6, 8, -1, 0, 5)
            drawPathRow(1, 8, 0, -1, 1, 2)

            // Blue side
            drawPathRow(8, 14, 0, -1, 5)
            drawPathRow(9, 8, 1, 0, 5)
            drawPathRow(13, 8, 0, -1, 1, 3)
        }
    }
}
