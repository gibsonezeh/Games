package com.gibson.games.tetris.engine

enum class Direction(
    val dx: Int,
    val dy: Int
) {
    Left(-1, 0),
    Right(1, 0),
    Down(0, 1),
    Up(0, -1)
}
