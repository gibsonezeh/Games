package com.gibson.games.dreamtreats

import kotlin.random.Random

enum class TreatType {
    CUPCAKE, COOKIE, DONUT, ICECREAM, CANDY,
    CAKE, CHOCOLATE, SHAVED_ICE, LOLLIPOP, PIE
}

data class TreatTile(val x: Int, val y: Int, var type: TreatType)

object GameLogic {
    const val BOARD_SIZE = 6

    fun createBoard(): List<List<TreatTile>> {
        return List(BOARD_SIZE) { y ->
            List(BOARD_SIZE) { x ->
                TreatTile(x, y, TreatType.values().random())
            }
        }
    }

    // 8-direction adjacency (includes diagonals)
    private val directions = listOf(
        Pair(0, -1),  // Up
        Pair(0, 1),   // Down
        Pair(-1, 0),  // Left
        Pair(1, 0),   // Right
        Pair(-1, -1), // Top-left
        Pair(1, -1),  // Top-right
        Pair(-1, 1),  // Bottom-left
        Pair(1, 1)    // Bottom-right
    )

    fun detectMatches(board: List<List<TreatTile>>): Set<Pair<Int, Int>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val matched = mutableSetOf<Pair<Int, Int>>()

        for (row in board) {
            for (tile in row) {
                if (visited.contains(tile.x to tile.y)) continue

                val group = mutableSetOf<Pair<Int, Int>>()
                dfs(tile, board, tile.type, group, visited)

                if (group.size >= 3) {
                    matched.addAll(group)
                }
            }
        }

        return matched
    }

    private fun dfs(
        tile: TreatTile,
        board: List<List<TreatTile>>,
        targetType: TreatType,
        group: MutableSet<Pair<Int, Int>>,
        visited: MutableSet<Pair<Int, Int>>
    ) {
        val key = tile.x to tile.y
        if (visited.contains(key)) return
        visited.add(key)

        if (tile.type != targetType) return

        group.add(key)

        for ((dx, dy) in directions) {
            val nx = tile.x + dx
            val ny = tile.y + dy

            if (nx in 0 until BOARD_SIZE && ny in 0 until BOARD_SIZE) {
                val neighbor = board[ny][nx]
                dfs(neighbor, board, targetType, group, visited)
            }
        }
    }

    fun clearMatchesAndRefill(board: List<List<TreatTile>>, matches: Set<Pair<Int, Int>>): List<List<TreatTile>> {
        val newBoard = board.map { it.toMutableList() }

        for ((x, y) in matches) {
            newBoard[y][x] = TreatTile(x, y, TreatType.values().random())
        }

        return newBoard
    }
}
