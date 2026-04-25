package com.gibson.games.tetris.engine

import kotlin.math.absoluteValue
import kotlin.random.Random

data class Cell(
    val x: Int,
    val y: Int
)

data class Tetromino(
    val shape: List<Cell> = emptyList(),
    val offsetX: Int = 0,
    val offsetY: Int = 0
) {
    val cells: List<Cell>
        get() = shape.map { cell ->
            Cell(
                x = cell.x + offsetX,
                y = cell.y + offsetY
            )
        }

    fun moveBy(dx: Int, dy: Int): Tetromino {
        return copy(
            offsetX = offsetX + dx,
            offsetY = offsetY + dy
        )
    }

    fun rotate(): Tetromino {
        val rotatedShape = shape.map { cell ->
            Cell(
                x = cell.y,
                y = -cell.x
            )
        }

        return copy(shape = rotatedShape)
    }

    fun adjustOffset(
        matrixWidth: Int,
        matrixHeight: Int,
        adjustY: Boolean = true
    ): Tetromino {
        val minX = cells.minOfOrNull { it.x } ?: 0
        val maxX = cells.maxOfOrNull { it.x } ?: 0
        val minY = cells.minOfOrNull { it.y } ?: 0
        val maxY = cells.maxOfOrNull { it.y } ?: 0

        val xOffset =
            if (minX < 0) {
                minX.absoluteValue
            } else if (maxX > matrixWidth - 1) {
                matrixWidth - maxX - 1
            } else {
                0
            }

        val yOffset =
            if (!adjustY) {
                0
            } else if (minY < 0) {
                minY.absoluteValue
            } else if (maxY > matrixHeight - 1) {
                matrixHeight - maxY - 1
            } else {
                0
            }

        return moveBy(xOffset, yOffset)
    }

    fun isValidInMatrix(
        bricks: List<Brick>,
        matrixWidth: Int,
        matrixHeight: Int
    ): Boolean {
        return cells.none { cell ->
            cell.x < 0 ||
                cell.x > matrixWidth - 1 ||
                cell.y > matrixHeight - 1 ||
                bricks.any { brick ->
                    brick.x == cell.x && brick.y == cell.y
                }
        }
    }

    companion object {
        val Empty = Tetromino()
    }
}

val TetrominoShapes = listOf(
    // Z
    listOf(
        Cell(1, -1),
        Cell(1, 0),
        Cell(0, 0),
        Cell(0, 1)
    ),

    // S
    listOf(
        Cell(0, -1),
        Cell(0, 0),
        Cell(1, 0),
        Cell(1, 1)
    ),

    // I
    listOf(
        Cell(0, -1),
        Cell(0, 0),
        Cell(0, 1),
        Cell(0, 2)
    ),

    // T
    listOf(
        Cell(0, 1),
        Cell(0, 0),
        Cell(0, -1),
        Cell(1, 0)
    ),

    // O
    listOf(
        Cell(1, 0),
        Cell(0, 0),
        Cell(1, -1),
        Cell(0, -1)
    ),

    // L
    listOf(
        Cell(0, -1),
        Cell(1, -1),
        Cell(1, 0),
        Cell(1, 1)
    ),

    // J
    listOf(
        Cell(1, -1),
        Cell(0, -1),
        Cell(0, 0),
        Cell(0, 1)
    )
)

fun generateTetrominoReserve(
    matrixWidth: Int = MATRIX_WIDTH
): List<Tetromino> {
    return TetrominoShapes.map { shape ->
        Tetromino(
            shape = shape,
            offsetX = Random.nextInt(matrixWidth - 1),
            offsetY = -1
        ).adjustOffset(
            matrixWidth = matrixWidth,
            matrixHeight = MATRIX_HEIGHT,
            adjustY = false
        )
    }.shuffled()
}
