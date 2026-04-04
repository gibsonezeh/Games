package com.gibson.games.ludo

import kotlin.random.Random

/**
 * Core Ludo game logic.
 *
 * UI position model:
 * -1       -> token is in base
 * 0..51    -> token is on the shared board path
 * 100..105 -> token is in the home path
 * 200      -> token has finished
 *
 * Internal movement model:
 * -1       -> in base
 * 0..51    -> main loop progress
 * 52..57   -> home path progress
 * 58       -> finished
 */
enum class PlayerColor {
    GREEN, RED, YELLOW, BLUE
}

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
    val getsExtraTurnOnThreeSixesForfeit: Boolean = true,
    val mustPlayRolledNumbers: Boolean = true,
    val capturedTokenReturnsToBase: Boolean = true,
    val captureGivesExtraTurn: Boolean = true,
    val captureSendsToHome: Boolean = false,
    val startingPointIsSafeZoneForColor: Boolean = true,
    val startingPointIsSafeZoneForAll: Boolean = false
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

/**
 * These indices must match the board path in getTokenCoordinates().
 *
 * 0  -> Green start at (1,6)
 * 13 -> Red start at (8,1)
 * 26 -> Blue start at (13,8)
 * 39 -> Yellow start at (6,13)
 */
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
        PlayerColor.RED -> PlayerColor.YELLOW
        PlayerColor.YELLOW -> PlayerColor.BLUE
        PlayerColor.BLUE -> PlayerColor.GREEN
    }
}

/**
 * Converts a UI position into relative progress for the token's own color.
 */
private fun getRelativeProgress(token: Token): Int {
    return when (val pos = token.position) {
        -1 -> -1
        200 -> 58
        in 100..105 -> 52 + (pos - 100)
        in 0..51 -> {
            val start = getStartingPosition(token.color)
            (pos - start + 52) % 52
        }
        else -> -1
    }
}

/**
 * Converts a relative progress value back into the UI position model.
 */
private fun getPositionFromProgress(color: PlayerColor, progress: Int): Int {
    return when {
        progress < 0 -> -1
        progress in 0..51 -> (getStartingPosition(color) + progress) % 52
        progress in 52..57 -> 100 + (progress - 52)
        progress >= 58 -> 200
        else -> -1
    }
}

/**
 * Safe zones aligned to the corrected board path.
 */
fun isSafeZone(position: Int, color: PlayerColor, rules: GameRules): Boolean {
    val mainPathSafeZones = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    val startingPoints = mapOf(
        PlayerColor.GREEN to 0,
        PlayerColor.RED to 13,
        PlayerColor.BLUE to 26,
        PlayerColor.YELLOW to 39
    )

    return when {
        position == 200 -> true
        position in 100..105 -> true
        rules.startingPointIsSafeZoneForAll && position in startingPoints.values -> true
        rules.startingPointIsSafeZoneForColor && startingPoints[color] == position -> true
        position in mainPathSafeZones -> true
        else -> false
    }
}

fun isValidMove(token: Token, steps: Int, rules: GameRules): Boolean {
    if (steps <= 0) return false

    val progress = getRelativeProgress(token)

    return when (progress) {
        -1 -> !rules.requiresSixToExitBase || steps == 6
        58 -> false
        else -> progress + steps <= 58
    }
}

fun getMovableTokens(player: Player, diceValue: Int, rules: GameRules): List<Token> {
    return player.tokens.filter { token ->
        isValidMove(token, diceValue, rules)
    }
}

fun moveToken(boardState: BoardState, token: Token, steps: Int, rules: GameRules): BoardState {
    if (!isValidMove(token, steps, rules)) {
        return boardState
    }

    val currentProgress = getRelativeProgress(token)
    val newProgress = when (currentProgress) {
        -1 -> 0
        else -> currentProgress + steps
    }

    val newPosition = getPositionFromProgress(token.color, newProgress)
    val movedToken = token.copy(position = newPosition)

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
                val updatedTokens = player.tokens.map { enemyToken ->
                    val shouldCapture = newPosition in 0..51 &&
                        enemyToken.position == newPosition &&
                        enemyToken.color != token.color &&
                        !isSafeZone(newPosition, enemyToken.color, rules) &&
                        rules.capturedTokenReturnsToBase

                    if (shouldCapture) {
                        enemyToken.copy(position = -1)
                    } else {
                        enemyToken
                    }
                }

                player.copy(tokens = updatedTokens)
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

private fun computeAvailableMoves(diceRoll: DiceRoll): List<Int> {
    val moves = mutableListOf<Int>()

    if (diceRoll.die1 == 6 || diceRoll.die2 == 6) {
        moves += diceRoll.die1
        moves += diceRoll.die2
    } else {
        moves += diceRoll.die1
        moves += diceRoll.die2
        moves += diceRoll.total
    }

    return moves.distinct()
}

fun handleTurn(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val availableMoves = computeAvailableMoves(diceRoll)
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

    return boardState.copy(
        players = updatedPlayers,
        diceRoll = diceRoll,
        gamePhase = if (winner != null) GamePhase.GAME_OVER else GamePhase.MOVING,
        winner = winner,
        availableMoves = availableMoves
    )
}

fun selectBestToken(player: Player, diceRoll: Int, rules: GameRules): Token? {
    val movableTokens = getMovableTokens(player, diceRoll, rules)
    if (movableTokens.isEmpty()) return null

    val finishingToken = movableTokens.firstOrNull { token ->
        val progress = getRelativeProgress(token)
        progress != -1 && progress + diceRoll == 58
    }
    if (finishingToken != null) return finishingToken

    val baseToken = movableTokens.firstOrNull { it.position == -1 }
    if (baseToken != null) return baseToken

    val homePathToken = movableTokens
        .filter { it.position in 100..105 }
        .maxByOrNull { getRelativeProgress(it) }
    if (homePathToken != null) return homePathToken

    return movableTokens.maxByOrNull { getRelativeProgress(it) }
}

fun performAutomaticMove(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val moveOptions = computeAvailableMoves(diceRoll)

    for (moveValue in moveOptions.sortedDescending()) {
        val selectedToken = selectBestToken(currentPlayer, moveValue, rules)
        if (selectedToken != null) {
            return moveToken(boardState, selectedToken, moveValue, rules)
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
            58 -> 100
            in 52..57 -> 70 + ((progress - 52) * 5)
            else -> progress
        }
    }
}

fun getGameProgress(boardState: BoardState): Map<PlayerColor, Float> {
    val maxPerToken = 58f
    val maxPerPlayer = maxPerToken * 4f

    return boardState.players.associate { player ->
        val totalProgress = player.tokens.sumOf { token ->
            when (val progress = getRelativeProgress(token)) {
                -1 -> 0
                58 -> 58
                else -> progress
            }
        }.toFloat()

        player.color to (totalProgress / maxPerPlayer)
    }
}
