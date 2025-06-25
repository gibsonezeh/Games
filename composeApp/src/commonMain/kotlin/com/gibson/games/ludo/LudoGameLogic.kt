package com.gibson.games.ludo

// This file will contain the core game logic for Ludo.



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
    var hasRolled: Boolean = false
)

data class BoardState(
    val players: List<Player>,
    val currentPlayer: PlayerColor,
    val diceRoll: Int? = null
)




fun initializeGameState(): BoardState {
    val players = PlayerColor.values().map { color ->
        val tokens = (1..4).map { id -> Token(id, color, -1) } // -1 indicates token is in home base
        Player(color, tokens)
    }
    return BoardState(players, PlayerColor.GREEN) // Green starts first
}




import kotlin.random.Random

fun rollDice(): Int {
    return Random.nextInt(1, 7) // Generates a random number between 1 and 6
}




fun moveToken(boardState: BoardState, token: Token, steps: Int): BoardState {
    // This is a simplified move logic. More complex rules (e.g., cutting, safe zones) will be added later.
    val newPosition = token.position + steps
    token.position = newPosition
    return boardState.copy(players = boardState.players.map { player ->
        if (player.color == token.color) {
            player.copy(tokens = player.tokens.map { if (it.id == token.id) token else it })
        } else {
            player
        }
    })
}




fun handleTurn(boardState: BoardState): BoardState {
    val currentPlayer = boardState.players.first { it.color == boardState.currentPlayer }
    val diceRoll = rollDice()

    // For now, just update the dice roll in the board state
    // More complex logic for moving tokens will be added here later
    return boardState.copy(diceRoll = diceRoll)
}


