package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bird-themed Ludo game board screen with dice placeholders and complete functionality
 */
@Composable
fun LudoGameScreen(onExit: () -> Unit, gameRules: GameRules = GameRules()) {
    var showExitDialog by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    var boardState by remember { mutableStateOf(initializeGameState(gameRules)) }
    var diceRoll by remember { mutableStateOf<DiceRoll?>(null) }
    var isRolling by remember { mutableStateOf(false) }
    var selectedToken by remember { mutableStateOf<Token?>(null) }
    var movableTokens by remember { mutableStateOf<List<Token>>(emptyList()) }
    var gameMessage by remember { mutableStateOf("") }

    // Handle back navigation with confirmation dialog
    BackHandler {
        showExitDialog = true
    }

    // Update movable tokens when dice is rolled
    LaunchedEffect(diceRoll, boardState.currentPlayer) {
        if (diceRoll != null && boardState.gamePhase == GamePhase.MOVING) {
            val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
            movableTokens = getMovableTokens(currentPlayer, diceRoll!!.total, gameRules)
            
            if (movableTokens.isEmpty()) {
                gameMessage = "No valid moves available!"
                delay(2000)
                // Auto-advance to next player
                boardState = handleTurn(boardState, gameRules, diceRoll!!)
                diceRoll = null
                gameMessage = ""
            } else {
                gameMessage = "Select a token to move"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = min(this.size.width, this.size.height)
            val squareSize = boardSize / 15f

            // Define colors
            val green = Color(0xFF00B04F)
            val blue = Color(0xFF0066CC)
            val yellow = Color(0xFFFFD700)
            val red = Color(0xFFC8102E)
            val white = Color.White
            val black = Color.Black
            val lightGray = Color(0xFFF5F5F5)
            val darkGray = Color(0xFF333333)

            // Draw outer border (silver/gray frame)
            drawRect(
                color = Color(0xFFE0E0E0),
                topLeft = Offset(-squareSize * 0.5f, -squareSize * 0.5f),
                size = Size(boardSize + squareSize, boardSize + squareSize)
            )
            drawRect(
                color = darkGray,
                topLeft = Offset(-squareSize * 0.5f, -squareSize * 0.5f),
                size = Size(boardSize + squareSize, boardSize + squareSize),
                style = Stroke(width = 8f)
            )

            // Draw main board background
            drawRect(color = white, size = Size(boardSize, boardSize))

            // --- Draw Corner Player Areas ---
            
            // Top-left: Green area
            drawRect(
                color = green,
                topLeft = Offset(0f, 0f),
                size = Size(squareSize * 6, squareSize * 6)
            )
            drawRect(
                color = black,
                topLeft = Offset(0f, 0f),
                size = Size(squareSize * 6, squareSize * 6),
                style = Stroke(width = 3f)
            )
            
            // Top-right: Red area
            drawRect(
                color = red,
                topLeft = Offset(squareSize * 9, 0f),
                size = Size(squareSize * 6, squareSize * 6)
            )
            drawRect(
                color = black,
                topLeft = Offset(squareSize * 9, 0f),
                size = Size(squareSize * 6, squareSize * 6),
                style = Stroke(width = 3f)
            )
            
            // Bottom-left: Yellow area
            drawRect(
                color = yellow,
                topLeft = Offset(0f, squareSize * 9),
                size = Size(squareSize * 6, squareSize * 6)
            )
            drawRect(
                color = black,
                topLeft = Offset(0f, squareSize * 9),
                size = Size(squareSize * 6, squareSize * 6),
                style = Stroke(width = 3f)
            )
            
            // Bottom-right: Blue area
            drawRect(
                color = blue,
                topLeft = Offset(squareSize * 9, squareSize * 9),
                size = Size(squareSize * 6, squareSize * 6)
            )
            drawRect(
                color = black,
                topLeft = Offset(squareSize * 9, squareSize * 9),
                size = Size(squareSize * 6, squareSize * 6),
                style = Stroke(width = 3f)
            )

            // --- Draw Center Area ---
            val centerTopLeft = Offset(squareSize * 6, squareSize * 6)
            val centerSize = Size(squareSize * 3, squareSize * 3)
            
            // Center background
            drawRoundRect(
                color = white,
                topLeft = centerTopLeft,
                size = centerSize,
                cornerRadius = CornerRadius(squareSize * 0.3f)
            )
            drawRoundRect(
                color = black,
                topLeft = centerTopLeft,
                size = centerSize,
                cornerRadius = CornerRadius(squareSize * 0.3f),
                style = Stroke(width = 3f)
            )

            // --- Draw Game Path ---
            fun drawGameSquare(x: Int, y: Int, color: Color, hasBorder: Boolean = true) {
                drawRect(
                    color = color,
                    topLeft = Offset(x * squareSize, y * squareSize),
                    size = Size(squareSize, squareSize)
                )
                if (hasBorder) {
                    drawRect(
                        color = black,
                        topLeft = Offset(x * squareSize, y * squareSize),
                        size = Size(squareSize, squareSize),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Vertical paths (left and right sides)
            for (i in 0 until 6) {
                // Left side
                drawGameSquare(6, i, white)
                drawGameSquare(7, i, if (i == 1) red else white)
                drawGameSquare(8, i, white)
                
                // Right side  
                drawGameSquare(6, i + 9, white)
                drawGameSquare(7, i + 9, if (i == 4) yellow else white)
                drawGameSquare(8, i + 9, white)
            }

            // Horizontal paths (top and bottom)
            for (i in 0 until 6) {
                // Top side
                drawGameSquare(i, 6, white)
                drawGameSquare(i, 7, if (i == 1) green else white)
                drawGameSquare(i, 8, white)
                
                // Bottom side
                drawGameSquare(i + 9, 6, white)
                drawGameSquare(i + 9, 7, if (i == 4) blue else white)
                drawGameSquare(i + 9, 8, white)
            }

            // Draw starting positions (colored squares)
            drawGameSquare(1, 6, green)  // Green start
            drawStar(Offset(1.5f * squareSize, 6.5f * squareSize), squareSize * 0.3f, Color.White)
            drawGameSquare(8, 1, red)  // Red start  
            drawStar(Offset(8.5f * squareSize, 1.5f * squareSize), squareSize * 0.3f, Color.White)  
            drawGameSquare(13, 8, blue)  // Blue start
            drawStar(Offset(13.5f * squareSize, 8.5f * squareSize), squareSize * 0.3f, Color.White)
            drawGameSquare(6, 13, yellow)  // Yellow start
            drawStar(Offset(6.5f * squareSize, 13.5f * squareSize), squareSize * 0.3f, Color.White)

            // Draw arrows in colored home paths
            for (i in 1..5) {
                drawStar(Offset(7.5f * squareSize, (i + 0.5f) * squareSize), squareSize * 0.3f, red)
                drawStar(Offset(7.5f * squareSize, (i + 8.5f) * squareSize), squareSize * 0.3f, yellow)
                drawStar(Offset((i + 0.5f) * squareSize, 7.5f * squareSize), squareSize * 0.3f, green)
                drawStar(Offset((i + 8.5f) * squareSize, 7.5f * squareSize), squareSize * 0.3f, blue)
            }

            // --- Draw Bird Emojis ---
            fun DrawScope.drawEmoji(emoji: String, centerX: Float, centerY: Float, fontSize: TextUnit, color: Color = Color.Black) {
                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(emoji),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = fontSize,
                        color = color
                    )
                )
                val textWidth = textLayoutResult.size.width.toFloat()
                val textHeight = textLayoutResult.size.height.toFloat()
                drawText(
                    textMeasurer = textMeasurer,
                    text = emoji,
                    topLeft = Offset(centerX - textWidth / 2, centerY - textHeight / 2),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = fontSize,
                        color = color
                    )
                )
            }

            // Center emoji
            drawEmoji("🕊️", squareSize * 7.5f, squareSize * 7.5f, (squareSize * 2f).sp)

            // Green emoji (top-left)
            drawEmoji("🦚", squareSize * 3f, squareSize * 3f, (squareSize * 3.5f).sp)

            // Red emoji (top-right)
            drawEmoji("🦜", squareSize * 12f, squareSize * 3f, (squareSize * 3.5f).sp)

            // Yellow emoji (bottom-left)
            drawEmoji("🐥", squareSize * 3f, squareSize * 12f, (squareSize * 3.5f).sp)

            // Blue emoji (bottom-right)
            drawEmoji("🐦", squareSize * 12f, squareSize * 12f, (squareSize * 3.5f).sp)

            // --- Draw Player Tokens ---
            fun drawToken(centerX: Float, centerY: Float, color: Color, isSelected: Boolean = false, isMovable: Boolean = false) {
                val tokenRadius = squareSize * 0.35f
                
                // Highlight for movable tokens
                if (isMovable) {
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.5f),
                        radius = tokenRadius * 1.3f,
                        center = Offset(centerX, centerY)
                    )
                }
                
                // Selection highlight
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = tokenRadius * 1.2f,
                        center = Offset(centerX, centerY)
                    )
                }
                
                // Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = tokenRadius,
                    center = Offset(centerX + 2f, centerY + 2f)
                )
                // Main token
                drawCircle(
                    color = color,
                    radius = tokenRadius,
                    center = Offset(centerX, centerY)
                )
                // Border
                drawCircle(
                    color = black,
                    radius = tokenRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
                // Highlight
                drawCircle(
                    color = white.copy(alpha = 0.6f),
                    radius = tokenRadius * 0.3f,
                    center = Offset(centerX - tokenRadius * 0.3f, centerY - tokenRadius * 0.3f)
                )
            }

            // Draw tokens dynamically based on boardState
            boardState.players.forEach { player ->
                player.tokens.forEach { token ->
                    val tokenCoords = getTokenCoordinates(token, squareSize)
                    val isSelected = selectedToken?.id == token.id && selectedToken?.color == token.color
                    val isMovable = movableTokens.contains(token)
                    
                    drawToken(
                        tokenCoords.x, 
                        tokenCoords.y, 
                        when (token.color) {
                            PlayerColor.GREEN -> green
                            PlayerColor.RED -> red
                            PlayerColor.YELLOW -> yellow
                            PlayerColor.BLUE -> blue
                        },
                        isSelected = isSelected,
                        isMovable = isMovable
                    )
                }
            }
        }
        
        // Exit Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = {
                    Text(
                        text = "Exit Game",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = "Do you want to exit the game?",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text(
                            text = "Yes",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text(
                            text = "No",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }

        // Winner Dialog
        if (boardState.winner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "Game Over!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                text = {
                    Text(
                        text = "${boardState.winner!!.name} wins!",
                        fontSize = 18.sp,
                        color = when (boardState.winner!!) {
                            PlayerColor.GREEN -> Color(0xFF00B04F)
                            PlayerColor.RED -> Color(0xFFC8102E)
                            PlayerColor.YELLOW -> Color(0xFFFFD700)
                            PlayerColor.BLUE -> Color(0xFF0066CC)
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            boardState = initializeGameState(gameRules)
                            diceRoll = null
                            selectedToken = null
                            movableTokens = emptyList()
                            gameMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = "New Game",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text(
                            text = "Exit",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }

        // Game Controls and Dice Display
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Message
            if (gameMessage.isNotEmpty()) {
                Text(
                    text = gameMessage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Current Player Indicator
            Text(
                text = "Current Player: ${boardState.currentPlayer.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (boardState.currentPlayer) {
                    PlayerColor.GREEN -> Color(0xFF00B04F)
                    PlayerColor.RED -> Color(0xFFC8102E)
                    PlayerColor.YELLOW -> Color(0xFFFFD700)
                    PlayerColor.BLUE -> Color(0xFF0066CC)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dice Display Area - THREE PLACEHOLDERS AS REQUESTED
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Die 1 Placeholder
                DiceCard(
                    title = "Die 1",
                    value = if (isRolling) "?" else (diceRoll?.die1?.toString() ?: "-"),
                    isRolling = isRolling
                )
                
                // Die 2 Placeholder
                DiceCard(
                    title = "Die 2", 
                    value = if (isRolling) "?" else (diceRoll?.die2?.toString() ?: "-"),
                    isRolling = isRolling
                )
                
                // Total Placeholder
                DiceCard(
                    title = "Total",
                    value = if (isRolling) "?" else (diceRoll?.total?.toString() ?: "-"),
                    isRolling = isRolling,
                    isTotal = true
                )
            }

            // Roll Dice Button
            Button(
                onClick = {
                    if (!isRolling && boardState.gamePhase == GamePhase.ROLLING) {
                        isRolling = true
                        // Simulate rolling animation delay
                        GlobalScope.launch {
                            delay(1000)
                            val newDiceRoll = rollTwoDice()
                            diceRoll = newDiceRoll
                            boardState = boardState.copy(gamePhase = GamePhase.MOVING)
                            isRolling = false
                        }
                    }
                },
                enabled = !isRolling && boardState.gamePhase == GamePhase.ROLLING,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(width = 120.dp, height = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF6B7280)
                )
            ) {
                Text(
                    text = if (isRolling) "Rolling..." else "Roll Dice",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Auto Move Button (for testing and AI play)
            if (boardState.gamePhase == GamePhase.MOVING && diceRoll != null && movableTokens.isNotEmpty()) {
                Button(
                    onClick = {
                        boardState = performAutomaticMove(boardState, gameRules, diceRoll!!)
                        boardState = handleTurn(boardState, gameRules, diceRoll!!)
                        selectedToken = null
                        diceRoll = null
                        movableTokens = emptyList()
                        gameMessage = ""
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(width = 120.dp, height = 40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    Text(
                        text = "Auto Move",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DiceCard(
    title: String,
    value: String,
    isRolling: Boolean,
    isTotal: Boolean = false
) {
    Card(
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTotal) Color(0xFF3B82F6) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isTotal) Color.White else Color.Gray
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTotal) Color.White else Color.Black
            )
        }
    }
}

// Helper function to get token coordinates
fun getTokenCoordinates(token: Token, squareSize: Float): Offset {
    return when (token.position) {
        -1 -> { // In home base
            when (token.color) {
                PlayerColor.GREEN -> when (token.id) {
                    1 -> Offset(squareSize * 1.5f, squareSize * 1.5f)
                    2 -> Offset(squareSize * 4.5f, squareSize * 1.5f)
                    3 -> Offset(squareSize * 1.5f, squareSize * 4.5f)
                    4 -> Offset(squareSize * 4.5f, squareSize * 4.5f)
                    else -> Offset.Zero
                }
                PlayerColor.RED -> when (token.id) {
                    1 -> Offset(squareSize * 10.5f, squareSize * 1.5f)
                    2 -> Offset(squareSize * 13.5f, squareSize * 1.5f)
                    3 -> Offset(squareSize * 10.5f, squareSize * 4.5f)
                    4 -> Offset(squareSize * 13.5f, squareSize * 4.5f)
                    else -> Offset.Zero
                }
                PlayerColor.YELLOW -> when (token.id) {
                    1 -> Offset(squareSize * 1.5f, squareSize * 10.5f)
                    2 -> Offset(squareSize * 4.5f, squareSize * 10.5f)
                    3 -> Offset(squareSize * 1.5f, squareSize * 13.5f)
                    4 -> Offset(squareSize * 4.5f, squareSize * 13.5f)
                    else -> Offset.Zero
                }
                PlayerColor.BLUE -> when (token.id) {
                    1 -> Offset(squareSize * 10.5f, squareSize * 10.5f)
                    2 -> Offset(squareSize * 13.5f, squareSize * 10.5f)
                    3 -> Offset(squareSize * 10.5f, squareSize * 13.5f)
                    4 -> Offset(squareSize * 13.5f, squareSize * 13.5f)
                    else -> Offset.Zero
                }
            }
        }
        in 0..51 -> { // Main path
            val x = when (token.position) {
                in 0..4 -> 6
                5 -> 5
                in 6..10 -> 5 - (token.position - 6)
                11 -> 0
                in 12..16 -> 0
                17 -> 1
                in 18..22 -> 1 + (token.position - 18)
                23 -> 6
                in 24..28 -> 6
                29 -> 7
                in 30..34 -> 7 + (token.position - 30)
                35 -> 12
                in 36..40 -> 12
                41 -> 13
                in 42..46 -> 13 - (token.position - 42)
                47 -> 8
                in 48..51 -> 8
                else -> 0
            }
            val y = when (token.position) {
                in 0..4 -> 14 - token.position
                5 -> 9
                in 6..10 -> 9
                11 -> 8
                in 12..16 -> 8 - (token.position - 12)
                17 -> 6
                in 18..22 -> 6
                23 -> 5
                in 24..28 -> 5 - (token.position - 24)
                29 -> 0
                in 30..34 -> 0
                35 -> 1
                in 36..40 -> 1 + (token.position - 36)
                41 -> 6
                in 42..46 -> 6
                47 -> 7
                in 48..51 -> 7 + (token.position - 48)
                else -> 0
            }
            Offset((x + 0.5f) * squareSize, (y + 0.5f) * squareSize)
        }
        in 100..105 -> { // Home path
            val homePathIndex = token.position - 100
            when (token.color) {
                PlayerColor.GREEN -> Offset((1 + homePathIndex + 0.5f) * squareSize, (7 + 0.5f) * squareSize)
                PlayerColor.RED -> Offset((7 + 0.5f) * squareSize, (1 + homePathIndex + 0.5f) * squareSize)
                PlayerColor.YELLOW -> Offset((7 + 0.5f) * squareSize, (13 - homePathIndex + 0.5f) * squareSize)
                PlayerColor.BLUE -> Offset((13 - homePathIndex + 0.5f) * squareSize, (7 + 0.5f) * squareSize)
            }
        }
        200 -> { // Finished
            Offset(squareSize * 7.5f, squareSize * 7.5f) // Center of the board
        }
        else -> Offset.Zero
    }
}

// --- Drawing Helpers ---

fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    drawPath(path = Path().apply { 
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close() 
    }, color = color)
}

fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    var angle = -90.0
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(Math.toRadians(angle))).toFloat()
        val y = center.y + (r * sin(Math.toRadians(angle))).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += 36.0
    }
    path.close()
    drawPath(path = path, color = color)
    drawPath(path = path, color = Color.Black.copy(alpha = 0.8f), style = Stroke(width = 2f))
}

