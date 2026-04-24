package com.gibson.games.carrom

import kotlin.math.sqrt

enum class DiscType {
    BLACK, WHITE, QUEEN, STRIKER
}

data class CarromDisc(
    val id: Int,
    val type: DiscType,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val radius: Float = 18f,
    val pocketed: Boolean = false
)

data class Pocket(
    val x: Float,
    val y: Float,
    val radius: Float = 32f
)

data class CarromState(
    val boardWidth: Float = 1000f,
    val boardHeight: Float = 1000f,
    val discs: List<CarromDisc>,
    val currentPlayer: Int = 0,
    val isMoving: Boolean = false,
    val pocketedThisTurn: List<CarromDisc> = emptyList()
)
