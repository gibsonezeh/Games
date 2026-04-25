package com.gibson.games.tetris.engine

import kotlin.math.min

data class TetrisState(
    val bricks: List<Brick> = emptyList(),
    val currentPiece: Tetromino = Tetromino.Empty,
    val nextPieces: List<Tetromino> = emptyList(),
    val matrixWidth: Int = MATRIX_WIDTH,
    val matrixHeight: Int = MATRIX_HEIGHT,
    val gameStatus: TetrisGameStatus = TetrisGameStatus.Onboard,
    val score: Int = 0,
    val lines: Int = 0,
    val isMute: Boolean = false
) {
    val level: Int
        get() = min(10, 1 + lines / 20)

    val nextPiece: Tetromino
        get() = nextPieces.firstOrNull() ?: Tetromino.Empty

    val isPaused: Boolean
        get() = gameStatus == TetrisGameStatus.Paused

    val isRunning: Boolean
        get() = gameStatus == TetrisGameStatus.Running

    val isGameOver: Boolean
        get() = gameStatus == TetrisGameStatus.GameOver
}

enum class TetrisGameStatus {
    Onboard,
    Running,
    LineClearing,
    Paused,
    ScreenClearing,
    GameOver
}

sealed interface TetrisAction {
    data class Move(val direction: Direction) : TetrisAction
    data object Reset : TetrisAction
    data object Pause : TetrisAction
    data object Resume : TetrisAction
    data object Rotate : TetrisAction
    data object Drop : TetrisAction
    data object GameTick : TetrisAction
    data object Mute : TetrisAction
}

const val MATRIX_WIDTH = 12
const val MATRIX_HEIGHT = 24
