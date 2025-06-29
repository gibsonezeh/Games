package com.gibson.games.ludo

// This file will contain the core game logic for Ludo.

import kotlin.random.Random




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
    val diceRoll: Int? = null
)




fun initializeGameState(rules: GameRules = GameRules()): BoardState {
    val players = PlayerColor.values().map { color ->
        val tokens = (1..4).map { id -> Token(id, color, -1) } // -1 indicates token is in home base
        Player(color, tokens)
    }
    return BoardState(players, PlayerColor.GREEN) // Green starts first
}




fun rollDice(): Int {
    val die1 = Random.nextInt(1, 7)
    val die2 = Random.nextInt(1, 7)
    return die1 + die2
}




fun moveToken(boardState: BoardState, token: Token, steps: Int, rules: GameRules): BoardState {
    val newPosition = token.position + steps
    var updatedBoardState = boardState.copy()

    // Check for capture
    val capturedToken = updatedBoardState.players.flatMap { it.tokens }
        .firstOrNull { it.position == newPosition && it.color != token.color && !isSafeZone(newPosition, it.color, rules) }

    if (capturedToken != null && rules.capturedTokenReturnsToBase) {
        capturedToken.position = -1 // Send captured token back to base
        updatedBoardState = updatedBoardState.copy(players = updatedBoardState.players.map { player ->
            if (player.color == capturedToken.color) {
                player.copy(tokens = player.tokens.map { if (it.id == capturedToken.id) capturedToken else it })
            } else {
                player
            }
        })

        if (rules.captureGivesExtraTurn) {
            // Player gets another turn, so current player remains the same
            updatedBoardState = updatedBoardState.copy(currentPlayer = token.color)
        }

        if (rules.captureSendsToHome) {
            // Move capturing token to home path (simplified for now, actual home path logic needed)
            token.position = 100 + token.id // Placeholder for home path
        } else {
            token.position = newPosition // Stay at captured position
        }
    } else {
        token.position = newPosition
    }

    return updatedBoardState.copy(players = updatedBoardState.players.map { player ->
        if (player.color == token.color) {
            player.copy(tokens = player.tokens.map { if (it.id == token.id) token else it })
        } else {
            player
        }
    })
}




fun handleTurn(boardState: BoardState, rules: GameRules): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val diceRoll = rollDice()
    val movableTokens = currentPlayer.tokens.filter { token ->
        val canMoveFromBase = (token.position == -1 && diceRoll == 6)
        val canMoveOnBoard = (token.position != -1 && (token.position + diceRoll <= 56)) // 56 is the end of the main path before home stretch

        if (rules.requiresSixToExitBase) {
            (canMoveFromBase || (token.position != -1 && canMoveOnBoard))
        } else {
            (canMoveFromBase || canMoveOnBoard)
        }
    }

    var nextPlayer = boardState.currentPlayer
    val newDiceRolls = currentPlayer.diceRolls.toMutableList()
    newDiceRolls.add(diceRoll)

    var currentDiceRoll = diceRoll

    if (rules.mustPlayRolledNumbers && movableTokens.isEmpty() && diceRoll != 6) {
        // If no movable tokens and not a 6, forfeit turn
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    } else if (rules.getsExtraTurnOnSix && diceRoll == 6) {
        // Player gets another turn, nextPlayer remains the same
    } else if (rules.getsExtraTurnOnThreeSixesForfeit && newDiceRolls.takeLast(3).all { it == 6 } && newDiceRolls.size >= 3) {
        // Forfeit turn
        newDiceRolls.clear() // Reset dice rolls
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    } else {
        nextPlayer = getNextPlayer(boardState.currentPlayer)
    }

    // For now, just update the dice roll in the board state and current player
    // More complex logic for moving tokens and applying rules will be added here later
    return boardState.copy(diceRoll = currentDiceRoll, currentPlayer = nextPlayer, players = boardState.players.map { player ->
        if (player.color == currentPlayer.color) {
            player.copy(diceRolls = newDiceRolls)
        } else {
            player
        }
    })
}




data class GameRules(
    val requiresSixToExitBase: Boolean = true,
    val getsExtraTurnOnSix: Boolean = true,
    val getsExtraTurnOnThreeSixesForfeit: Boolean = true,
    val mustPlayRolledNumbers: Boolean = true,
    val capturedTokenReturnsToBase: Boolean = true,
    val captureGivesExtraTurn: Boolean = true,
    val captureSendsToHome: Boolean = true,
    val startingPointIsSafeZoneForColor: Boolean = true,
    val startingPointIsSafeZoneForAll: Boolean = true
)




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

    // Starting points are safe zones for their own color
    val startingPoints = mapOf(
        PlayerColor.GREEN to 1,
        PlayerColor.RED to 14,
        PlayerColor.YELLOW to 27,
        PlayerColor.BLUE to 40
    )

    return if (rules.startingPointIsSafeZoneForAll && startingPoints.values.contains(position)) {
        true
    } else if (rules.startingPointIsSafeZoneForColor && startingPoints[color] == position) {
        true
    } else {
        mainPathSafeZones.contains(position)
    }
}


