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

            // Draw home areas with borders
            fun drawHomeArea(color: Color, topLeftX: Float, topLeftY: Float) {
                // Main home area
                drawRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(topLeftX, topLeftY),
                    size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6)
                )
                // Border
                drawRect(
                    color = darkGray,
                    topLeft = Offset(topLeftX, topLeftY),
                    size = androidx.compose.ui.geometry.Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )
                // Inner safe area
                drawRect(
                    color = color.copy(alpha = 0.6f),
                    topLeft = Offset(topLeftX + squareSize, topLeftY + squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize * 4, squareSize * 4),
                    cornerRadius = CornerRadius(squareSize * 0.2f)
                )
                drawRect(
                    color = white,
                    topLeft = Offset(topLeftX + squareSize, topLeftY + squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize * 4, squareSize * 4),
                    style = Stroke(width = 2f),
                    cornerRadius = CornerRadius(squareSize * 0.2f)
                )
            }

            // Draw all home areas
            drawHomeArea(green, 0f, 0f)                    // Top-left (Green)
            drawHomeArea(red, squareSize * 9, 0f)          // Top-right (Red)
            drawHomeArea(yellow, 0f, squareSize * 9)       // Bottom-left (Yellow)
            drawHomeArea(blue, squareSize * 9, squareSize * 9) // Bottom-right (Blue)

            // Draw center area
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

            // Draw center triangular paths (home stretch)
            val center = Offset(squareSize * 7.5f, squareSize * 7.5f)
            drawTriangle(center, Offset(squareSize * 6, squareSize * 6), Offset(squareSize * 9, squareSize * 6), red.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 9, squareSize * 6), Offset(squareSize * 9, squareSize * 9), blue.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 9, squareSize * 9), Offset(squareSize * 6, squareSize * 9), yellow.copy(alpha = 0.8f))
            drawTriangle(center, Offset(squareSize * 6, squareSize * 9), Offset(squareSize * 6, squareSize * 6), green.copy(alpha = 0.8f))

            // Draw center star/goal
            drawStar(center, squareSize * 0.8f, Color(0xFFFFD700))

            // Draw complete game path with proper Ludo board layout
            fun drawGameSquare(x: Int, y: Int, color: Color, isSpecial: Boolean = false, isStart: Boolean = false) {
                val squareX = x * squareSize
                val squareY = y * squareSize
                
                // Background
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
                
                // Border
                drawRect(
                    color = darkGray,
                    topLeft = Offset(squareX, squareY),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = Stroke(width = 1.5f)
                )
                
                // Special square marker
                if (isSpecial && !isStart) {
                    drawCircle(
                        color = Color.Gray,
                        radius = squareSize * 0.1f,
                        center = Offset(squareX + squareSize/2, squareY + squareSize/2)
                    )
                }
            }

            // Draw the main path (52 squares total in Ludo)
            // Bottom row (left to right) - positions 1-6
            for (i in 0..5) {
                val isYellowStart = i == 1
                drawGameSquare(1+i, 6, if (isYellowStart) yellow else lightGray, false, isYellowStart)
            }
            
            // Right column (bottom to top) - positions 7-12
            for (i in 0..5) {
                drawGameSquare(7, 5-i, lightGray)
            }
            
            // Top-right corner and top row - positions 13-18
            for (i in 0..5) {
                val isRedStart = i == 1
                drawGameSquare(8+i, 0, if (isRedStart) red else lightGray, false, isRedStart)
            }
            
            // Right column (top to bottom) - positions 19-24
            for (i in 0..5) {
                drawGameSquare(14, 1+i, lightGray)
            }
            
            // Bottom-right and bottom row - positions 25-30
            for (i in 0..5) {
                val isBlueStart = i == 1
                drawGameSquare(13-i, 8, if (isBlueStart) blue else lightGray, false, isBlueStart)
            }
            
            // Left column (bottom to top) - positions 31-36
            for (i in 0..5) {
                drawGameSquare(7, 9+i, lightGray)
            }
            
            // Bottom-left and left column - positions 37-42
            for (i in 0..5) {
                val isGreenStart = i == 1
                drawGameSquare(6-i, 14, if (isGreenStart) green else lightGray, false, isGreenStart)
            }
            
            // Left column (top to bottom) - positions 43-48
            for (i in 0..5) {
                drawGameSquare(0, 13-i, lightGray)
            }
            
            // Top-left corner and remaining - positions 49-52
            for (i in 0..3) {
                drawGameSquare(1+i, 7, lightGray)
            }

            // Draw home stretch paths (colored paths leading to center)
            // Green home stretch (horizontal, left to right)
            for (i in 1..5) {
                drawGameSquare(1+i, 7, green.copy(alpha = 0.6f))
            }
            
            // Red home stretch (vertical, top to bottom)
            for (i in 1..5) {
                drawGameSquare(8, 1+i, red.copy(alpha = 0.6f))
            }
            
            // Yellow home stretch (horizontal, right to left)
            for (i in 1..5) {
                drawGameSquare(13-i, 8, yellow.copy(alpha = 0.6f))
            }
            
            // Blue home stretch (vertical, bottom to top)
            for (i in 1..5) {
                drawGameSquare(7, 13-i, blue.copy(alpha = 0.6f))
            }

            // Draw starting positions with enhanced visibility
            fun drawStartPosition(x: Int, y: Int, color: Color) {
                val centerX = x * squareSize + squareSize/2
                val centerY = y * squareSize + squareSize/2
                
                // Outer ring
                drawCircle(
                    color = color,
                    radius = squareSize * 0.4f,
                    center = Offset(centerX, centerY),
                    style = Fill
                )
                
                // Inner ring
                drawCircle(
                    color = white,
                    radius = squareSize * 0.3f,
                    center = Offset(centerX, centerY),
                    style = Fill
                )
                
                // Center dot
                drawCircle(
                    color = color,
                    radius = squareSize * 0.1f,
                    center = Offset(centerX, centerY),
                    style = Fill
                )
            }

            // Mark starting positions (corrected positions)
            drawStartPosition(2, 6, yellow)   // Yellow start
            drawStartPosition(9, 0, red)      // Red start  
            drawStartPosition(12, 8, blue)    // Blue start
            drawStartPosition(5, 14, green)   // Green start

            // Draw player tokens in home areas
            fun drawPlayerTokens(color: Color, homeTopLeftX: Float, homeTopLeftY: Float) {
                val tokenRadius = squareSize * 0.35f
                val positions = listOf(
                    Offset(homeTopLeftX + squareSize * 2, homeTopLeftY + squareSize * 2),
                    Offset(homeTopLeftX + squareSize * 4, homeTopLeftY + squareSize * 2),
                    Offset(homeTopLeftX + squareSize * 2, homeTopLeftY + squareSize * 4),
                    Offset(homeTopLeftX + squareSize * 4, homeTopLeftY + squareSize * 4)
                )

                positions.forEach { position ->
                    // Token shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = tokenRadius,
                        center = Offset(position.x + 3f, position.y + 3f)
                    )
                    
                    // Main token body
                    drawCircle(
                        color = color,
                        radius = tokenRadius,
                        center = position
                    )
                    
                    // Token highlight
                    drawCircle(
                        color = color.copy(alpha = 0.7f),
                        radius = tokenRadius * 0.8f,
                        center = position
                    )
                    
                    // Token border
                    drawCircle(
                        color = darkGray,
                        radius = tokenRadius,
                        center = position,
                        style = Stroke(width = 2f)
                    )
                    
                    // Inner highlight
                    drawCircle(
                        color = white.copy(alpha = 0.6f),
                        radius = tokenRadius * 0.3f,
                        center = Offset(position.x - tokenRadius * 0.2f, position.y - tokenRadius * 0.2f)
                    )
                }
            }

            // Draw all player tokens
            drawPlayerTokens(green, 0f, 0f)                    // Green tokens
            drawPlayerTokens(red, squareSize * 9, 0f)          // Red tokens
            drawPlayerTokens(yellow, 0f, squareSize * 9)       // Yellow tokens
            drawPlayerTokens(blue, squareSize * 9, squareSize * 9) // Blue tokens

            // Draw safe zone markers
            fun drawSafeZone(x: Int, y: Int, color: Color) {
                val centerX = x * squareSize + squareSize/2
                val centerY = y * squareSize + squareSize/2
                
                // Draw star shape for safe zones
                drawStar(Offset(centerX, centerY), squareSize * 0.2f, color)
            }

            // Mark safe zones (starting positions and safe squares)
            drawSafeZone(2, 6, yellow)    // Yellow start (safe)
            drawSafeZone(9, 0, red)       // Red start (safe)
            drawSafeZone(12, 8, blue)     // Blue start (safe)
            drawSafeZone(5, 14, green)    // Green start (safe)
            
            // Additional safe zones around the board (every 8th square)
            drawSafeZone(0, 8, Color.Gray)    // Safe zone
            drawSafeZone(6, 0, Color.Gray)    // Safe zone
            drawSafeZone(14, 6, Color.Gray)   // Safe zone
            drawSafeZone(8, 14, Color.Gray)   // Safe zone

            // Draw directional arrows on the path
            fun drawPathArrow(x: Int, y: Int, direction: String, color: Color = Color.Gray) {
                val centerX = x * squareSize + squareSize/2
                val centerY = y * squareSize + squareSize/2
                val arrowSize = squareSize * 0.15f
                
                val path = Path()
                when (direction) {
                    "up" -> {
                        path.moveTo(centerX, centerY - arrowSize)
                        path.lineTo(centerX - arrowSize/2, centerY + arrowSize/2)
                        path.lineTo(centerX + arrowSize/2, centerY + arrowSize/2)
                    }
                    "down" -> {
                        path.moveTo(centerX, centerY + arrowSize)
                        path.lineTo(centerX - arrowSize/2, centerY - arrowSize/2)
                        path.lineTo(centerX + arrowSize/2, centerY - arrowSize/2)
                    }
                    "left" -> {
                        path.moveTo(centerX - arrowSize, centerY)
                        path.lineTo(centerX + arrowSize/2, centerY - arrowSize/2)
                        path.lineTo(centerX + arrowSize/2, centerY + arrowSize/2)
                    }
                    "right" -> {
                        path.moveTo(centerX + arrowSize, centerY)
                        path.lineTo(centerX - arrowSize/2, centerY - arrowSize/2)
                        path.lineTo(centerX - arrowSize/2, centerY + arrowSize/2)
                    }
                }
                path.close()
                
                drawPath(path, color)
            }

            // Add directional arrows to show movement flow
            drawPathArrow(3, 6, "right", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(7, 3, "up", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(11, 0, "right", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(14, 3, "down", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(11, 8, "left", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(7, 11, "down", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(3, 14, "left", Color.Gray.copy(alpha = 0.6f))
            drawPathArrow(0, 11, "up", Color.Gray.copy(alpha = 0.6f))
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

fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    
    for (i in 0 until 8) {
        val angle = (i * 45 - 90) * Math.PI / 180
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * kotlin.math.cos(angle)).toFloat()
        val y = center.y + (r * kotlin.math.sin(angle)).toFloat()
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    
    drawPath(path = path, color = color)
    drawPath(path = path, color = Color.Black, style = Stroke(width = 2f))
}

