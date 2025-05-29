package com.gibson.games.dreamtreats

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import kotlin.random.Random

// Represents a single candy tile
data class Candy(val type: String)

class GameLogic(private val rows: Int, private val columns: Int) {

    private val candyEmojis = listOf("❤️", "🧡", "💛", "💚", "💙", "💜") // Use emojis for candies
    val board = mutableStateListOf<MutableList<Candy>>()

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        for (i in 0 until rows) {
            val row = mutableStateListOf<Candy>()
            for (j in 0 until columns) {
                row.add(generateRandomCandy())
            }
            board.add(row)
        }
        // Ensure no initial matches
        while (findMatches().isNotEmpty()) {
            removeMatchesAndRefill()
        }
    }

    private fun generateRandomCandy(): Candy {
        return Candy(candyEmojis.random())
    }

    // Swaps two candies on the board
    fun swapCandies(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (!isValidCoordinate(row1, col1) || !isValidCoordinate(row2, col2)) return false
        if (!areAdjacent(row1, col1, row2, col2)) return false

        val temp = board[row1][col1]
        board[row1][col1] = board[row2][col2]
        board[row2][col2] = temp

        val matches = findMatches()
        if (matches.isEmpty()) {
            // If no match, swap back
            val tempBack = board[row1][col1]
            board[row1][col1] = board[row2][col2]
            board[row2][col2] = tempBack
            return false
        }
        return true
    }

    private fun isValidCoordinate(row: Int, col: Int): Boolean {
        return row >= 0 && row < rows && col >= 0 && col < columns
    }

    private fun areAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        return (kotlin.math.abs(r1 - r2) == 1 && c1 == c2) || (kotlin.math.abs(c1 - c2) == 1 && r1 == r2)
    }

    // Finds all matches on the board
    fun findMatches(): Set<Pair<Int, Int>> {
        val matches = mutableSetOf<Pair<Int, Int>>()

        // Check horizontal matches
        for (r in 0 until rows) {
            for (c in 0 until columns - 2) {
                val candy1 = board[r][c].type
                if (candy1 == board[r][c + 1].type && candy1 == board[r][c + 2].type) {
                    matches.add(Pair(r, c))
                    matches.add(Pair(r, c + 1))
                    matches.add(Pair(r, c + 2))
                }
            }
        }

        // Check vertical matches
        for (c in 0 until columns) {
            for (r in 0 until rows - 2) {
                val candy1 = board[r][c].type
                if (candy1 == board[r + 1][c].type && candy1 == board[r + 2][c].type) {
                    matches.add(Pair(r, c))
                    matches.add(Pair(r + 1, c))
                    matches.add(Pair(r + 2, c))
                }
            }
        }
        return matches
    }

    // Removes matched candies, shifts remaining candies down, and refills new candies
    fun removeMatchesAndRefill() {
        val matches = findMatches()
        if (matches.isEmpty()) return

        // Mark matched candies as null (or a placeholder)
        val tempBoard = board.map { it.toMutableList() }.toMutableList()
        matches.forEach { (r, c) ->
            tempBoard[r][c] = Candy("") // Use an empty string or special placeholder for "removed"
        }

        // Shift candies down
        for (c in 0 until columns) {
            var writeRow = rows - 1
            for (r in rows - 1 downTo 0) {
                if (tempBoard[r][c].type != "") { // If not a removed candy
                    board[writeRow][c] = tempBoard[r][c]
                    writeRow--
                }
            }
            // Fill top with new candies
            for (r in 0..writeRow) {
                board[r][c] = generateRandomCandy()
            }
        }
    }
}
