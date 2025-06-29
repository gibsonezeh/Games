package com.gibson.games.ludo

import kotlin.random.Random

// This file contains the complete core game logic for Ludo.

enum class PlayerColor {
    GREEN, RED, YELLOW, BLUE
}

data class Token(
    val id: Int,
    val color: PlayerColor,
    var position: Int // 0-51 for main path, -1 for home, 100+ for home path, 200 for finished
)

data class Player(
    val color: PlayerColor,
    val tokens: List<Token>,
    var hasRolled: Boolean = false,
    val diceRolls: MutableList<Int> = mutableListOf()
)

data class BoardState(
    val players: List<Player>,
    val currentPlayer: PlayerColor,
    val diceRoll: Int? = null,
    val gamePhase: GamePhase = GamePhase.ROLLING,
    val winner: PlayerColor? = null
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
    val players = PlayerColor.values().map { color ->
        val tokens = (1..4).map { id -> Token(id, color, -1) } // -1 indicates token is in home base
        Player(color, tokens)
    }
    return BoardState(players, PlayerColor.GREEN) // Green starts first
}

fun rollDice(): Int {
    return Random.nextInt(1, 7)
}

fun rollTwoDice(): DiceRoll {
    val die1 = Random.nextInt(1, 7)
    val die2 = Random.nextInt(1, 7)
    return DiceRoll(die1, die2)
}

fun moveToken(boardState: BoardState, token: Token, steps: Int, rules: GameRules): BoardState {
    val newPosition = when {
        token.position == -1 -> {
            // Token is in home base, move to starting position
            getStartingPosition(token.color)
        }
        token.position in 0..51 -> {
            // Token is on main path
            val nextPos = token.position + steps
            if (nextPos > 51) {
                // Move to home path
                100 + (nextPos - 52)
            } else {
                nextPos
            }
        }
        token.position in 100..105 -> {
            // Token is in home path
            val homePathPos = token.position - 100 + steps
            if (homePathPos >= 6) {
                200 // Finished
            } else {
                100 + homePathPos
            }
        }
        else -> token.position // Already finished or invalid position
    }

    var updatedBoardState = boardState.copy()

    // Check for capture (only on main path, not in home areas or home path)
    if (newPosition in 0..51) {
        val capturedToken = updatedBoardState.players.flatMap { it.tokens }
            .firstOrNull { 
                it.position == newPosition && 
                it.color != token.color && 
                !isSafeZone(newPosition, it.color, rules) 
            }

        if (capturedToken != null && rules.capturedTokenReturnsToBase) {
            capturedToken.position = -1 // Send captured token back to base
            updatedBoardState = updatedBoardState.copy(players = updatedBoardState.players.map { player ->
                if (player.color == capturedToken.color) {
                    player.copy(tokens = player.tokens.map { if (it.id == capturedToken.id) capturedToken else it })
                } else {
                    player
                }
            })
        }
    }

    // Update token position
    token.position = newPosition

    return updatedBoardState.copy(players = updatedBoardState.players.map { player ->
        if (player.color == token.color) {
            player.copy(tokens = player.tokens.map { if (it.id == token.id) token else it })
        } else {
            player
        }
    })
}

fun handleTurn(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    
    // Check if player can move any tokens
    val movableTokens = getMovableTokens(currentPlayer, diceRoll.total, rules)
    
    var nextPlayer = boardState.currentPlayer
    val newDiceRolls = currentPlayer.diceRolls.toMutableList()
    newDiceRolls.add(diceRoll.total)

    // Determine next player based on rules
    if (rules.getsExtraTurnOnSix && (diceRoll.die1 == 6 || diceRoll.die2 == 6)) {
        // Player gets another turn if they rolled a 6
        nextPlayer = boardState.currentPlayer
    } else if (rules.getsExtraTurnOnThreeSixesForfeit && 
               newDiceRolls.takeLast(3).all { it >= 6 } && 
               newDiceRolls.size >= 3) {
        // Forfeit turn for three consecutive high rolls
        newDiceRolls.clear()
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    } else if (movableTokens.isEmpty()) {
        // No movable tokens, forfeit turn
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    } else {
        // Normal turn progression
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    }

    // Check for winner
    val winner = checkForWinner(boardState)

    return boardState.copy(
        diceRoll = diceRoll.total,
        currentPlayer = nextPlayer,
        gamePhase = if (winner != null) GamePhase.GAME_OVER else GamePhase.ROLLING,
        winner = winner,
        players = boardState.players.map { player ->
            if (player.color == currentPlayer.color) {
                player.copy(diceRolls = newDiceRolls)
            } else {
                player
            }
        }
    )
}

fun getMovableTokens(player: Player, diceRoll: Int, rules: GameRules): List<Token> {
    return player.tokens.filter { token ->
        when {
            token.position == -1 -> {
                // Token in home base - can only move out with a 6 (if rule is enabled)
                !rules.requiresSixToExitBase || diceRoll == 6
            }
            token.position in 0..51 -> {
                // Token on main path - can always move if not going past finish
                token.position + diceRoll <= 57 // 52-57 is the home stretch
            }
            token.position in 100..105 -> {
                // Token in home path - can move if not going past finish
                (token.position - 100) + diceRoll <= 6
            }
            token.position == 200 -> {
                // Token already finished
                false
            }
            else -> false
        }
    }
}

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

fun isSafeZone(position: Int, color: PlayerColor, rules: GameRules): Boolean {
    // Main path safe zones (stars on the board)
    val mainPathSafeZones = listOf(1, 9, 14, 22, 27, 35, 40, 48)

    // Starting points are safe zones
    val startingPoints = mapOf(
        PlayerColor.GREEN to 1,
        PlayerColor.RED to 14,
        PlayerColor.YELLOW to 27,
        PlayerColor.BLUE to 40
    )

    return when {
        rules.startingPointIsSafeZoneForAll && startingPoints.values.contains(position) -> true
        rules.startingPointIsSafeZoneForColor && startingPoints[color] == position -> true
        mainPathSafeZones.contains(position) -> true
        position in 100..105 -> true // Home path is always safe
        position == 200 -> true // Finished position is safe
        else -> false
    }
}

fun checkForWinner(boardState: BoardState): PlayerColor? {
    return boardState.players.find { player ->
        player.tokens.all { it.position == 200 }
    }?.color
}

// AI logic for automatic token movement (simplified)
fun selectBestToken(player: Player, diceRoll: Int, rules: GameRules): Token? {
    val movableTokens = getMovableTokens(player, diceRoll, rules)
    
    return when {
        movableTokens.isEmpty() -> null
        // Prioritize moving tokens out of home base
        movableTokens.any { it.position == -1 } -> movableTokens.first { it.position == -1 }
        // Prioritize tokens close to finishing
        movableTokens.any { it.position in 100..105 } -> {
            movableTokens.filter { it.position in 100..105 }
                .maxByOrNull { it.position }
        }
        // Move the most advanced token on main path
        else -> movableTokens.maxByOrNull { it.position }
    }
}

fun performAutomaticMove(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val selectedToken = selectBestToken(currentPlayer, diceRoll.total, rules)
    
    return if (selectedToken != null) {
        moveToken(boardState, selectedToken, diceRoll.total, rules)
    } else {
        boardState
    }
}

// Additional utility functions for complete game functionality

fun isValidMove(token: Token, steps: Int, rules: GameRules): Boolean {
    return when {
        token.position == -1 -> {
            // Token in home base
            !rules.requiresSixToExitBase || steps == 6
        }
        token.position in 0..51 -> {
            // Token on main path
            token.position + steps <= 57
        }
        token.position in 100..105 -> {
            // Token in home path
            (token.position - 100) + steps <= 6
        }
        token.position == 200 -> {
            // Token already finished
            false
        }
        else -> false
    }
}

fun getTokensAtPosition(boardState: BoardState, position: Int): List<Token> {
    return boardState.players.flatMap { it.tokens }.filter { it.position == position }
}

fun canCaptureAt(position: Int, attackingColor: PlayerColor, rules: GameRules, boardState: BoardState): Boolean {
    val tokensAtPosition = getTokensAtPosition(boardState, position)
    return tokensAtPosition.any { 
        it.color != attackingColor && 
        !isSafeZone(position, it.color, rules) 
    }
}

fun getPlayerScore(player: Player): Int {
    return player.tokens.sumOf { token ->
        when {
            token.position == 200 -> 100 // Finished token
            token.position in 100..105 -> 50 + (token.position - 100) * 5 // Home path
            token.position in 0..51 -> token.position // Main path
            else -> 0 // In base
        }
    }
}

fun getGameProgress(boardState: BoardState): Map<PlayerColor, Float> {
    return boardState.players.associate { player ->
        val totalProgress = player.tokens.sumOf { token ->
            when {
                token.position == 200 -> 57 // Finished
                token.position in 100..105 -> 52 + (token.position - 100) // Home path
                token.position in 0..51 -> token.position // Main path
                else -> 0 // In base
            }
        }
        player.color to (totalProgress / (57f * 4)) // Normalize to 0-1
    }
}

