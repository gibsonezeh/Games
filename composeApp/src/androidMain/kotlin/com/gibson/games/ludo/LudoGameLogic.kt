package com.gibson.games.ludo

import kotlin.random.Random

enum class PlayerColor {
    GREEN, RED, YELLOW, BLUE
}

enum class CapturePenalty {
    NONE,
    RETURN_TO_BASE,
    MOVE_BACK_5
}

enum class CaptureReward {
    NONE,
    GO_HOME,
    EXTRA_TURN
}

enum class MoveSource {
    DIE,
    TOTAL
}

data class MoveChoice(
    val value: Int,
    val source: MoveSource
)

data class AIDecision(
    val choice: MoveChoice,
    val token: Token
)

data class Token(
    val id: Int,
    val color: PlayerColor,
    val position: Int
)

data class Player(
    val color: PlayerColor,
    val tokens: List<Token>,
    val isAI: Boolean = false,
    val hasRolled: Boolean = false,
    val diceRolls: List<Int> = emptyList()
)

data class BoardState(
    val players: List<Player>,
    val currentPlayer: PlayerColor,
    val diceRoll: DiceRoll? = null,
    val gamePhase: GamePhase = GamePhase.ROLLING,
    val winner: PlayerColor? = null,
    val availableMoves: List<Int> = emptyList()
)

data class DiceRoll(
    val die1: Int,
    val die2: Int,
    val total: Int = die1 + die2
)

enum class GamePhase {
    ROLLING,
    MOVING,
    GAME_OVER
}

data class GameRules(
    val requiresSixToExitBase: Boolean = true,
    val getsExtraTurnOnSix: Boolean = true,
    val getsExtraTurnOnDoubleSix: Boolean = true,
    val getsExtraTurnOnThreeSixesForfeit: Boolean = true,
    val mustPlayRolledNumbers: Boolean = true,
    val capturedTokenReturnsToBase: Boolean = true,
    val captureReward: CaptureReward = CaptureReward.EXTRA_TURN,
    val capturePenalty: CapturePenalty = CapturePenalty.RETURN_TO_BASE,
    val captureSendsToHome: Boolean = false,
    val startingPointIsSafeZoneForColor: Boolean = true,
    val startingPointIsSafeZoneForAll: Boolean = false
)

private fun getActiveColors(playerCount: Int): List<PlayerColor> {
    return when (playerCount) {
        1 -> listOf(PlayerColor.GREEN)
        2 -> listOf(PlayerColor.GREEN, PlayerColor.RED)
        3 -> listOf(PlayerColor.GREEN, PlayerColor.RED, PlayerColor.BLUE)
        else -> listOf(
            PlayerColor.GREEN,
            PlayerColor.RED,
            PlayerColor.BLUE,
            PlayerColor.YELLOW
        )
    }
}

fun initializeGameState(
    rules: GameRules = GameRules(),
    setupConfig: LudoSetupConfig? = null
): BoardState {
    val activeColors = getActiveColors(setupConfig?.playerCount ?: 4)

    val players = activeColors.mapIndexed { index, color ->
        val tokens = (1..4).map { id ->
            Token(id = id, color = color, position = -1)
        }

        val isAIPlayer = when (setupConfig?.mode) {
            LudoMode.PLAY_VS_AI -> index == 1
            else -> false
        }

        Player(
            color = color,
            tokens = tokens,
            isAI = isAIPlayer
        )
    }

    return BoardState(
        players = players,
        currentPlayer = activeColors.first(),
        gamePhase = GamePhase.ROLLING
    )
}

fun rollDice(): Int = Random.nextInt(1, 7)

fun rollTwoDice(): DiceRoll {
    val die1 = Random.nextInt(1, 7)
    val die2 = Random.nextInt(1, 7)
    return DiceRoll(die1, die2)
}

fun getStartingPosition(color: PlayerColor): Int {
    return when (color) {
        PlayerColor.GREEN -> 0
        PlayerColor.RED -> 13
        PlayerColor.BLUE -> 26
        PlayerColor.YELLOW -> 39
    }
}

fun getNextPlayer(
    currentPlayer: PlayerColor,
    players: List<Player>
): PlayerColor {
    if (players.isEmpty()) return currentPlayer

    val currentIndex = players.indexOfFirst { it.color == currentPlayer }
    if (currentIndex == -1) return players.first().color

    val nextIndex = (currentIndex + 1) % players.size
    return players[nextIndex].color
}

fun getNextPlayer(currentPlayer: PlayerColor): PlayerColor {
    return when (currentPlayer) {
        PlayerColor.GREEN -> PlayerColor.RED
        PlayerColor.RED -> PlayerColor.BLUE
        PlayerColor.BLUE -> PlayerColor.YELLOW
        PlayerColor.YELLOW -> PlayerColor.GREEN
    }
}

/**
 * Internal progress model:
 * -1      -> base
 * 0..50   -> outer path
 * 51..55  -> 5 home-lane steps
 * 56      -> finished
 */
private fun getRelativeProgress(token: Token): Int {
    return when (val pos = token.position) {
        -1 -> -1
        200 -> 56
        in 100..104 -> 51 + (pos - 100)
        in 0..51 -> {
            val start = getStartingPosition(token.color)
            val raw = (pos - start + 52) % 52
            if (raw == 51) 50 else raw
        }
        else -> -1
    }
}

private fun getPositionFromProgress(color: PlayerColor, progress: Int): Int {
    return when {
        progress < 0 -> -1
        progress in 0..50 -> (getStartingPosition(color) + progress) % 52
        progress in 51..55 -> 100 + (progress - 51)
        progress >= 56 -> 200
        else -> -1
    }
}

private fun predictLandingPosition(
    token: Token,
    steps: Int,
    rules: GameRules,
    moveSource: MoveSource = MoveSource.DIE
): Int? {
    if (!isValidMove(token, steps, rules, moveSource)) return null

    val currentProgress = getRelativeProgress(token)
    val newProgress = when (currentProgress) {
        -1 -> 0
        else -> currentProgress + steps
    }

    return getPositionFromProgress(token.color, newProgress)
}

fun isSafeZone(position: Int, color: PlayerColor, rules: GameRules): Boolean {
    val mainPathSafeZones = setOf(8, 21, 34, 47)

    val startingPoints = mapOf(
        PlayerColor.GREEN to 0,
        PlayerColor.RED to 13,
        PlayerColor.BLUE to 26,
        PlayerColor.YELLOW to 39
    )

    return when {
        position == 200 -> true
        position in 100..104 -> true
        rules.startingPointIsSafeZoneForAll && position in startingPoints.values -> true
        rules.startingPointIsSafeZoneForColor && startingPoints[color] == position -> true
        position in mainPathSafeZones -> true
        else -> false
    }
}

private fun canExitBase(
    steps: Int,
    rules: GameRules,
    moveSource: MoveSource
): Boolean {
    return !rules.requiresSixToExitBase ||
        (moveSource == MoveSource.DIE && steps == 6)
}

fun isValidMove(
    token: Token,
    steps: Int,
    rules: GameRules,
    moveSource: MoveSource = MoveSource.DIE
): Boolean {
    if (steps <= 0) return false

    val progress = getRelativeProgress(token)

    return when (progress) {
        -1 -> canExitBase(steps, rules, moveSource)
        56 -> false
        else -> progress + steps <= 56
    }
}

fun isValidMove(
    token: Token,
    steps: Int,
    rules: GameRules
): Boolean = isValidMove(token, steps, rules, MoveSource.DIE)

fun getMovableTokens(
    player: Player,
    diceValue: Int,
    rules: GameRules,
    moveSource: MoveSource = MoveSource.DIE
): List<Token> {
    return player.tokens.filter { token ->
        isValidMove(token, diceValue, rules, moveSource)
    }
}

fun getMovableTokens(
    player: Player,
    diceValue: Int,
    rules: GameRules
): List<Token> = getMovableTokens(player, diceValue, rules, MoveSource.DIE)

fun moveToken(
    boardState: BoardState,
    token: Token,
    steps: Int,
    rules: GameRules,
    moveSource: MoveSource = MoveSource.DIE
): BoardState {
    if (!isValidMove(token, steps, rules, moveSource)) {
        return boardState
    }

    val currentProgress = getRelativeProgress(token)
    val newProgress = when (currentProgress) {
        -1 -> 0
        else -> currentProgress + steps
    }

    val landingPosition = getPositionFromProgress(token.color, newProgress)

    val capturedEnemies = boardState.players
        .filter { it.color != token.color }
        .flatMap { player -> player.tokens }
        .filter { enemyToken ->
            landingPosition in 0..51 &&
                enemyToken.position == landingPosition &&
                !isSafeZone(landingPosition, enemyToken.color, rules)
        }

    val didCapture = capturedEnemies.isNotEmpty()

    val movedToken = when {
        didCapture && rules.captureReward == CaptureReward.GO_HOME -> {
            token.copy(position = 200)
        }
        else -> {
            token.copy(position = landingPosition)
        }
    }

    val updatedPlayers = boardState.players.map { player ->
        when {
            player.color == token.color -> {
                player.copy(
                    tokens = player.tokens.map { existing ->
                        if (existing.id == token.id) movedToken else existing
                    }
                )
            }

            else -> {
                player.copy(
                    tokens = player.tokens.map { enemyToken ->
                        val wasCaptured = capturedEnemies.any {
                            it.color == enemyToken.color && it.id == enemyToken.id
                        }

                        if (!wasCaptured) {
                            enemyToken
                        } else {
                            when (rules.capturePenalty) {
                                CapturePenalty.RETURN_TO_BASE -> {
                                    enemyToken.copy(position = -1)
                                }

                                CapturePenalty.MOVE_BACK_5 -> {
                                    val enemyProgress = getRelativeProgress(enemyToken)
                                    val movedBackProgress = (enemyProgress - 5).coerceAtLeast(0)
                                    val movedBackPosition = getPositionFromProgress(
                                        enemyToken.color,
                                        movedBackProgress
                                    )
                                    enemyToken.copy(position = movedBackPosition)
                                }

                                CapturePenalty.NONE -> {
                                    enemyToken
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    val updatedBoard = boardState.copy(players = updatedPlayers)
    val winner = checkForWinner(updatedBoard)

    return if (winner != null) {
        updatedBoard.copy(
            winner = winner,
            gamePhase = GamePhase.GAME_OVER
        )
    } else {
        updatedBoard
    }
}

fun moveToken(
    boardState: BoardState,
    token: Token,
    steps: Int,
    rules: GameRules
): BoardState = moveToken(boardState, token, steps, rules, MoveSource.DIE)

fun getRollMoveChoices(diceRoll: DiceRoll): List<MoveChoice> {
    return listOf(
        MoveChoice(diceRoll.die1, MoveSource.DIE),
        MoveChoice(diceRoll.die2, MoveSource.DIE),
        MoveChoice(diceRoll.total, MoveSource.TOTAL)
    )
}

fun hasAnyPlayableMove(
    player: Player,
    diceRoll: DiceRoll,
    rules: GameRules
): Boolean {
    return getRollMoveChoices(diceRoll).any { choice ->
        getMovableTokens(player, choice.value, rules, choice.source).isNotEmpty()
    }
}

fun shouldGrantExtraTurnAfterRoll(
    diceRoll: DiceRoll,
    rules: GameRules
): Boolean {
    return rules.getsExtraTurnOnDoubleSix &&
        diceRoll.die1 == 6 &&
        diceRoll.die2 == 6
}

fun handleTurn(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val winner = checkForWinner(boardState)

    val updatedPlayers = boardState.players.map { player ->
        if (player.color == currentPlayer.color) {
            player.copy(
                hasRolled = true,
                diceRolls = player.diceRolls + listOf(diceRoll.die1, diceRoll.die2)
            )
        } else {
            player
        }
    }

    if (winner != null) {
        return boardState.copy(
            players = updatedPlayers,
            winner = winner,
            gamePhase = GamePhase.GAME_OVER
        )
    }

    val playerAfterUpdate = updatedPlayers.first { it.color == boardState.currentPlayer }
    val hasMove = hasAnyPlayableMove(playerAfterUpdate, diceRoll, rules)

    return if (!hasMove) {
        boardState.copy(
            players = updatedPlayers,
            currentPlayer = getNextPlayer(boardState.currentPlayer, updatedPlayers),
            diceRoll = null,
            gamePhase = GamePhase.ROLLING,
            availableMoves = emptyList()
        )
    } else {
        boardState.copy(
            players = updatedPlayers,
            diceRoll = diceRoll,
            gamePhase = GamePhase.MOVING,
            availableMoves = listOf(diceRoll.die1, diceRoll.die2, diceRoll.total)
        )
    }
}

fun selectBestToken(
    player: Player,
    diceRoll: Int,
    rules: GameRules,
    moveSource: MoveSource = MoveSource.DIE
): Token? {
    val movableTokens = getMovableTokens(player, diceRoll, rules, moveSource)
    if (movableTokens.isEmpty()) return null

    val finishingToken = movableTokens.firstOrNull { token ->
        val progress = getRelativeProgress(token)
        progress != -1 && progress + diceRoll == 56
    }
    if (finishingToken != null) return finishingToken

    val baseToken = movableTokens.firstOrNull { it.position == -1 }
    if (baseToken != null) return baseToken

    val homePathToken = movableTokens
        .filter { it.position in 100..104 }
        .maxByOrNull { getRelativeProgress(it) }
    if (homePathToken != null) return homePathToken

    return movableTokens.maxByOrNull { getRelativeProgress(it) }
}

private fun scoreAIMove(
    token: Token,
    landingPosition: Int,
    boardState: BoardState,
    rules: GameRules,
    choice: MoveChoice
): Int {
    var score = 0

    if (token.position == -1) {
        score += 60
    }

    if (landingPosition == 200) {
        score += 120
    }

    if (landingPosition in 100..104) {
        score += 50 + (landingPosition - 100) * 5
    }

    if (canCaptureAt(landingPosition, token.color, rules, boardState)) {
        score += 90
    }

    if (isSafeZone(landingPosition, token.color, rules)) {
        score += 25
    }

    score += when (choice.source) {
        MoveSource.TOTAL -> 8
        MoveSource.DIE -> 4
    }

    score += choice.value
    score += getRelativeProgress(token).coerceAtLeast(0)

    return score
}

fun chooseBestAIMove(
    player: Player,
    boardState: BoardState,
    rules: GameRules,
    choices: List<MoveChoice>
): AIDecision? {
    var bestDecision: AIDecision? = null
    var bestScore = Int.MIN_VALUE

    for (choice in choices) {
        val movableTokens = getMovableTokens(player, choice.value, rules, choice.source)

        for (token in movableTokens) {
            val landingPosition = predictLandingPosition(token, choice.value, rules, choice.source)
                ?: continue

            val score = scoreAIMove(
                token = token,
                landingPosition = landingPosition,
                boardState = boardState,
                rules = rules,
                choice = choice
            )

            if (score > bestScore) {
                bestScore = score
                bestDecision = AIDecision(choice = choice, token = token)
            }
        }
    }

    return bestDecision
}

fun performAutomaticMove(
    boardState: BoardState,
    rules: GameRules,
    diceRoll: DiceRoll
): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val decision = chooseBestAIMove(
        player = currentPlayer,
        boardState = boardState.copy(diceRoll = diceRoll),
        rules = rules,
        choices = getRollMoveChoices(diceRoll)
    )

    return if (decision != null) {
        moveToken(
            boardState = boardState,
            token = decision.token,
            steps = decision.choice.value,
            rules = rules,
            moveSource = decision.choice.source
        )
    } else {
        boardState
    }
}

fun checkForWinner(boardState: BoardState): PlayerColor? {
    return boardState.players.firstOrNull { player ->
        player.tokens.all { it.position == 200 }
    }?.color
}

fun getTokensAtPosition(boardState: BoardState, position: Int): List<Token> {
    return boardState.players
        .flatMap { it.tokens }
        .filter { it.position == position }
}

fun canCaptureAt(
    position: Int,
    attackingColor: PlayerColor,
    rules: GameRules,
    boardState: BoardState
): Boolean {
    if (position !in 0..51) return false

    return getTokensAtPosition(boardState, position).any { token ->
        token.color != attackingColor &&
            !isSafeZone(position, token.color, rules)
    }
}

fun getPlayerScore(player: Player): Int {
    return player.tokens.sumOf { token ->
        when (val progress = getRelativeProgress(token)) {
            -1 -> 0
            56 -> 100
            in 51..55 -> 70 + ((progress - 51) * 5)
            else -> progress
        }
    }
}

fun getGameProgress(boardState: BoardState): Map<PlayerColor, Float> {
    val maxPerToken = 56f
    val maxPerPlayer = maxPerToken * 4f

    return boardState.players.associate { player ->
        val totalProgress = player.tokens.sumOf { token ->
            when (val progress = getRelativeProgress(token)) {
                -1 -> 0
                56 -> 56
                else -> progress
            }
        }.toFloat()

        player.color to (totalProgress / maxPerPlayer)
    }
}
