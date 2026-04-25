package com.gibson.games.tetris.engine

const val NEXT_MATRIX_WIDTH = 4
const val NEXT_MATRIX_HEIGHT = 2

const val SCORE_EVERY_PIECE = 12

fun calculateScore(lines: Int): Int {
    return when (lines) {
        1 -> 100
        2 -> 300
        3 -> 700
        4 -> 1500
        else -> 0
    }
}
