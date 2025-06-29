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
    var movableTokens by remember { mutableStateOf<List<TokenMove>>(emptyList()) } // Changed to TokenMove
    var gameMessage by remember { mutableStateOf("") }
    var selectedMoveValue by remember { mutableStateOf<Int?>(null) }

    // Handle back navigation with confirmation dialog
    BackHandler {
        showExitDialog = true
    }

    // Update movable tokens when dice is rolled
    LaunchedEffect(diceRoll, boardState.currentPlayer) {
        if (diceRoll != null && boardState.gamePhase == GamePhase.MOVING) {
            val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
            // Now use getAvailableMovesForDiceRoll which returns List<TokenMove>
            movableTokens = getAvailableMovesForDiceRoll(currentPlayer, diceRoll!!, gameRules)
            
            if (movableTokens.isEmpty()) {
                gameMessage = "No valid moves available!"
                delay(2000)
                // Auto-advance to next player if no moves possible
                boardState = handleTurn(boardState, gameRules, diceRoll!!)
                diceRoll = null
                gameMessage = ""
            } else {
                gameMessage = "Choose a dice value and select a token to move"
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

            // Draw starting positions (colored squares) - ORIGINAL POSITIONS PRESERVED
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
                    // Check if the token itself is movable (exists in any TokenMove in movableTokens)
                    val isMovable = movableTokens.any { it.token == token }
                    
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
        
        // UI Controls (Dice Roll, Token Selection, etc.)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = gameMessage, color = Color.Black, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Dice Roll Button
            Button(
                onClick = {
                    if (!isRolling && boardState.gamePhase == GamePhase.ROLLING) {
                        isRolling = true
                        gameMessage = "Rolling dice..."
                        GlobalScope.launch {
                            delay(1000) // Simulate dice roll animation
                            val newDiceRoll = rollTwoDice()
                            diceRoll = newDiceRoll
                            boardState = handleTurn(boardState, gameRules, newDiceRoll)
                            isRolling = false
                            gameMessage = "You rolled ${newDiceRoll.die1} and ${newDiceRoll.die2}"
                            
                            // If no moves are available after rolling, immediately advance turn
                            val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
                            val availableMovesAfterRoll = getAvailableMovesForDiceRoll(currentPlayer, newDiceRoll, gameRules)
                            if (availableMovesAfterRoll.isEmpty()) {
                                delay(1500)
                                gameMessage = "No valid moves. Next player's turn."
                                boardState = boardState.copy(currentPlayer = getNextPlayer(boardState.currentPlayer))
                                diceRoll = null // Reset dice for next turn
                            } else {
                                movableTokens = availableMovesAfterRoll
                                gameMessage = "Select a token to move with ${newDiceRoll.die1} or ${newDiceRoll.die2}"
                            }
                        }
                    }
                },
                enabled = !isRolling && boardState.gamePhase == GamePhase.ROLLING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Roll Dice")
            }

            Spacer(Modifier.height(16.dp))

            // Display Dice Values and allow selection
            if (diceRoll != null && boardState.gamePhase == GamePhase.MOVING) {
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    // Die 1 button
                    Button(
                        onClick = { selectedMoveValue = diceRoll!!.die1 },
                        enabled = movableTokens.any { it.dieUsed == 1 || it.dieUsed == 0 } && selectedMoveValue != diceRoll!!.die1
                    ) {
                        Text("Use ${diceRoll!!.die1}")
                    }
                    // Die 2 button
                    Button(
                        onClick = { selectedMoveValue = diceRoll!!.die2 },
                        enabled = movableTokens.any { it.dieUsed == 2 || it.dieUsed == 0 } && selectedMoveValue != diceRoll!!.die2
                    ) {
                        Text("Use ${diceRoll!!.die2}")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Token selection and move button
            if (selectedMoveValue != null && movableTokens.isNotEmpty()) {
                Text("Selected Die Value: ${selectedMoveValue}", modifier = Modifier.padding(bottom = 8.dp))
                
                // Display movable tokens for the selected die value
                val tokensForSelectedDie = movableTokens.filter { 
                    (it.dieUsed == selectedMoveValue || it.dieUsed == 0) && it.steps == selectedMoveValue
                }.map { it.token }.distinct()

                if (tokensForSelectedDie.isNotEmpty()) {
                    Text("Select a token:", modifier = Modifier.padding(bottom = 4.dp))
                    FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                        tokensForSelectedDie.forEach { token ->
                            Card(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        selectedToken = token
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedToken == token) Color.LightGray else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("${token.color.name.first()}${token.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedToken?.let { tokenToMove ->
                                selectedMoveValue?.let { steps ->
                                    // Find the specific TokenMove that matches the selected token and steps
                                    val chosenMove = movableTokens.firstOrNull { 
                                        it.token == tokenToMove && it.steps == steps && (it.dieUsed == steps || it.dieUsed == 0)
                                    }
                                    
                                    if (chosenMove != null) {
                                        boardState = moveToken(boardState, chosenMove.token, chosenMove.steps, gameRules)
                                        selectedToken = null
                                        selectedMoveValue = null
                                        movableTokens = emptyList()
                                        gameMessage = "Token moved!"
                                        
                                        // After a move, handle the next turn (e.g., extra turn or next player)
                                        boardState = handleTurn(boardState, gameRules, diceRoll!!)
                                        diceRoll = null // Reset dice for next turn
                                    } else {
                                        gameMessage = "Invalid move combination selected."
                                    }
                                }
                            }
                        },
                        enabled = selectedToken != null && selectedMoveValue != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Move Selected Token")
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
        }
    }
}

// Helper function to get token coordinates on the board
fun getTokenCoordinates(token: Token, squareSize: Float): Offset {
    return when (token.position) {
        -1 -> getBaseCoordinates(token.color, token.id, squareSize)
        in 0..51 -> getMainPathCoordinates(token.position, squareSize)
        in 100..105 -> getHomePathCoordinates(token.color, token.position - 100, squareSize)
        200 -> getFinishedCoordinates(token.color, token.id, squareSize)
        else -> Offset.Zero
    }
}

fun getBaseCoordinates(color: PlayerColor, tokenId: Int, squareSize: Float): Offset {
    val baseCenterX = when (color) {
        PlayerColor.GREEN -> squareSize * 3
        PlayerColor.RED -> squareSize * 12
        PlayerColor.YELLOW -> squareSize * 3
        PlayerColor.BLUE -> squareSize * 12
    }
    val baseCenterY = when (color) {
        PlayerColor.GREEN -> squareSize * 3
        PlayerColor.RED -> squareSize * 3
        PlayerColor.YELLOW -> squareSize * 12
        PlayerColor.BLUE -> squareSize * 12
    }

    // Arrange tokens in a 2x2 grid within the base area
    val offsetX = if (tokenId % 2 == 1) -squareSize * 0.7f else squareSize * 0.7f
    val offsetY = if (tokenId <= 2) -squareSize * 0.7f else squareSize * 0.7f

    return Offset(baseCenterX + offsetX, baseCenterY + offsetY)
}

fun getMainPathCoordinates(position: Int, squareSize: Float): Offset {
    // This is a simplified mapping. A real Ludo board has a more complex path.
    // You'll need to map each of the 52 positions to its exact (x,y) coordinate.
    // This example assumes a linear path for demonstration.
    val x: Float
    val y: Float

    when (position) {
        in 0..4 -> { // Right from green start
            x = squareSize * (1 + position)
            y = squareSize * 6
        }
        in 5..11 -> { // Upwards towards red start
            x = squareSize * 6
            y = squareSize * (5 - (position - 5))
        }
        in 12..17 -> { // Right from red start
            x = squareSize * (7 + (position - 12))
            y = squareSize * 0
        }
        in 18..24 -> { // Downwards towards yellow start
            x = squareSize * 8
            y = squareSize * (1 + (position - 18))
        }
        in 25..30 -> { // Left from yellow start
            x = squareSize * (7 - (position - 25))
            y = squareSize * 8
        }
        in 31..37 -> { // Downwards towards blue start
            x = squareSize * 0
            y = squareSize * (9 + (position - 31))
        }
        in 38..43 -> { // Left from blue start
            x = squareSize * (1 + (position - 38))
            y = squareSize * 14
        }
        in 44..50 -> { // Upwards towards green start
            x = squareSize * 6
            y = squareSize * (13 - (position - 44))
        }
        51 -> { // Before green home path
            x = squareSize * 6
            y = squareSize * 7
        }
        else -> Offset.Zero
    }
    return Offset(x + squareSize / 2, y + squareSize / 2)
}

fun getHomePathCoordinates(color: PlayerColor, homePathPosition: Int, squareSize: Float): Offset {
    val x: Float
    val y: Float
    when (color) {
        PlayerColor.GREEN -> {
            x = squareSize * 7
            y = squareSize * (1 + homePathPosition)
        }
        PlayerColor.RED -> {
            x = squareSize * (13 - homePathPosition)
            y = squareSize * 7
        }
        PlayerColor.YELLOW -> {
            x = squareSize * 7
            y = squareSize * (13 - homePathPosition)
        }
        PlayerColor.BLUE -> {
            x = squareSize * (1 + homePathPosition)
            y = squareSize * 7
        }
    }
    return Offset(x + squareSize / 2, y + squareSize / 2)
}

fun getFinishedCoordinates(color: PlayerColor, tokenId: Int, squareSize: Float): Offset {
    val centerX = squareSize * 7.5f
    val centerY = squareSize * 7.5f
    val offset = squareSize * 0.2f

    return when (color) {
        PlayerColor.GREEN -> Offset(centerX - offset, centerY - offset)
        PlayerColor.RED -> Offset(centerX + offset, centerY - offset)
        PlayerColor.YELLOW -> Offset(centerX - offset, centerY + offset)
        PlayerColor.BLUE -> Offset(centerX + offset, centerY + offset)
    }
}

// Helper to draw a star (for safe zones and home path arrows)
fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        val numPoints = 5
        val innerRadius = radius * 0.4f
        var angle = Math.PI / 2.0 // Start at top

        moveTo(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())

        for (i in 0 until numPoints) {
            angle += Math.PI / numPoints
            lineTo(center.x + innerRadius * cos(angle).toFloat(), center.y + innerRadius * sin(angle).toFloat())
            angle += Math.PI / numPoints
            lineTo(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
        }
        close()
    }
    drawPath(path, color)
}






































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































val (x, y) = coordinates
    val tokenRadius = squareSize * 0.35f
    val tokenColor = when (color) {
        PlayerColor.GREEN -> green
        PlayerColor.RED -> red
        PlayerColor.YELLOW -> yellow
        PlayerColor.BLUE -> blue
    }

    drawCircle(
        color = tokenColor,
        radius = tokenRadius,
        center = Offset(x, y)
    )
    drawCircle(
        color = black,
        radius = tokenRadius,
        center = Offset(x, y),
        style = Stroke(width = 2f)
    )
    if (isSelected) {
        drawCircle(
            color = Color.White,
            radius = tokenRadius * 1.2f,
            center = Offset(x, y),
            style = Stroke(width = 3f)
        )
    }
    if (isMovable) {
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.5f),
            radius = tokenRadius * 1.3f,
            center = Offset(x, y)
        )
    }
}

// Helper function to get token coordinates on the board
fun getTokenCoordinates(token: Token, squareSize: Float): Offset {
    return when (token.position) {
        -1 -> getBaseCoordinates(token.color, token.id, squareSize)
        in 0..51 -> getMainPathCoordinates(token.position, squareSize)
        in 100..105 -> getHomePathCoordinates(token.color, token.position - 100, squareSize)
        200 -> getFinishedCoordinates(token.color, token.id, squareSize)
        else -> Offset.Zero
    }
}

fun getBaseCoordinates(color: PlayerColor, tokenId: Int, squareSize: Float): Offset {
    val baseCenterX = when (color) {
        PlayerColor.GREEN -> squareSize * 3
        PlayerColor.RED -> squareSize * 12
        PlayerColor.YELLOW -> squareSize * 3
        PlayerColor.BLUE -> squareSize * 12
    }
    val baseCenterY = when (color) {
        PlayerColor.GREEN -> squareSize * 3
        PlayerColor.RED -> squareSize * 3
        PlayerColor.YELLOW -> squareSize * 12
        PlayerColor.BLUE -> squareSize * 12
    }

    // Arrange tokens in a 2x2 grid within the base area
    val offsetX = if (tokenId % 2 == 1) -squareSize * 0.7f else squareSize * 0.7f
    val offsetY = if (tokenId <= 2) -squareSize * 0.7f else squareSize * 0.7f

    return Offset(baseCenterX + offsetX, baseCenterY + offsetY)
}

fun getMainPathCoordinates(position: Int, squareSize: Float): Offset {
    // This is a simplified mapping. A real Ludo board has a more complex path.
    // You'll need to map each of the 52 positions to its exact (x,y) coordinate.
    // This example assumes a linear path for demonstration.
    val x: Float
    val y: Float

    when (position) {
        in 0..4 -> { // Right from green start
            x = squareSize * (1 + position)
            y = squareSize * 6
        }
        in 5..11 -> { // Upwards towards red start
            x = squareSize * 6
            y = squareSize * (5 - (position - 5))
        }
        in 12..17 -> { // Right from red start
            x = squareSize * (7 + (position - 12))
            y = squareSize * 0
        }
        in 18..24 -> { // Downwards towards yellow start
            x = squareSize * 8
            y = squareSize * (1 + (position - 18))
        }
        in 25..30 -> { // Left from yellow start
            x = squareSize * (7 - (position - 25))
            y = squareSize * 8
        }
        in 31..37 -> { // Downwards towards blue start
            x = squareSize * 0
            y = squareSize * (9 + (position - 31))
        }
        in 38..43 -> { // Left from blue start
            x = squareSize * (1 + (position - 38))
            y = squareSize * 14
        }
        in 44..50 -> { // Upwards towards green start
            x = squareSize * 6
            y = squareSize * (13 - (position - 44))
        }
        51 -> { // Before green home path
            x = squareSize * 6
            y = squareSize * 7
        }
        else -> Offset.Zero
    }
    return Offset(x + squareSize / 2, y + squareSize / 2)
}

fun getHomePathCoordinates(color: PlayerColor, homePathPosition: Int, squareSize: Float): Offset {
    val x: Float
    val y: Float
    when (color) {
        PlayerColor.GREEN -> {
            x = squareSize * 7
            y = squareSize * (1 + homePathPosition)
        }
        PlayerColor.RED -> {
            x = squareSize * (13 - homePathPosition)
            y = squareSize * 7
        }
        PlayerColor.YELLOW -> {
            x = squareSize * 7
            y = squareSize * (13 - homePathPosition)
        }
        PlayerColor.BLUE -> {
            x = squareSize * (1 + homePathPosition)
            y = squareSize * 7
        }
    }
    return Offset(x + squareSize / 2, y + squareSize / 2)
}

fun getFinishedCoordinates(color: PlayerColor, tokenId: Int, squareSize: Float): Offset {
    val centerX = squareSize * 7.5f
    val centerY = squareSize * 7.5f
    val offset = squareSize * 0.2f

    return when (color) {
        PlayerColor.GREEN -> Offset(centerX - offset, centerY - offset)
        PlayerColor.RED -> Offset(centerX + offset, centerY - offset)
        PlayerColor.YELLOW -> Offset(centerX - offset, centerY + offset)
        PlayerColor.BLUE -> Offset(centerX + offset, centerY + offset)
    }
}

// Helper to draw a star (for safe zones and home path arrows)
fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        val numPoints = 5
        val innerRadius = radius * 0.4f
        var angle = Math.PI / 2.0 // Start at top

        moveTo(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())

        for (i in 0 until numPoints) {
            angle += Math.PI / numPoints
            lineTo(center.x + innerRadius * cos(angle).toFloat(), center.y + innerRadius * sin(angle).toFloat())
            angle += Math.PI / numPoints
            lineTo(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
        }
        close()
    }
    drawPath(path, color)
}


