package com.gibson.games.tetris.engine

data class Brick(
    val x: Int,
    val y: Int
) {

    companion object {

        fun of(cells: List<Cell>): List<Brick> {
            return cells.map { cell ->
                Brick(cell.x, cell.y)
            }
        }

        fun of(tetromino: Tetromino): List<Brick> {
            return tetromino.cells.map { cell ->
                Brick(cell.x, cell.y)
            }
        }

        fun of(
            xRange: IntRange,
            yRange: IntRange
        ): List<Brick> {
            val list = mutableListOf<Brick>()

            for (x in xRange) {
                for (y in yRange) {
                    list.add(Brick(x, y))
                }
            }

            return list
        }
    }

    fun offsetBy(dx: Int, dy: Int): Brick {
        return copy(
            x = x + dx,
            y = y + dy
        )
    }
}
