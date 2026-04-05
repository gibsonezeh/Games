package com.gibson.games.ludo

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class MoveOptionKind {
    DIE,
    TOTAL
}

private data class MoveOption(
    val key: String,
    val title: String,
    val value: Int,
    val kind: MoveOptionKind
)

@Composable
fun LudoGameScreen(
    onExit: () -> Unit,
    gameRules: GameRules = GameRules()
) {
    var showExitDialog by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    var boardState by remember(gameRules) { mutableStateOf(initializeGameState(gameRules)) }
    var isRolling by remember { mutableStateOf(false) }
    var selectedToken by remember { mutableStateOf<Token?>(null) }
    var movableTokens by remember { mutableStateOf<List<Token>>(emptyList()) }
    var gameMessage by remember { mutableStateOf("") }

    var selectedMoveOption by remember { mutableStateOf<MoveOption?>(null) }
    var remainingDiceValues by remember { mutableStateOf<List<Int>>(emptyList()) }
    var totalAvailable by remember { mutableStateOf(false) }
    var initializedRoll by remember { mutableStateOf<DiceRoll?>(null) }

    BackHandler {
        showExitDialog = true
    }

    fun buildMoveOptions(): List<MoveOption> {
        val options = mutableListOf<MoveOption>()

        remainingDiceValues.forEachIndexed { index, value ->
            options += MoveOption(
                key = "die_$index_$value",
                title = "Die ${index + 1}",
                value = value,
                kind = MoveOptionKind.DIE
            )
        }

        if (totalAvailable && remainingDiceValues.size == 2) {
            options += MoveOption(
                key = "total_${remainingDiceValues.sum()}",
                title = "Total",
                value = remainingDiceValues.sum(),
                kind = MoveOptionKind.TOTAL
            )
        }

        return options
    }

    LaunchedEffect(boardState.diceRoll, boardState.gamePhase) {
        if (boardState.gamePhase == GamePhase.MOVING && boardState.diceRoll != null) {
            if (initializedRoll != boardState.diceRoll) {
                initializedRoll = boardState.diceRoll
                remainingDiceValues = listOf(boardState.diceRoll.die1, boardState.diceRoll.die2)
                totalAvailable = true
                selectedMoveOption = null
                movableTokens = emptyList()
            }
        } else if (boardState.gamePhase == GamePhase.ROLLING) {
            initializedRoll = null
            remainingDiceValues = emptyList()
            totalAvailable = false
            selectedMoveOption = null
            movableTokens = emptyList()
            selectedToken = null
            if (!isRolling) {
                gameMessage = "Roll the dice"
            }
        }
    }

    LaunchedEffect(selectedMoveOption, boardState.currentPlayer, boardState.gamePhase, remainingDiceValues) {
        if (boardState.gamePhase == GamePhase.MOVING) {
            val moveOption = selectedMoveOption
            if (moveOption == null) {
                movableTokens = emptyList()
                gameMessage = "Choose a dice value to use for movement"
            } else {
                val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
                val available = getMovableTokens(currentPlayer, moveOption.value, gameRules)
                movableTokens = available

                if (available.isEmpty()) {
                    val selectedKey = moveOption.key
                    gameMessage = "No valid moves with ${moveOption.value}"
                    delay(1200)
                    if (selectedMoveOption?.key == selectedKey) {
                        selectedMoveOption = null
                        movableTokens = emptyList()
                        gameMessage = "Choose another dice value"
                    }
                } else {
                    gameMessage = "Select a token to move with ${moveOption.value}"
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val density = LocalDensity.current
        val boardSizePx = with(density) { minOf(maxWidth.toPx(), maxHeight.toPx()) }
        val squareSizePx = boardSizePx / 15f

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boardSize = min(size.width, size.height)
                val squareSize = boardSize / 15f

                val green = Color(0xFF00B04F)
                val blue = Color(0xFF0066CC)
                val yellow = Color(0xFFFFD700)
                val red = Color(0xFFC8102E)
                val white = Color.White
                val black = Color.Black
                val darkGray = Color(0xFF333333)

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

                drawRect(color = white, size = Size(boardSize, boardSize))

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

                val centerTopLeft = Offset(squareSize * 6, squareSize * 6)
                val centerSize = Size(squareSize * 3, squareSize * 3)

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

                for (i in 0 until 6) {
                    drawGameSquare(6, i, white)
                    drawGameSquare(7, i, if (i == 1) red else white)
                    drawGameSquare(8, i, white)

                    drawGameSquare(6, i + 9, white)
                    drawGameSquare(7, i + 9, if (i == 4) yellow else white)
                    drawGameSquare(8, i + 9, white)
                }

                for (i in 0 until 6) {
                    drawGameSquare(i, 6, white)
                    drawGameSquare(i, 7, if (i == 1) green else white)
                    drawGameSquare(i, 8, white)

                    drawGameSquare(i + 9, 6, white)
                    drawGameSquare(i + 9, 7, if (i == 4) blue else white)
                    drawGameSquare(i + 9, 8, white)
                }

                drawGameSquare(1, 6, green)
                drawStar(Offset(1.5f * squareSize, 6.5f * squareSize), squareSize * 0.3f, Color.White)

                drawGameSquare(8, 1, red)
                drawStar(Offset(8.5f * squareSize, 1.5f * squareSize), squareSize * 0.3f, Color.White)

                drawGameSquare(13, 8, blue)
                drawStar(Offset(13.5f * squareSize, 8.5f * squareSize), squareSize * 0.3f, Color.White)

                drawGameSquare(6, 13, yellow)
                drawStar(Offset(6.5f * squareSize, 13.5f * squareSize), squareSize * 0.3f, Color.White)

                for (i in 1..5) {
                    drawStar(Offset(7.5f * squareSize, (i + 0.5f) * squareSize), squareSize * 0.3f, red)
                    drawStar(Offset(7.5f * squareSize, (i + 8.5f) * squareSize), squareSize * 0.3f, yellow)
                    drawStar(Offset((i + 0.5f) * squareSize, 7.5f * squareSize), squareSize * 0.3f, green)
                    drawStar(Offset((i + 8.5f) * squareSize, 7.5f * squareSize), squareSize * 0.3f, blue)
                }

                fun DrawScope.drawEmoji(
                    emoji: String,
                    centerX: Float,
                    centerY: Float,
                    fontSize: TextUnit,
                    color: Color = Color.Black
                ) {
                    val measured = textMeasurer.measure(
                        text = AnnotatedString(emoji),
                        style = TextStyle(fontSize = fontSize, color = color)
                    )
                    val textWidth = measured.size.width.toFloat()
                    val textHeight = measured.size.height.toFloat()

                    drawText(
                        textMeasurer = textMeasurer,
                        text = emoji,
                        topLeft = Offset(centerX - textWidth / 2f, centerY - textHeight / 2f),
                        style = TextStyle(fontSize = fontSize, color = color)
                    )
                }

                drawEmoji("🕊️", squareSize * 7.5f, squareSize * 7.5f, (squareSize * 2f).sp)
                drawEmoji("🦚", squareSize * 3f, squareSize * 3f, (squareSize * 3.5f).sp)
                drawEmoji("🦜", squareSize * 12f, squareSize * 3f, (squareSize * 3.5f).sp)
                drawEmoji("🐥", squareSize * 3f, squareSize * 12f, (squareSize * 3.5f).sp)
                drawEmoji("🐦", squareSize * 12f, squareSize * 12f, (squareSize * 3.5f).sp)

                fun drawToken(
                    centerX: Float,
                    centerY: Float,
                    color: Color,
                    isSelected: Boolean = false,
                    isMovable: Boolean = false
                ) {
                    val tokenRadius = squareSize * 0.35f

                    if (isMovable) {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = 0.5f),
                            radius = tokenRadius * 1.3f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    if (isSelected) {
                        drawCircle(
                            color = Color.White,
                            radius = tokenRadius * 1.2f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = tokenRadius,
                        center = Offset(centerX + 2f, centerY + 2f)
                    )

                    drawCircle(
                        color = color,
                        radius = tokenRadius,
                        center = Offset(centerX, centerY)
                    )

                    drawCircle(
                        color = black,
                        radius = tokenRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = white.copy(alpha = 0.6f),
                        radius = tokenRadius * 0.3f,
                        center = Offset(centerX - tokenRadius * 0.3f, centerY - tokenRadius * 0.3f)
                    )
                }

                boardState.players.forEach { player ->
                    player.tokens.forEach { token ->
                        val tokenCoords = getTokenCoordinates(token, squareSize)
                        val isSelected =
                            selectedToken?.id == token.id && selectedToken?.color == token.color
                        val isMovable = movableTokens.any {
                            it.id == token.id && it.color == token.color
                        }

                        drawToken(
                            centerX = tokenCoords.x,
                            centerY = tokenCoords.y,
                            color = when (token.color) {
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

            if (boardState.gamePhase == GamePhase.MOVING && selectedMoveOption != null) {
                movableTokens.forEach { token ->
                    val coords = getTokenCoordinates(token, squareSizePx)
                    val xDp = with(density) { coords.x.toDp() }
                    val yDp = with(density) { coords.y.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xDp - 22.dp, y = yDp - 22.dp)
                            .size(44.dp)
                            .clickable {
                                selectedToken = token

                                val moveOption = selectedMoveOption ?: return@clickable
                                val beforeMove = boardState
                                val movedState = moveToken(beforeMove, token, moveOption.value, gameRules)

                                if (movedState.winner != null) {
                                    boardState = movedState.copy(gamePhase = GamePhase.GAME_OVER)
                                    gameMessage = "${movedState.winner.name} wins!"
                                    selectedMoveOption = null
                                    movableTokens = emptyList()
                                    remainingDiceValues = emptyList()
                                    totalAvailable = false
                                    return@clickable
                                }

                                val newRemainingDiceValues =
                                    if (moveOption.kind == MoveOptionKind.TOTAL) {
                                        emptyList()
                                    } else {
                                        val temp = remainingDiceValues.toMutableList()
                                        val index = temp.indexOf(moveOption.value)
                                        if (index != -1) temp.removeAt(index)
                                        temp.toList()
                                    }

                                val newTotalAvailable =
                                    if (moveOption.kind == MoveOptionKind.TOTAL) false else false

                                val gotCaptureExtraTurn =
                                    gameRules.captureGivesExtraTurn &&
                                        didCaptureEnemy(beforeMove, movedState, beforeMove.currentPlayer)

                                val originalRoll = beforeMove.diceRoll
                                val gotSixExtraTurn =
                                    gameRules.getsExtraTurnOnSix &&
                                        originalRoll != null &&
                                        (originalRoll.die1 == 6 || originalRoll.die2 == 6)
 val currentPlayerAfterMove =
                                    movedState.players.first { it.color == beforeMove.currentPlayer }

                                val anyRemainingPlayable = newRemainingDiceValues.any { remainingValue ->
                                    getMovableTokens(currentPlayerAfterMove, remainingValue, gameRules).isNotEmpty()
                                }

                                remainingDiceValues = newRemainingDiceValues
                                totalAvailable = newTotalAvailable

                                boardState = when {
                                    newRemainingDiceValues.isNotEmpty() && anyRemainingPlayable -> {
                                        movedState.copy(
                                            currentPlayer = beforeMove.currentPlayer,
                                            gamePhase = GamePhase.MOVING,
                                            diceRoll = beforeMove.diceRoll,
                                            availableMoves = newRemainingDiceValues
                                        )
                                    }

                                    gotCaptureExtraTurn || gotSixExtraTurn -> {
                                        movedState.copy(
                                            currentPlayer = beforeMove.currentPlayer,
                                            gamePhase = GamePhase.ROLLING,
                                            diceRoll = null,
                                            availableMoves = emptyList()
                                        )
                                    }

                                    else -> {
                                        movedState.copy(
                                            currentPlayer = getNextPlayer(beforeMove.currentPlayer),
                                            gamePhase = GamePhase.ROLLING,
                                            diceRoll = null,
                                            availableMoves = emptyList()
                                        )
                                    }
                                }

                                gameMessage = when {
                                    newRemainingDiceValues.isNotEmpty() && anyRemainingPlayable ->
                                        "Play the remaining die"

                                    gotCaptureExtraTurn ->
                                        "Capture! Roll again"

                                    gotSixExtraTurn ->
                                        "You rolled a 6. Roll again"

                                    else ->
                                        "Next player: ${boardState.currentPlayer.name}"
                                }

                                selectedToken = null
                                selectedMoveOption = null
                                movableTokens = emptyList()
                            }
                    )
                }
            }

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

            if (boardState.winner != null) {
                AlertDialog(
                    onDismissRequest = {},
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
                                selectedToken = null
                                movableTokens = emptyList()
                                gameMessage = "New game started"
                                selectedMoveOption = null
                                remainingDiceValues = emptyList()
                                totalAvailable = false
                                initializedRoll = null
                                isRolling = false
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gameMessage.isNotEmpty()) {
                    Text(
                        text = gameMessage,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

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

                val moveOptions = buildMoveOptions()
                if (moveOptions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        moveOptions.forEach { option ->
                            DiceCard(
                                title = option.title,
                                value = option.value.toString(),
                                isRolling = false,
                                isTotal = option.kind == MoveOptionKind.TOTAL,
                                isSelected = selectedMoveOption?.key == option.key,
                                onClick = {
                                    if (boardState.gamePhase == GamePhase.MOVING) {
                                        selectedMoveOption = option
                                    }
                                },
                                isEnabled = boardState.gamePhase == GamePhase.MOVING
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (!isRolling && boardState.gamePhase == GamePhase.ROLLING) {
                            isRolling = true
                            selectedMoveOption = null
                            movableTokens = emptyList()
                            selectedToken = null
                            remainingDiceValues = emptyList()
                            totalAvailable = false
                            initializedRoll = null
                            gameMessage = "Rolling..."

                            scope.launch {
                                delay(700)
                                val newDiceRoll = rollTwoDice()
                                boardState = handleTurn(boardState, gameRules, newDiceRoll)
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
            }
        }
    }
}

@Composable
fun DiceCard(
    title: String,
    value: String,
    isRolling: Boolean,
    isTotal: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable(enabled = isEnabled && !isRolling, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFF10B981)
                isTotal -> Color(0xFF3B82F6)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(14.dp)
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
                color = if (isSelected || isTotal) Color.White else Color.Gray
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected || isTotal) Color.White else Color.Black
            )
        }
    }
}

private fun didCaptureEnemy(
    before: BoardState,
    after: BoardState,
    currentPlayer: PlayerColor
): Boolean {
    val beforeEnemies = before.players
        .filter { it.color != currentPlayer }
        .flatMap { it.tokens }

    val afterEnemies = after.players
        .filter { it.color != currentPlayer }
        .flatMap { it.tokens }

    return beforeEnemies.any { beforeToken ->
        val afterToken = afterEnemies.firstOrNull {
            it.color == beforeToken.color && it.id == beforeToken.id
        }
        beforeToken.position != -1 && afterToken?.position == -1
    }
}

fun getTokenCoordinates(token: Token, squareSize: Float): Offset {
    return when (token.position) {
        -1 -> {
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

        in 0..51 -> {
            val path = listOf(
                1 to 6,
                2 to 6,
                3 to 6,
                4 to 6,
                5 to 6,
                6 to 5,
                6 to 4,
                6 to 3,
                6 to 2,
                6 to 1,
                6 to 0,
                7 to 0,
                8 to 0,
                8 to 1,
                8 to 2,
                8 to 3,
                8 to 4,
                8 to 5,
                9 to 6,
                10 to 6,
                11 to 6,
                12 to 6,
                13 to 6,
                14 to 6,
                14 to 7,
                14 to 8,
                13 to 8,
                12 to 8,
                11 to 8,
                10 to 8,
                9 to 8,
                8 to 9,
                8 to 10,
                8 to 11,
                8 to 12,
                8 to 13,
                8 to 14,
                7 to 14,
                6 to 14,
                6 to 13,
                6 to 12,
                6 to 11,
                6 to 10,
                6 to 9,
                5 to 8,
                4 to 8,
                3 to 8,
                2 to 8,
                1 to 8,
                0 to 8,
                0 to 7,
                0 to 6
            )

            val (x, y) = path[token.position]
            Offset((x + 0.5f) * squareSize, (y + 0.5f) * squareSize)
        }

        in 100..105 -> {
            val homePathIndex = token.position - 100
            when (token.color) {
                PlayerColor.GREEN -> Offset(
                    (1 + homePathIndex + 0.5f) * squareSize,
                    (7 + 0.5f) * squareSize
                )

                PlayerColor.RED -> Offset(
                    (7 + 0.5f) * squareSize,
                    (1 + homePathIndex + 0.5f) * squareSize
                )

                PlayerColor.YELLOW -> Offset(
                    (7 + 0.5f) * squareSize,
                    (13 - homePathIndex + 0.5f) * squareSize
                )

                PlayerColor.BLUE -> Offset(
                    (13 - homePathIndex + 0.5f) * squareSize,
                    (7 + 0.5f) * squareSize
                )
            }
        }

        200 -> Offset(squareSize * 7.5f, squareSize * 7.5f)
        else -> Offset.Zero
    }
}

fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    drawPath(
        path = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        },
        color = color
    )
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
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.8f),
        style = Stroke(width = 2f)
    )
}
