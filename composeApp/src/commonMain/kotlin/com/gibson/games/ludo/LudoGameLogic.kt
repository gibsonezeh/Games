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

/**
 * Source of a move value.
 *
 * DIE   -> came from a real die face
 * TOTAL -> came from die1 + die2
 */
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

    /**
     * Kept for compatibility with your existing settings screen.
     * The gameplay logic below now uses getsExtraTurnOnDoubleSix instead.
     */
    val getsExtraTurnOnSix: Boolean = true,

    /**
     * New rule: extra turn only if the roll is 6 and 6.
     */
    val getsExtraTurnOnDoubleSix: Boolean = true,

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
 * These start indices must match the board path in getTokenCoordinates().
 *
 * 0  -> Green start
 * 13 -> Red start
 * 26 -> Blue start
 * 39 -> Yellow start
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
    val mainPathSafeZones = setOf(8, 21, 34, 47)

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

/**
 * Base exit is only allowed by a real die = 6.
 * Total = 6 is NOT allowed to bring a token out of base.
 */
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
        58 -> false
        else -> progress + steps <= 58
    }
}

/**
 * Backward-compatible overload.
 */
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

/**
 * Backward-compatible overload.
 */
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

/**
 * Backward-compatible overload.
 */
fun moveToken(
    boardState: BoardState,
    token: Token,
    steps: Int,
    rules: GameRules
): BoardState = moveToken(boardState, token, steps, rules, MoveSource.DIE)

/**
 * Returns all legal move choices for a roll.
 *
 * Includes:
 * - die1 as a DIE move
 * - die2 as a DIE move
 * - total as a TOTAL move
 */
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

/**
 * Extra turn now means true double six only.
 */
fun shouldGrantExtraTurnAfterRoll(
    diceRoll: DiceRoll,
    rules: GameRules
): Boolean {
    return rules.getsExtraTurnOnDoubleSix &&
        diceRoll.die1 == 6 &&
        diceRoll.die2 == 6
}

/**
 * Handles the roll phase and prepares the board for movement selection.
 *
 * If the current player has no valid move at all, the turn auto-passes
 * to the next player immediately.
 */
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
