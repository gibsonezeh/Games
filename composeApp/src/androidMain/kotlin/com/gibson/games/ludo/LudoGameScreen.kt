package com.gibson.games.ludo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
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
    gameRules: GameRules = GameRules(),
    setupConfig: LudoSetupConfig
) {
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bluetoothMessageManager = remember { BluetoothMessageManager() }

    val isBluetoothGame = setupConfig.mode == LudoMode.BLUETOOTH
    val localPlayerColor = if (isBluetoothGame) {
        BluetoothSessionHolder.localPlayerColor
    } else {
        PlayerColor.GREEN
    }

    fun isLocalPlayersTurn(boardState: BoardState): Boolean {
        return !isBluetoothGame || boardState.currentPlayer == localPlayerColor
    }

    var boardState by remember(gameRules, setupConfig) {
        mutableStateOf(initializeGameState(gameRules, setupConfig))
    }

    var isRolling by remember { mutableStateOf(false) }
    var isAnimatingMove by remember { mutableStateOf(false) }
    var selectedToken by remember { mutableStateOf<Token?>(null) }
    var movableTokens by remember { mutableStateOf<List<Token>>(emptyList()) }
    var gameMessage by remember { mutableStateOf("Roll the dice") }
    var aiBusy by remember { mutableStateOf(false) }

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

    fun currentPlayerState(): Player {
        return boardState.players.first { it.color == boardState.currentPlayer }
    }

    fun currentPlayerName(): String {
        val index = boardState.players.indexOfFirst { it.color == boardState.currentPlayer }
        return setupConfig.playerNames.getOrNull(index) ?: boardState.currentPlayer.name
    }

    fun currentPlayerIsAI(): Boolean = currentPlayerState().isAI

    fun optionToChoice(option: MoveOption): MoveChoice {
        return MoveChoice(
            value = option.value,
            source = if (option.kind == MoveOptionKind.TOTAL) {
                MoveSource.TOTAL
            } else {
                MoveSource.DIE
            }
        )
    }

    fun choiceToMoveOption(choice: MoveChoice): MoveOption {
        return MoveOption(
            key = if (choice.source == MoveSource.TOTAL) {
                "total_${choice.value}"
            } else {
                "die_remote_${choice.value}"
            },
            title = if (choice.source == MoveSource.TOTAL) "Total" else "Die",
            value = choice.value,
            kind = if (choice.source == MoveSource.TOTAL) {
                MoveOptionKind.TOTAL
            } else {
                MoveOptionKind.DIE
            }
        )
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

    fun buildImmediateMoveOptionsForAI(): List<MoveOption> {
        val existing = buildMoveOptions()
        if (existing.isNotEmpty()) return existing

        val roll = boardState.diceRoll ?: return emptyList()

        return listOf(
            MoveOption(
                key = "die_0_${roll.die1}",
                title = "Die 1",
                value = roll.die1,
                kind = MoveOptionKind.DIE
            ),
            MoveOption(
                key = "die_1_${roll.die2}",
                title = "Die 2",
                value = roll.die2,
                kind = MoveOptionKind.DIE
            ),
            MoveOption(
                key = "total_${roll.total}",
                title = "Total",
                value = roll.total,
                kind = MoveOptionKind.TOTAL
            )
        )
    }

    suspend fun animateAndRollDice(
        shouldBroadcastBluetooth: Boolean = true,
        forcedRoll: DiceRoll? = null
    ) {
        if (isRolling || isAnimatingMove || boardState.gamePhase != GamePhase.ROLLING) return

        val finalRoll = forcedRoll ?: rollTwoDice()

        if (isBluetoothGame && shouldBroadcastBluetooth) {
            bluetoothMessageManager.send("ROLL_START:${finalRoll.die1},${finalRoll.die2}")
        }

        isRolling = true
        selectedMoveOption = null
        movableTokens = emptyList()
        selectedToken = null
        remainingDiceValues = emptyList()
        totalAvailable = false
        initializedRoll = null
        animatedTokenPositions = emptyMap()
        gameMessage = if (currentPlayerIsAI()) {
            "${currentPlayerName()} is rolling..."
        } else {
            "Rolling..."
        }

        SoundManager.playRoll()
        centerDiceState = CenterDiceAnimState.SPLIT

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
            gameMessage = "No valid move. Next player: ${currentPlayerName()}"
        }
    }

    suspend fun executeMove(
        token: Token,
        moveOption: MoveOption,
        shouldBroadcastBluetooth: Boolean = true
    ) {
        if (isAnimatingMove) return

        val moveSource =
            if (moveOption.kind == MoveOptionKind.TOTAL) {
                MoveSource.TOTAL
            } else {
                MoveSource.DIE
            }

        if (isBluetoothGame && shouldBroadcastBluetooth) {
            bluetoothMessageManager.send(
                "MOVE_START:${token.color.name},${token.id},${moveOption.value},${moveSource.name}"
            )
        }

        selectedMoveOption = moveOption
        selectedToken = token

        val beforeMove = boardState

        isAnimatingMove = true

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
            return
        }

        val dicePool =
            if (remainingDiceValues.isNotEmpty()) {
                remainingDiceValues
            } else {
                beforeMove.diceRoll?.let { listOf(it.die1, it.die2) } ?: emptyList()
            }

        val newRemainingDiceValues =
            if (moveOption.kind == MoveOptionKind.TOTAL) {
                emptyList()
            } else {
                val temp = dicePool.toMutableList()
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
                    currentPlayer = getNextPlayer(beforeMove.currentPlayer, movedState.players),
                    gamePhase = GamePhase.ROLLING,
                    diceRoll = null,
                    availableMoves = emptyList()
                )
            }
        }

        gameMessage = when {
            newRemainingDiceValues.isNotEmpty() && anyRemainingPlayable -> {
                if (currentPlayerIsAI()) {
                    "${currentPlayerName()} will play the remaining die"
                } else {
                    "Play the remaining die"
                }
            }

            gotCaptureExtraTurn -> {
                "Capture! Roll again"
            }

            gotDoubleSixExtraTurn -> {
                "Double six! Roll again"
            }

            else -> {
                "Next player: ${currentPlayerName()}"
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

    suspend fun performAITurnLoop() {
        if (aiBusy) return
        aiBusy = true

        try {
            while (currentPlayerIsAI() && boardState.winner == null) {
                when (boardState.gamePhase) {
                    GamePhase.ROLLING -> {
                        delay(700)
                        animateAndRollDice()
                        delay(300)
                    }

                    GamePhase.MOVING -> {
                        val options = buildImmediateMoveOptionsForAI()
                        if (options.isEmpty()) break

                        val decision = chooseBestAIMove(
                            player = currentPlayerState(),
                            boardState = boardState,
                            rules = gameRules,
                            choices = options.map { optionToChoice(it) }
                        ) ?: break

                        val option = options.firstOrNull {
                            it.value == decision.choice.value &&
                                ((it.kind == MoveOptionKind.TOTAL && decision.choice.source == MoveSource.TOTAL) ||
                                    (it.kind == MoveOptionKind.DIE && decision.choice.source == MoveSource.DIE))
                        } ?: break

                        selectedMoveOption = option
                        delay(500)
                        executeMove(decision.token, option)
                        delay(300)
                    }

                    GamePhase.GAME_OVER -> break
                }
            }
        } finally {
            aiBusy = false
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
            bluetoothMessageManager.stop()
        }
    }

    LaunchedEffect(isBluetoothGame) {
        if (!isBluetoothGame) return@LaunchedEffect

        bluetoothMessageManager.startListening(
            onMessage = { message ->
                when {
                    message.startsWith("ROLL_START:") -> {
                        val parts = message.removePrefix("ROLL_START:").split(",")
                        if (parts.size == 2) {
                            val d1 = parts[0].toIntOrNull()
                            val d2 = parts[1].toIntOrNull()

                            if (d1 != null && d2 != null) {
                                scope.launch {
                                    animateAndRollDice(
                                        shouldBroadcastBluetooth = false,
                                        forcedRoll = DiceRoll(d1, d2)
                                    )
                                }
                            }
                        }
                    }

                    message.startsWith("MOVE_START:") -> {
                        val parts = message.removePrefix("MOVE_START:").split(",")
                        if (parts.size == 4) {
                            val color = runCatching { PlayerColor.valueOf(parts[0]) }.getOrNull()
                            val tokenId = parts[1].toIntOrNull()
                            val steps = parts[2].toIntOrNull()
                            val source = runCatching { MoveSource.valueOf(parts[3]) }.getOrNull()

                            if (color != null && tokenId != null && steps != null && source != null) {
                                val player = boardState.players.firstOrNull { it.color == color }
                                val token = player?.tokens?.firstOrNull { it.id == tokenId }

                                if (token != null) {
                                    scope.launch {
                                        executeMove(
                                            token = token,
                                            moveOption = choiceToMoveOption(
                                                MoveChoice(
                                                    value = steps,
                                                    source = source
                                                )
                                            ),
                                            shouldBroadcastBluetooth = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    message.startsWith("ROLL:") -> {
                        val parts = message.removePrefix("ROLL:").split(",")
                        if (parts.size == 2) {
                            val d1 = parts[0].toIntOrNull()
                            val d2 = parts[1].toIntOrNull()

                            if (d1 != null && d2 != null) {
                                scope.launch {
                                    animateAndRollDice(
                                        shouldBroadcastBluetooth = false,
                                        forcedRoll = DiceRoll(d1, d2)
                                    )
                                }
                            }
                        }
                    }

                    message.startsWith("MOVE:") -> {
                        val parts = message.removePrefix("MOVE:").split(",")
                        if (parts.size == 4) {
                            val color = runCatching { PlayerColor.valueOf(parts[0]) }.getOrNull()
                            val tokenId = parts[1].toIntOrNull()
                            val steps = parts[2].toIntOrNull()
                            val source = runCatching { MoveSource.valueOf(parts[3]) }.getOrNull()

                            if (color != null && tokenId != null && steps != null && source != null) {
                                val player = boardState.players.firstOrNull { it.color == color }
                                val token = player?.tokens?.firstOrNull { it.id == tokenId }

                                if (token != null) {
                                    scope.launch {
                                        executeMove(
                                            token = token,
                                            moveOption = choiceToMoveOption(
                                                MoveChoice(
                                                    value = steps,
                                                    source = source
                                                )
                                            ),
                                            shouldBroadcastBluetooth = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            onDisconnected = {
                gameMessage = "Bluetooth disconnected"
            }
        )
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
            if (!isRolling && !isAnimatingMove && boardState.winner == null && !aiBusy) {
                gameMessage = if (currentPlayerIsAI()) {
                    "${currentPlayerName()}'s turn"
                } else if (isBluetoothGame && !isLocalPlayersTurn(boardState)) {
                    "${currentPlayerName()}'s turn on remote device"
                } else {
                    "Roll the dice"
                }
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
                gameMessage = when {
                    currentPlayerIsAI() -> "${currentPlayerName()} is thinking..."
                    isBluetoothGame && !isLocalPlayersTurn(boardState) ->
                        "${currentPlayerName()} is choosing on remote device..."
                    else -> "Choose a dice value to use for movement"
                }
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
                        gameMessage = if (isBluetoothGame && !isLocalPlayersTurn(boardState)) {
                            "${currentPlayerName()} is choosing on remote device..."
                        } else {
                            "Choose another dice value"
                        }
                    }
                } else {
                    gameMessage = when {
                        currentPlayerIsAI() -> "${currentPlayerName()} is choosing a move..."
                        isBluetoothGame && !isLocalPlayersTurn(boardState) ->
                            "${currentPlayerName()} is choosing on remote device..."
                        else -> "Select a token to move with ${moveOption.value}"
                    }
                }
            }
        }
    }

    LaunchedEffect(
        boardState.currentPlayer,
        boardState.gamePhase,
        boardState.winner
    ) {
        if (boardState.winner != null) return@LaunchedEffect
        if (!currentPlayerIsAI()) return@LaunchedEffect
        if (aiBusy) return@LaunchedEffect

        performAITurnLoop()
    }

    Box(
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
                    if (currentPlayerIsAI()) return@LudoBoard
                    if (isBluetoothGame && !isLocalPlayersTurn(boardState)) return@LudoBoard

                    val moveOption = selectedMoveOption ?: return@LudoBoard
                    if (isAnimatingMove) return@LudoBoard

                    scope.launch {
                        executeMove(token, moveOption)
                    }
                },
                centerDiceState = centerDiceState,
                die1Display = die1Display,
                die2Display = die2Display,
                isRolling = isRolling,
                onCenterDiceClick = {
                    if (currentPlayerIsAI()) return@LudoBoard
                    if (isBluetoothGame && !isLocalPlayersTurn(boardState)) return@LudoBoard

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
                                boardState = initializeGameState(gameRules, setupConfig)
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
                                aiBusy = false
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
                    text = if (isBluetoothGame) {
                        "Current Player: ${currentPlayerName()} (${boardState.currentPlayer.name})"
                    } else {
                        "Current Player: ${currentPlayerName()}"
                    },
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
                                    if (!currentPlayerIsAI() &&
                                        (!isBluetoothGame || isLocalPlayersTurn(boardState)) &&
                                        boardState.gamePhase == GamePhase.MOVING &&
                                        !isAnimatingMove
                                    ) {
                                        selectedMoveOption = option
                                    }
                                },
                                isEnabled = !currentPlayerIsAI() &&
                                    (!isBluetoothGame || isLocalPlayersTurn(boardState)) &&
                                    boardState.gamePhase == GamePhase.MOVING &&
                                    !isAnimatingMove
                            )
                        }
                    }
                }
            }
        }
    }
}
