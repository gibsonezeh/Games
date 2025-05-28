package com.gibson.games.dreamtreats

import kotlin.random.Random

object GameLogic {
    const val BOARD_SIZE = 8

    fun createBoard(): List<List<GameTile>> {
        return List(BOARD_SIZE) { row ->
            List(BOARD_SIZE) { col ->
                GameTile(
                    id = row * BOARD_SIZE + col,
                    type = TreatType.values().random()
                )
            }
        }
    }

    fun swapTiles(board: List<List<GameTile>>, pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): List<List<GameTile>> {
        val newBoard = board.map { it.toMutableList() }.toMutableList()
        val (r1, c1) = pos1
        val (r2, c2) = pos2
        val temp = newBoard[r1][c1]
        newBoard[r1][c1] = newBoard[r2][c2]
        newBoard[r2][c2] = temp
        return newBoard
    }

    // Matching detection and clearing logic would go here
}
