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
            val center = Offset(squareSize * 7.5f, squareSize * 7.5f)
            drawTriangle(center, Offset(squareSize * 6, squareSize * 6), Offset(squareSize * 9, squareSize * 6), red)
            drawTriangle(center, Offset(squareSize * 9, squareSize * 6), Offset(squareSize * 9, squareSize * 9), blue)
            drawTriangle(center, Offset(squareSize * 9, squareSize * 9), Offset(squareSize * 6, squareSize * 9), yellow)
            drawTriangle(center, Offset(squareSize * 6, squareSize * 9), Offset(squareSize * 6, squareSize * 6), green)

            // Draw player tokens (2x2 layout)
            fun drawTokens(color: Color, topLeftX: Float, topLeftY: Float) {
                val radius = squareSize * 0.6f
                val spacing = squareSize * 1.5f
                for (i in 0..1) {
                    for (j in 0..1) {
                        val centerX = topLeftX + j * spacing + squareSize
                        val centerY = topLeftY + i * spacing + squareSize
                        drawCircle(color, radius = radius, center = Offset(centerX, centerY), style = Fill)
                        drawCircle(Color.Black, radius = radius, center = Offset(centerX, centerY), style = Stroke(2f))
                    }
                }
            }

            drawTokens(green, 0f, 0f)
            drawTokens(red, squareSize * 9, 0f)
            drawTokens(yellow, 0f, squareSize * 9)
            drawTokens(blue, squareSize * 9, squareSize * 9)

            // Draw main path
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

            // Green path
            drawPathRow(6, 0, 0, 1, 5)
            drawPathRow(6, 6, -1, 0, 5)
            drawPathRow(1, 6, 0, 1, 1, 0)

            // Red path
            drawPathRow(8, 0, 0, 1, 5)
            drawPathRow(9, 6, 1, 0, 5)
            drawPathRow(13, 6, 0, 1, 1, 1)

            // Yellow path
            drawPathRow(6, 14, 0, -1, 5)
            drawPathRow(6, 8, -1, 0, 5)
            drawPathRow(1, 8, 0, -1, 1, 2)

            // Blue path
            drawPathRow(8, 14, 0, -1, 5)
            drawPathRow(9, 8, 1, 0, 5)
            drawPathRow(13, 8, 0, -1, 1, 3)

            // Draw arrows on paths
            fun drawArrow(fromX: Int, fromY: Int, dx: Int, dy: Int, color: Color) {
                val cx = (fromX + 0.5f) * squareSize
                val cy = (fromY + 0.5f) * squareSize
                val arrowLength = squareSize * 0.4f
                val headSize = squareSize * 0.2f

                val tip = Offset(cx + dx * arrowLength, cy + dy * arrowLength)
                val base1 = Offset(cx + dy * headSize - dx * headSize, cy - dx * headSize - dy * headSize)
                val base2 = Offset(cx - dy * headSize - dx * headSize, cy + dx * headSize - dy * headSize)

                drawLine(color, Offset(cx, cy), tip, strokeWidth = 4f)
                drawPath(Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(base1.x, base1.y)
                    lineTo(base2.x, base2.y)
                    close()
                }, color)
            }

            // Arrow examples (can be extended)
            drawArrow(6, 0, 0, 1, green)
            drawArrow(6, 5, -1, 0, green)
            drawArrow(1, 6, 0, 1, green)

            drawArrow(8, 0, 0, 1, red)
            drawArrow(9, 5, 1, 0, red)
            drawArrow(13, 6, 0, 1, red)

            drawArrow(6, 14, 0, -1, yellow)
            drawArrow(6, 9, -1, 0, yellow)
            drawArrow(1, 8, 0, -1, yellow)

            drawArrow(8, 14, 0, -1, blue)
            drawArrow(9, 9, 1, 0, blue)
            drawArrow(13, 8, 0, -1, blue)
        }
    }
}

fun DrawScope.drawTriangle(center: Offset, p1: Offset, p2: Offset, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path = path, color = color)
}
