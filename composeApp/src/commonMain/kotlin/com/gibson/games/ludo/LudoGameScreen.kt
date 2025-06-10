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
import androidx.compose.ui.graphics.drawscope.DrawScope
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

            // Define enhanced colors
            val red = Color(0xFFE53E3E)
            val green = Color(0xFF38A169)
            val blue = Color(0xFF3182CE)
            val yellow = Color(0xFFD69E2E)
            val lightGray = Color(0xFFF7FAFC)
            val darkGray = Color(0xFF2D3748)
            val white = Color.White

            // Draw board background
            drawRect(
                color = lightGray,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(boardSize, boardSize)
            )

            // Draw outer border
            drawRect(
                color = darkGray,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(boardSize, boardSize),
                style = Stroke(width = 4f)
            )

            // Function to draw each home area
            fun drawHomeArea(color: Color, topLeftX: Float, topLeftY: Float) {
                drawRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(topLeftX, topLeftY),
                    size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6)
                )
                drawRect(
                    color = darkGray,
                    topLeft = Offset(topLeftX, topLeftY),
                    size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )
                drawRoundRect(
                    color = color.copy(alpha = 0.6f),
                    topLeft = Offset(topLeftX + squareSize, topLeftY + squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize * 4, squareSize * 4),
                    cornerRadius = CornerRadius(squareSize * 0.2f)
                )
                drawRoundRect(
                    color = white,
                    topLeft = Offset(topLeftX + squareSize, topLeftY + squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize * 4, squareSize * 4),
                    style = Stroke(width = 2f),
                    cornerRadius = CornerRadius(squareSize * 0.2f)
                )
            }

            // Draw all home areas
            drawHomeArea(green, 0f, 0f)
            drawHomeArea(red, squareSize * 9, 0f)
            drawHomeArea(yellow, 0f, squareSize * 9)
            drawHomeArea(blue, squareSize * 9, squareSize * 9)

            // Center area
            drawRect(
                color = white,
                topLeft = Offset(squareSize * 6, squareSize * 6),
                size = androidx.compose.ui.geometry.Size(squareSize * 3, squareSize * 3)
            )
            drawRect(
                color = darkGray,
                topLeft = Offset(squareSize * 6, squareSize * 6),
                size = androidx.compose.ui.geometry.Size(squareSize * 3, squareSize * 3),
                style = Stroke(width = 3f)
            )

            val center = Offset(squareSize * 7.5f, squareSize * 7.5f)
            drawTriangle(center, Offset(squareSize * 6, squareSize * 6), Offset(squareSize * 9, squareSize * 6), red.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 9, squareSize * 6), Offset(squareSize * 9, squareSize * 9), blue.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 9, squareSize * 9), Offset(squareSize * 6, squareSize * 9), yellow.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 6, squareSize * 9), Offset(squareSize * 6, squareSize * 6), green.copy(alpha = 0.8f))

            drawStar(center, squareSize * 0.8f, Color(0xFFFFD700))

            fun drawGameSquare(x: Int, y: Int, color: Color, isSpecial: Boolean = false, isStart: Boolean = false) {
                val squareX = x * squareSize
                val squareY = y * squareSize

                val bgColor = when {
                    isStart -> color.copy(alpha = 0.9f)
                    isSpecial -> color.copy(alpha = 0.6f)
                    else -> white
                }

                drawRect(
                    color = bgColor,
                    topLeft = Offset(squareX, squareY),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )

                drawRect(
                    color = darkGray,
                    topLeft = Offset(squareX, squareY),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = Stroke(width = 1.5f)
                )
            }

            val mainPathCoordinates = listOf(
                Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5), Pair(5, 6),
                Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6), Pair(0, 6), Pair(0, 7),
                Pair(0, 8), Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8),
                Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), Pair(6, 14),
                Pair(7, 14), Pair(8, 14), Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10),
                Pair(8, 9), Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8),
                Pair(14, 8), Pair(14, 7), Pair(14, 6), Pair(13, 6), Pair(12, 6), Pair(11, 6),
                Pair(10, 6), Pair(9, 6), Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2),
                Pair(8, 1), Pair(8, 0), Pair(7, 0), Pair(6, 0), Pair(6, 1)
            )

            val startSquares = setOf(
                Pair(6, 1), Pair(1, 8), Pair(8, 13), Pair(13, 6)
            )

            val safeSquares = setOf(
                Pair(6, 1), Pair(1, 8), Pair(8, 13), Pair(13, 6),
                Pair(5, 6), Pair(6, 9), Pair(9, 8), Pair(8, 5),
                Pair(0, 6), Pair(0, 8), Pair(6, 0), Pair(8, 0),
                Pair(14, 6), Pair(14, 8), Pair(6, 14), Pair(8, 14),
                Pair(0, 7), Pair(7, 0), Pair(7, 14), Pair(14, 7)
            )

            for (coord in mainPathCoordinates) {
                val (x, y) = coord
                val isStart = startSquares.contains(coord)
                val isSpecial = safeSquares.contains(coord)
                val colorForStart = when (coord) {
                    Pair(6, 1) -> yellow
                    Pair(1, 8) -> green
                    Pair(8, 13) -> blue
                    Pair(13, 6) -> red
                    else -> lightGray
                }
                drawGameSquare(x, y, colorForStart, isSpecial, isStart)
            }

            // Home stretches
            for (i in 1..5) drawGameSquare(1 + i, 7, green.copy(alpha = 0.6f))
            for (i in 1..5) drawGameSquare(8, 1 + i, red.copy(alpha = 0.6f))
            for (i in 1..5) drawGameSquare(13 - i, 8, yellow.copy(alpha = 0.6f))
            for (i in 1..5) drawGameSquare(7, 13 - i, blue.copy(alpha = 0.6f))
        }
    }
}

// Draw a filled triangle between 3 points
fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

// Draw a simple 5-pointed star centered at `center` with given `radius`
fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val angle = Math.PI / 5
    for (i in 0..9) {
        val r = if (i % 2 == 0) radius else radius * 0.4f
        val x = center.x + (r * Math.cos(i * angle - Math.PI / 2)).toFloat()
        val y = center.y + (r * Math.sin(i * angle - Math.PI / 2)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}
