package com.gibson.games;

import androidx.compose.ui.graphics.Color

// Data class representing a single piece on the board
data class PieceState(
    val id: Int, // 0-3 for each player
    var position: Int, // -1 for yard, 0-51 for main track, 100-105 for home path, 200 for finished
    val color: Color, // Player color, e.g., Color.Red
    val playerId: String // e.g., "red", "green"
)

// Data class representing a player
data class PlayerState(
    val id: String, // e.g., "red", "green", "yellow", "blue"
    val name: String,
    val color: Color,
    val pieces: List<PieceState>,
    val startCellIndex: Int, // 0-indexed on the main track (the actual cell piece lands on from yard)
    val homeEntryCellIndex: Int, // 0-indexed on the main track (cell before home path starts)
    var hasWon: Boolean = false
)

// Data class representing the state of the two dice
data class DiceRoll(
    val d1: Int = 0,
    val d2: Int = 0,
    val sum: Int = 0,
    var d1Used: Boolean = false,
    var d2Used: Boolean = false,
    var rolled: Boolean = false
)

// Data class representing the overall game state
data class GameState(
    val players: List<PlayerState>,
    var currentPlayerIndex: Int = 0,
    var diceRoll: DiceRoll = DiceRoll(),
    var message: String = "Roll dice to start!",
    var selectedDiceValue: Int? = null, // Stores the chosen dice value (d1, d2, or sum) for the current move
    var awaitingPieceChoice: Boolean = false, // True if a dice value has been selected and the game is waiting for a piece click
    var winner: PlayerState? = null,
    // Board layout information can be static or part of this if dynamic in the future
    // For now, assume a standard Ludo board configuration
)

// Example initial setup for colors (can be moved to a theme or constants file)
object PlayerColors {
    val RED = Color(0xFFFF4444)
    val GREEN = Color(0xFF99CC00)
    val YELLOW = Color(0xFFFFBB33)
    val BLUE = Color(0xFF33B5E5)
}

