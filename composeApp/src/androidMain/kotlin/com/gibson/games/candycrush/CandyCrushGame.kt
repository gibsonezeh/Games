package com.gibson.games.candycrush

import kotlin.random.Random

class CandyCrushGame(val size: Int = 8) {
    private val candyTypes = listOf("🍒", "🍇", "🍋", "🍊", "🍏", "🍉")
    var board = MutableList(size * size) { randomCandy() }

    fun get(x: Int, y: Int): String = board[y * size + x]

    private fun randomCandy(): String = candyTypes.random()

    fun swap(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val idx1 = y1 * size + x1
        val idx2 = y2 * size + x2

        if (!isAdjacent(x1, y1, x2, y2)) return false

        board[idx1] = board[idx2].also { board[idx2] = board[idx1] }

        return if (hasMatches()) {
            processMatches()
            true
        } else {
            // revert
            board[idx1] = board[idx2].also { board[idx2] = board[idx1] }
            false
        }
    }

    private fun isAdjacent(x1: Int, y1: Int, x2: Int, y2: Int) =
        (x1 == x2 && (y1 - y2).absoluteValue == 1) ||
        (y1 == y2 && (x1 - x2).absoluteValue == 1)

    fun hasMatches(): Boolean {
        return findMatches().isNotEmpty()
    }

    private fun findMatches(): Set<Int> {
        val matched = mutableSetOf<Int>()

        // Horizontal
        for (y in 0 until size) {
            var count = 1
            for (x in 1 until size) {
                val cur = get(x, y)
                val prev = get(x - 1, y)
                if (cur == prev) count++ else count = 1
                if (count >= 3) {
                    for (i in 0 until count) matched.add(y * size + x - i)
                }
            }
        }

        // Vertical
        for (x in 0 until size) {
            var count = 1
            for (y in 1 until size) {
                val cur = get(x, y)
                val prev = get(x, y - 1)
                if (cur == prev) count++ else count = 1
                if (count >= 3) {
                    for (i in 0 until count) matched.add((y - i) * size + x)
                }
            }
        }

        return matched
    }

    fun processMatches() {
        while (true) {
            val matches = findMatches()
            if (matches.isEmpty()) break

            // Remove matched
            for (index in matches) {
                board[index] = ""
            }

            // Drop candies
            for (x in 0 until size) {
                val column = mutableListOf<String>()
                for (y in size - 1 downTo 0) {
                    val idx = y * size + x
                    if (board[idx].isNotEmpty()) {
                        column.add(board[idx])
                    }
                }

                // Fill column from bottom
                for (y in size - 1 downTo 0) {
                    val idx = y * size + x
                    board[idx] = if (column.isNotEmpty()) column.removeAt(0) else randomCandy()
                }
            }
        }

        // Auto-refresh if no more matches
        if (!hasPossibleMoves()) reshuffle()
    }

    fun reshuffle() {
        do {
            board = MutableList(size * size) { randomCandy() }
        } while (!hasPossibleMoves())
    }

    fun hasPossibleMoves(): Boolean {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val current = get(x, y)
                val neighbors = listOf(
                    x + 1 to y, x - 1 to y, x to y + 1, x to y - 1
                ).filter { (nx, ny) -> nx in 0 until size && ny in 0 until size }

                for ((nx, ny) in neighbors) {
                    val idx1 = y * size + x
                    val idx2 = ny * size + nx
                    board[idx1] = board[idx2].also { board[idx2] = board[idx1] }
                    if (hasMatches()) {
                        board[idx1] = board[idx2].also { board[idx2] = board[idx1] }
                        return true
                    }
                    board[idx1] = board[idx2].also { board[idx2] = board[idx1] }
                }
            }
        }
        return false
    }
}
