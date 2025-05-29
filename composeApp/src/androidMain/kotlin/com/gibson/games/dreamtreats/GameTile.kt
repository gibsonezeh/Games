package com.gibson.games.dreamtreats

// Consolidated TreatType enum
enum class TreatType {
    CUPCAKE, COOKIE, DONUT, ICECREAM, CANDY,
    CAKE, CHOCOLATE, SHAVED_ICE, LOLLIPOP, PIE
}

// Consolidated GameTile data class
data class GameTile(
    val x: Int,         // Column index
    val y: Int,         // Row index
    var type: TreatType,
    val id: Int = (y * GameLogic.BOARD_SIZE + x), // Unique ID based on position
    var isMatched: Boolean = false // Flag for matching state
)

