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
    
    // Check available moves for each die individually and combined
    val availableMoves = getAvailableMovesForDiceRoll(currentPlayer, diceRoll, rules)
    
    var nextPlayer = boardState.currentPlayer
    val newDiceRolls = currentPlayer.diceRolls.toMutableList()
    newDiceRolls.add(diceRoll.die1)
    newDiceRolls.add(diceRoll.die2)

    // Determine next player based on rules
    if (rules.getsExtraTurnOnSix && (diceRoll.die1 == 6 || diceRoll.die2 == 6)) {
        // Player gets another turn if they rolled a 6 on either die
        nextPlayer = boardState.currentPlayer
        
        // Special case: double 6s give another turn after playing both dice
        if (diceRoll.die1 == 6 && diceRoll.die2 == 6) {
            nextPlayer = boardState.currentPlayer
        }
    } else if (rules.getsExtraTurnOnThreeSixesForfeit && 
               newDiceRolls.takeLast(6).count { it == 6 } >= 3) {
        // Forfeit turn for three 6s in recent rolls
        newDiceRolls.clear()
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    } else if (availableMoves.isEmpty()) {
        // No available moves, forfeit turn
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
        gamePhase = if (winner != null) GamePhase.GAME_OVER else GamePhase.MOVING,
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
                // Token in home base - can only move out with a 6 (individual die, not total)
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

// New function to get available moves for both dice
fun getAvailableMovesForDiceRoll(player: Player, diceRoll: DiceRoll, rules: GameRules): List<TokenMove> {
    val moves = mutableListOf<TokenMove>()
    
    // Check moves for die1
    val movableWithDie1 = getMovableTokens(player, diceRoll.die1, rules)
    movableWithDie1.forEach { token ->
        moves.add(TokenMove(token, diceRoll.die1, 1))
    }
    
    // Check moves for die2
    val movableWithDie2 = getMovableTokens(player, diceRoll.die2, rules)
    movableWithDie2.forEach { token ->
        moves.add(TokenMove(token, diceRoll.die2, 2))
    }
    
    // Special handling for double 6s - player can use either die for any valid move
    if (diceRoll.die1 == 6 && diceRoll.die2 == 6) {
        // Add flexible moves for double 6s
        player.tokens.forEach { token ->
            if (isValidMove(token, 6, rules)) {
                moves.add(TokenMove(token, 6, 0)) // 0 indicates flexible die choice
            }
        }
    }
    
    return moves
}

data class TokenMove(
    val token: Token,
    val steps: Int,
    val dieUsed: Int // 1 for die1, 2 for die2, 0 for flexible (double 6s)
)

// PRESERVING ORIGINAL STARTING POSITIONS FROM YOUR BOARD LAYOUT
fun getStartingPosition(color: PlayerColor): Int {
    return when (color) {
        PlayerColor.GREEN -> 1   // Green starts at position 1 (matches visual position 1,6)
        PlayerColor.RED -> 14    // Red starts at position 14 (matches visual position 8,1)
        PlayerColor.YELLOW -> 27 // Yellow starts at position 27 (matches visual position 6,13)
        PlayerColor.BLUE -> 40   // Blue starts at position 40 (matches visual position 13,8)
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
    // Main path safe zones (stars on the board) - PRESERVING ORIGINAL SAFE ZONES
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
        rules.startingPointIsSaf    return boardState.copy(
        diceRoll = diceRoll.total,
        currentPlayer = nextPlayer,
        gamePhase = if (winner != null) GamePhase.GAME_OVER else GamePhase.MOVING,
        winner = winner,
        players = boardState.players.map { player ->
            if (player.color == currentPlayer.color) {
                player.copy(diceRolls = newDiceRolls)
            } else {
                player
            }
        }
    )
} function to handle complex dice moves (e.g., using one 6 to get out and other number to move)
fun executeComplexMove(boardState: BoardState, rules: GameRules, diceRoll: DiceRoll, 
                      firstMove: TokenMove?, secondMove: TokenMove?): BoardState {
    var updatedState = boardState
    
    // Execute first move if provided
    if (firstMove != null) {
        updatedState = moveToken(updatedState, firstMove.token, firstMove.steps, rules)
    }
    
    // Execute second move if provided
    if (secondMove != null) {
        updatedState = moveToken(updatedState, secondMove.token, secondMove.steps, rules)
    }
    
    return updatedState
}

// Additional utility functions for complete game functionality

fun isValidMove(token: Token, steps: Int, rules: GameRules): Boolean {
    return when {
        token.position == -1 -> {
            // Token in home base - can only move out with a 6 (individual die)
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

