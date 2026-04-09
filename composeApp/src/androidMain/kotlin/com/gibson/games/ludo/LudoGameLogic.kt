package com.gibson.games.ludo

import kotlin.random.Random

enum class PlayerColor {
    GREEN, RED, YELLOW, BLUE
}

enum class CapturePenalty{
    NONE,
    RETURN_TO_BASE,
    MOVE_BACK_5
}

enum class CaptureReward{
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

data class Token(
    val id: Int,
    val color: PlayerColor,
    val position: Int
)

data class Player(
    val color: PlayerColor,
    val tokens: List<Token>,
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
    val captureGivesExtraTurn: Boolean = true,
    val captureReward: CaptureReward = CaptureReward.EXTRA_TURN,
    val capturePenalty: CapturePenalty = CapturePenalty.RETURN_TO_BASE,
    val captureSendsToHome: Boolean = false,
    val startingPointIsSafeZoneForColor: Boolean = true,
    val startingPointIsSafeZoneForAll: Boolean = false,
)

fun initializeGameState(rules: GameRules = GameRules()): BoardState {
    val players = PlayerColor.entries.map { color ->
        val tokens = (1..4).map { id ->
            Token(id = id, color = color, position = -1)
        }
        Player(color = color, tokens = tokens)
    }

    return BoardState(
        players = players,
        currentPlayer = PlayerColor.GREEN,
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

            // Skip the extra outer tile before entering the colored home lane
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
            currentPlayer = getNextPlayer(boardState.currentPlayer),
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

fun performAutomaticMove(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val choices = getRollMoveChoices(diceRoll)

    for (choice in choices.sortedByDescending { it.value }) {
        val selectedToken = selectBestToken(currentPlayer, choice.value, rules, choice.source)
        if (selectedToken != null) {
            return moveToken(boardState, selectedToken, choice.value, rules, choice.source)
        }
    }

    return boardState
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
