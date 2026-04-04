package com.gibson.games.ludo

import kotlin.random.Random

/**
 * Core Ludo game logic.
 *
 * Position model used by the UI:
 * -1      -> token is in base
 * 0..51   -> token is on the shared board path
 * 100..105 -> token is in the home path
 * 200     -> token has finished
 *
 * Internally, movement is computed using relative progress per color:
 * -1      -> in base
 * 0..51   -> on main loop
 * 52..57  -> in home path
 * 58      -> finished
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
 * Preserves your original board layout starting positions.
 */
fun getStartingPosition(color: PlayerColor): Int {
    return when (color) {
        PlayerColor.GREEN -> 1
        PlayerColor.RED -> 14
        PlayerColor.YELLOW -> 27
        PlayerColor.BLUE -> 40
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
 * Converts a UI position into relative movement progress for that token color.
 *
 * Relative progress:
 * -1      -> base
 * 0..51   -> main loop
 * 52..57  -> home path
 * 58      -> finished
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
 * Converts relative progress back into the UI position model.
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
 * Returns true if the given board position is protected from capture.
 */
fun isSafeZone(position: Int, color: PlayerColor, rules: GameRules): Boolean {
    val mainPathSafeZones = setOf(1, 9, 14, 22, 27, 35, 40, 48)

    val startingPoints = mapOf(
        PlayerColor.GREEN to 1,
        PlayerColor.RED to 14,
        PlayerColor.YELLOW to 27,
        PlayerColor.BLUE to 40
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

/**
 * Returns whether a token can be moved by the given number of steps.
 */
fun isValidMove(token: Token, steps: Int, rules: GameRules): Boolean {
    if (steps <= 0) return false

    val progress = getRelativeProgress(token)

    return when (progress) {
        -1 -> !rules.requiresSixToExitBase || steps == 6
        58 -> false
        else -> progress + steps <= 58
    }
}

/**
 * Returns all tokens of a player that can legally move with the given dice value.
 */
fun getMovableTokens(player: Player, diceValue: Int, rules: GameRules): List<Token> {
    return player.tokens.filter { token ->
        isValidMove(token, diceValue, rules)
    }
}

/**
 * Moves a token immutably and applies capture logic if needed.
 */
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

    var capturedAny = false

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
                        capturedAny = true
                        enemyToken.copy(position = -1)
                    } else {
                        enemyToken
                    }
                }

                player.copy(tokens = updatedTokens)
            }
        }
    }

    val movedBoardState = boardState.copy(
        players = updatedPlayers,
        winner = checkForWinner(boardState.copy(players = updatedPlayers))
    )

    return if (movedBoardState.winner != null) {
        movedBoardState.copy(gamePhase = GamePhase.GAME_OVER)
    } else {
        movedBoardState
    }
}

/**
 * Computes available move values from a two-dice roll.
 *
 * Current rule model:
 * - If neither die is 6: available moves are die1, die2, total
 * - If one or both dice are 6: allow each die value separately
 *
 * This keeps compatibility with your current UI flow.
 */
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

/**
 * Handles the roll phase and prepares the board for movement selection.
 *
 * Turn advancement is intentionally not forced here because your UI currently
 * expects to stay on the same player while selecting a move.
 */
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

/**
 * Simple AI token selection.
 *
 * Priority:
 * 1. Finish a token if possible
 * 2. Move a token out of base
 * 3. Move token in home path
 * 4. Move most advanced token
 */
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

/**
 * Simplified AI move. Uses the best available move value and best token for that move.
 */
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

/**
 * Score based on relative progress rather than raw board index.
 */
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

/**
 * Returns progress from 0f..1f per player.
 */
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
