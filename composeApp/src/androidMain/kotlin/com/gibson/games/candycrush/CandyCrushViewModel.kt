package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class CandyCrushViewModel : ViewModel() {

    companion object {
        const val BOARD_SIZE = 8
        val CANDIES = listOf("🍒", "🍋", "🍇", "🍉", "🍍", "🍓") // Define candies
    }

    private val _board = MutableStateFlow(generateInitialBoard())
    val board: StateFlow<List<List<String>>> = _board.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    // Generates a board with no initial matches
    private fun generateInitialBoard(): List<List<String>> {
        var newBoard: MutableList<MutableList<String>>
        do {
            newBoard = MutableList(BOARD_SIZE) { MutableList(BOARD_SIZE) { randomCandy() } }
        } while (hasMatches(newBoard))
        return newBoard
    }

    private fun randomCandy(): String {
        return CANDIES.random()
    }

    // Checks if a given board state has any matches
    private fun hasMatches(board: List<List<String>>): Boolean {
        // Check horizontal matches
        for (i in board.indices) {
            for (j in 0 until board[i].size - 2) {
                if (board[i][j].isNotEmpty() && board[i][j] == board[i][j + 1] && board[i][j] == board[i][j + 2]) {
                    return true
                }
            }
        }
        // Check vertical matches
        for (j in board[0].indices) {
            for (i in 0 until board.size - 2) {
                if (board[i][j].isNotEmpty() && board[i][j] == board[i + 1][j] && board[i][j] == board[i + 2][j]) {
                    return true
                }
            }
        }
        return false
    }

    // Attempts to swap candies if the swap is valid (adjacent and creates a match)
    fun trySwapCandies(row1: Int, col1: Int, row2: Int, col2: Int) {
        // Check bounds
        if (row1 !in 0 until BOARD_SIZE || col1 !in 0 until BOARD_SIZE ||
            row2 !in 0 until BOARD_SIZE || col2 !in 0 until BOARD_SIZE) {
            return // Invalid coordinates
        }

        // Check adjacency
        if (abs(row1 - row2) + abs(col1 - col2) != 1) {
            return // Not adjacent
        }

        // Create a temporary board to test the swap
        val tempBoard = _board.value.map { it.toMutableList() }.toMutableList()
        val temp = tempBoard[row1][col1]
        tempBoard[row1][col1] = tempBoard[row2][col2]
        tempBoard[row2][col2] = temp

        // Check if the swap results in a match
        if (hasMatches(tempBoard)) {
            // If valid, update the actual board and process matches
            _board.value = tempBoard
            processMatches()
        } else {
            // Optional: Add feedback for invalid swap (e.g., animate swap back)
            println("Invalid swap: No match formed.")
        }
    }

    // Processes matches, clears them, refills the board, and updates score
    private fun processMatches() {
        var currentBoard = _board.value.map { it.toMutableList() }.toMutableList()
        var totalScoreIncrease = 0
        var boardChanged = false

        while (true) {
            val matchedCoords = findMatches(currentBoard)
            if (matchedCoords.isEmpty()) break

            boardChanged = true
            totalScoreIncrease += matchedCoords.size * 10 // Score based on number of matched candies

            // Clear matched candies (set to empty string or a special marker)
            matchedCoords.forEach { (r, c) -> currentBoard[r][c] = "" }

            // Refill the board (gravity and new candies)
            currentBoard = refillBoard(currentBoard)
        }

        if (boardChanged) {
            _board.value = currentBoard // Update the state flow only after all cascades
            _score.update { it + totalScoreIncrease } // Update score atomically
        }
    }

    // Finds all coordinates of matching candies (3 or more)
    private fun findMatches(board: List<List<String>>): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()

        // Check horizontal matches
        for (r in board.indices) {
            for (c in 0 until board[r].size - 2) {
                val candy = board[r][c]
                if (candy.isNotEmpty() && candy == board[r][c + 1] && candy == board[r][c + 2]) {
                    // Found a match of 3, check for more
                    var matchLength = 3
                    while (c + matchLength < board[r].size && board[r][c + matchLength] == candy) {
                        matchLength++
                    }
                    for (k in 0 until matchLength) {
                        matched.add(r to (c + k))
                    }
                }
            }
        }

        // Check vertical matches
        for (c in board[0].indices) {
            for (r in 0 until board.size - 2) {
                val candy = board[r][c]
                if (candy.isNotEmpty() && candy == board[r + 1][c] && candy == board[r + 2][c]) {
                    // Found a match of 3, check for more
                    var matchLength = 3
                    while (r + matchLength < board.size && board[r + matchLength][c] == candy) {
                        matchLength++
                    }
                    for (k in 0 until matchLength) {
                        matched.add((r + k) to c)
                    }
                }
            }
        }
        return matched
    }

    // Applies gravity and refills empty spots from the top
    private fun refillBoard(board: MutableList<MutableList<String>>): MutableList<MutableList<String>> {
        for (c in 0 until BOARD_SIZE) {
            val column = mutableListOf<String>()
            // Collect non-empty candies from bottom up
            for (r in BOARD_SIZE - 1 downTo 0) {
                if (board[r][c].isNotEmpty()) {
                    column.add(board[r][c])
                }
            }
            // Fill remaining spots with new candies
            while (column.size < BOARD_SIZE) {
                column.add(randomCandy())
            }
            // Place the updated column back, ensuring correct order (new candies at top)
            for (r in 0 until BOARD_SIZE) {
                board[r][c] = column[BOARD_SIZE - 1 - r]
            }
        }
        // Important: After refilling, check again for new matches caused by falling candies
        // This check is handled by the loop in processMatches()
        return board
    }
}

