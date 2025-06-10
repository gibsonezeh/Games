package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.checkerframework.checker.units.qual.g
import kotlin.math.cos
import kotlin.math.sin

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
            val starGold = Color(0xFFFFD700)

            // --- 1. Draw Board Background and Grid ---
            drawRect(
                color = lightGray,
                size = Size(boardSize, boardSize)
            )
            drawRect(
                color = darkGray,
                size = Size(boardSize, boardSize),
                style = Stroke(width = 4f)
            )

            // --- 2. Draw Home Areas for Each Player ---
            fun drawHomeArea(color: Color, topLeftX: Float, topLeftY: Float) {
                val homeAreaSize = squareSize * 6
                // Main home area background
                drawRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(topLeftX, topLeftY),
                    size = Size(homeAreaSize, homeAreaSize)
                )
                // Border
                drawRect(
                    color = darkGray,
                    topLeft = Offset(topLeftX, topLeftY),
                    size = Size(homeAreaSize, homeAreaSize),
                    style = Stroke(width = 3f)
                )
                // Inner white area for tokens
                drawRoundRect(
                    color = white,
                    topLeft = Offset(topLeftX + squareSize, topLeftY + squareSize),
                    size = Size(squareSize * 4, squareSize * 4),
                    cornerRadius = CornerRadius(squareSize * 0.2f)
                )
            }

            drawHomeArea(green, 0f, 0f)                          // Top-left (Green)
            drawHomeArea(red, squareSize * 9, 0f)            // Top-right (Red)
            drawHomeArea(yellow, 0f, squareSize * 9)         // Bottom-left (Yellow)
            drawHomeArea(blue, squareSize * 9, squareSize * 9)   // Bottom-right (Blue)

            // --- 3. Draw Center Goal Area ---
            val centerTopLeft = Offset(squareSize * 6, squareSize * 6)
            val centerSize = Size(squareSize * 3, squareSize * 3)
            
            // Draw the triangular home entries
            val center = Offset(boardSize / 2, boardSize / 2)
            drawTriangle(center, Offset(centerTopLeft.x, centerTopLeft.y), Offset(centerTopLeft.x + centerSize.width, centerTopLeft.y), red)
            drawTriangle(center, Offset(centerTopLeft.x + centerSize.width, centerTopLeft.y), Offset(centerTopLeft.x + centerSize.width, centerTopLeft.y + centerSize.height), blue)
            drawTriangle(center, Offset(centerTopLeft.x + centerSize.width, centerTopLeft.y + centerSize.height), Offset(centerTopLeft.x, centerTopLeft.y + centerSize.height), yellow)
            drawTriangle(center, Offset(centerTopLeft.x, centerTopLeft.y + centerSize.height), Offset(centerTopLeft.x, centerTopLeft.y), green)
            
            // Draw the border for the center goal area
             drawRect(
                color = darkGray,
                topLeft = centerTopLeft,
                size = centerSize,
                style = Stroke(width = 3f)
            )

            // --- 4. Draw Game Path Tiles ---
            fun drawTile(gridX: Int, gridY: Int, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(gridX * squareSize, gridY * squareSize),
                    size = Size(squareSize, squareSize)
                )
                drawRect(
                    color = darkGray,
                    topLeft = Offset(gridX * squareSize, gridY * squareSize),
                    size = Size(squareSize, squareSize),
                    style = Stroke(width = 1.5f)
                )
            }
            
            // Draw the main cross-shaped path
            for (i in 0 until 6) {
                // Top arm
                drawTile(6, i, white)
                drawTile(7, i, if (i > 0) red else white) // Red home path
                drawTile(8, i, white)

                // Bottom arm
                drawTile(6, i + 9, white)
                drawTile(7, i + 9, if (i < 5) blue else white) // Blue home path
                drawTile(8, i + 9, white)
                
                // Left arm
                drawTile(i, 6, white)
                drawTile(i, 7, if (i > 0) green else white) // Green home path
                drawTile(i, 8, white)

                // Right arm
                drawTile(i + 9, 6, white)
                drawTile(i + 9, 7, if (i < 5) yellow else white) // Yellow home path
                drawTile(i + 9, 8, white)
            }
            
            // Color the starting tiles
            drawTile(1, 6, green) // Green start
            drawTile(8, 1, red)   // Red start
            drawTile(13, 8, blue) // Blue start
            drawTile(6, 13, yellow) // Yellow start


            // --- 5. Draw Safe Zone Stars ---
            val safeZones = listOf(
                Offset(1f, 6f), Offset(6f, 1f), // Green & Top Safe
                Offset(8f, 1f), Offset(13f, 6f), // Red & Right Safe
                Offset(6f, 13f), Offset(1f, 8f) , // Yellow & Left Safe
                Offset(13f, 8f), Offset(8f, 13f) // Blue & Bottom Safe

            )
            
            safeZones.forEach { (gridX, gridY) ->
                drawStar(
                    center = Offset(gridX * squareSize + squareSize / 2, gridY * squareSize + squareSize / 2),
                    radius = squareSize * 0.3f,
                    color = starGold
                )
            }
            

            // --- 6. Draw Player Tokens in Home ---
            fun drawPlayerToken(center: Offset, color: Color) {
                val tokenRadius = squareSize * 0.4f
                // Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.2f),
                    radius = tokenRadius,
                    center = center.copy(x = center.x + 3f, y = center.y + 3f)
                )
                // Main body
                drawCircle(color = color, radius = tokenRadius, center = center)
                // Border
                drawCircle(color = darkGray, radius = tokenRadius, center = center, style = Stroke(2f))
                // Highlight
                drawCircle(
                    color = white.copy(alpha = 0.5f),
                    radius = tokenRadius * 0.25f,
                    center = center.copy(x = center.x - tokenRadius * 0.2f, y = center.y - tokenRadius * 0.2f)
                )
            }

            fun drawHomeTokens(homeColor: Color, homeGridTopLeft: Offset) {
                val positions = listOf(
                    homeGridTopLeft + Offset(2.5f, 2.5f), homeGridTopLeft + Offset(4.5f, 2.5f),
                    homeGridTopLeft + Offset(2.5f, 4.5f), homeGridTopLeft + Offset(4.5f, 4.5f)
                )
                positions.forEach { gridPos ->
                    drawPlayerToken(
                        center = Offset(gridPos.x * squareSize, gridPos.y * squareSize),
                        color = homeColor
                    )
                }
            }

            drawHomeTokens(green, Offset(0f, 0f))
            drawHomeTokens(red, Offset(8f, 0f))
            drawHomeTokens(blue, Offset(8f, 8f))
            drawHomeTokens(yellow, Offset(0f, 8f))

        }
    }
}

/**
 * Draws a triangle path given a center and two other points.
 */
fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Draws a 5-pointed star for safe zones.
 */
fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    var angle = -90.0
    
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(Math.toRadians(angle))).toFloat()
        val y = center.y + (r * sin(Math.toRadians(angle))).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += 36.0
    }
    path.close()
    
    drawPath(path = path, color = color)
    drawPath(path = path, color = Color.Black.copy(alpha = 0.8f), style = Stroke(width = 2f))
}

