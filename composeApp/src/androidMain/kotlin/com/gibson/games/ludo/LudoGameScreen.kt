package com.gibson.games.ludo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    setupConfig: LudoSetupConfig // ✅ ADD THIS
) {
    val playerCount = setupConfig.playerCount
    val playerNames = setupConfig.playerNames
    val mode = setupConfig.mode
    
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var boardState by remember(gameRules) { mutableStateOf(initializeGameState(gameRules)) }
    var isRolling by remember { mutableStateOf(false) }
    var isAnimatingMove by remember { mutableStateOf(false) }
    var selectedToken by remember { mutableStateOf<Token?>(null) }
    var movableTokens by remember { mutableStateOf<List<Token>>(emptyList()) }
    var gameMessage by remember { mutableStateOf("Roll the dice") }

    var selectedMoveOption by remember { mutableStateOf<MoveOption?>(null) }
    var remainingDiceValues by remember { mutableStateOf<List<Int>>(emptyList()) }
    var totalAvailable by remember { mutableStateOf(false) }
    var initializedRoll by remember { mutableStateOf<DiceRoll?>(null) }

    var animatedTokenPositions by remember {
        mutableStateOf<Map<Pair<PlayerColor, Int>, Int>>(emptyMap())
    }

    var die1Display by remember { mutableStateOf(1) }
    var die2Display by remember { mutableStateOf(1) }
    var centerDiceState by remember { mutableStateOf(CenterDiceAnimState.IDLE) }

    fun tokenKey(token: Token): Pair<PlayerColor, Int> = token.color to token.id

    fun displayedPosition(token: Token): Int {
        return animatedTokenPositions[tokenKey(token)] ?: token.position
    }

    fun buildMoveOptions(): List<MoveOption> {
        val options = mutableListOf<MoveOption>()

        remainingDiceValues.forEachIndexed { index, value ->
            options += MoveOption(
                key = "die_${index}_$value",
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

    suspend fun animateAndRollDice() {
        if (isRolling || isAnimatingMove || boardState.gamePhase != GamePhase.ROLLING) return

        isRolling = true
        selectedMoveOption = null
        movableTokens = emptyList()
        selectedToken = null
        remainingDiceValues = emptyList()
        totalAvailable = false
        initializedRoll = null
        animatedTokenPositions = emptyMap()
        gameMessage = "Rolling..."
        SoundManager.playRoll()

        centerDiceState = CenterDiceAnimState.SPLIT

        val finalRoll = rollTwoDice()

        repeat(10) {
            die1Display = Random.nextInt(1, 7)
            die2Display = Random.nextInt(1, 7)
            delay(55)
        }

        delay(120)

        die1Display = finalRoll.die1
        die2Display = finalRoll.die2

        centerDiceState = CenterDiceAnimState.RETURN
        delay(320)
        centerDiceState = CenterDiceAnimState.IDLE

        boardState = handleTurn(boardState, gameRules, finalRoll)
        isRolling = false

        if (boardState.gamePhase == GamePhase.ROLLING &&
            boardState.diceRoll == null &&
            boardState.winner == null
        ) {
            gameMessage = "No valid move. Next player: ${boardState.currentPlayer.name}"
        }
    }

    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(Unit) {
        SoundManager.init(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            SoundManager.release()
        }
    }

    LaunchedEffect(boardState.diceRoll, boardState.gamePhase) {
        if (boardState.gamePhase == GamePhase.MOVING && boardState.diceRoll != null) {
            if (initializedRoll != boardState.diceRoll) {
                initializedRoll = boardState.diceRoll
                remainingDiceValues = listOf(
                    boardState.diceRoll!!.die1,
                    boardState.diceRoll!!.die2
                )
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
            animatedTokenPositions = emptyMap()
            if (!isRolling && !isAnimatingMove && boardState.winner == null) {
                gameMessage = "Roll the dice"
            }
        }
    }

    LaunchedEffect(
        selectedMoveOption,
        boardState.currentPlayer,
        boardState.gamePhase,
        remainingDiceValues
    ) {
        if (boardState.gamePhase == GamePhase.MOVING && !isAnimatingMove) {
            val moveOption = selectedMoveOption
            if (moveOption == null) {
                movableTokens = emptyList()
                gameMessage = "Choose a dice value to use for movement"
            } else {
                val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
                val moveSource =
                    if (moveOption.kind == MoveOptionKind.TOTAL) MoveSource.TOTAL else MoveSource.DIE

                val available = getMovableTokens(
                    currentPlayer,
                    moveOption.value,
                    gameRules,
                    moveSource
                )

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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LudoBoard(
                boardState = boardState,
                gameRules = gameRules,
                selectedToken = selectedToken,
                movableTokens = movableTokens,
                displayedPosition = ::displayedPosition,
                isAnimatingMove = isAnimatingMove,
                onTokenClick = { token ->
                    val moveOption = selectedMoveOption ?: return@LudoBoard
                    if (isAnimatingMove) return@LudoBoard

                    selectedToken = token
                    val beforeMove = boardState
                    val moveSource =
                        if (moveOption.kind == MoveOptionKind.TOTAL) {
                            MoveSource.TOTAL
                        } else {
                            MoveSource.DIE
                        }

                    isAnimatingMove = true

                    scope.launch {
                        val path = buildMovementPath(token, moveOption.value)

                        for (stepPosition in path) {
                            animatedTokenPositions =
                                animatedTokenPositions.toMutableMap().apply {
                                    this[tokenKey(token)] = stepPosition
                                }
                            SoundManager.playMove()
                            delay(160)
                        }

                        val movedState = moveToken(
                            beforeMove,
                            token,
                            moveOption.value,
                            gameRules,
                            moveSource
                        )

                        animatedTokenPositions =
                            animatedTokenPositions.toMutableMap().apply {
                                remove(tokenKey(token))
                            }

                        val didCapture = didCaptureEnemy(
                            beforeMove,
                            movedState,
                            beforeMove.currentPlayer
                        )

                        if (didCapture) {
                            SoundManager.playCapture()
                        }

                        if (movedState.winner != null) {
                            SoundManager.playWin()
                            boardState = movedState.copy(
                                gamePhase = GamePhase.GAME_OVER
                            )
                            gameMessage = "${movedState.winner.name} wins!"
                            selectedMoveOption = null
                            movableTokens = emptyList()
                            remainingDiceValues = emptyList()
                            totalAvailable = false
                            selectedToken = null
                            isAnimatingMove = false
                            return@launch
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

                        val gotCaptureExtraTurn =
                            didCapture &&
                                gameRules.captureReward == CaptureReward.EXTRA_TURN

                        val originalRoll = beforeMove.diceRoll
                        val gotDoubleSixExtraTurn =
                            originalRoll != null &&
                                shouldGrantExtraTurnAfterRoll(
                                    originalRoll,
                                    gameRules
                                )

                        val currentPlayerAfterMove =
                            movedState.players.first {
                                it.color == beforeMove.currentPlayer
                            }

                        val anyRemainingPlayable =
                            newRemainingDiceValues.any { remainingValue ->
                                getMovableTokens(
                                    currentPlayerAfterMove,
                                    remainingValue,
                                    gameRules,
                                    MoveSource.DIE
                                ).isNotEmpty()
                            }

                        remainingDiceValues = newRemainingDiceValues
                        totalAvailable = false

                        boardState = when {
                            newRemainingDiceValues.isNotEmpty() &&
                                anyRemainingPlayable -> {
                                movedState.copy(
                                    currentPlayer = beforeMove.currentPlayer,
                                    gamePhase = GamePhase.MOVING,
                                    diceRoll = beforeMove.diceRoll,
                                    availableMoves = newRemainingDiceValues
                                )
                            }

                            gotCaptureExtraTurn || gotDoubleSixExtraTurn -> {
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
                            newRemainingDiceValues.isNotEmpty() && anyRemainingPlayable -> {
                                "Play the remaining die"
                            }

                            gotCaptureExtraTurn -> {
                                "Capture! Roll again"
                            }

                            gotDoubleSixExtraTurn -> {
                                "Double six! Roll again"
                            }

                            else -> {
                                "Next player: ${boardState.currentPlayer.name}"
                            }
                        }

                        if (gotDoubleSixExtraTurn) {
                            SoundManager.playExtraTurn()
                        }

                        selectedToken = null
                        selectedMoveOption = null
                        movableTokens = emptyList()
                        isAnimatingMove = false
                    }
                },
                centerDiceState = centerDiceState,
                die1Display = die1Display,
                die2Display = die2Display,
                isRolling = isRolling,
                onCenterDiceClick = {
                    if (!isRolling &&
                        !isAnimatingMove &&
                        boardState.gamePhase == GamePhase.ROLLING
                    ) {
                        scope.launch { animateAndRollDice() }
                    }
                }
            )

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
                                animatedTokenPositions = emptyMap()
                                isRolling = false
                                isAnimatingMove = false
                                die1Display = 1
                                die2Display = 1
                                centerDiceState = CenterDiceAnimState.IDLE
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
                                    if (boardState.gamePhase == GamePhase.MOVING &&
                                        !isAnimatingMove
                                    ) {
                                        selectedMoveOption = option
                                    }
                                },
                                isEnabled = boardState.gamePhase == GamePhase.MOVING &&
                                    !isAnimatingMove
                            )
                        }
                    }
                }
            }
        }
    }
}
