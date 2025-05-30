package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CandyCrushViewModel : ViewModel() {

    private val _board = MutableStateFlow(generateInitialBoard())
    val board: StateFlow<List<List<String>>> = _board

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private fun generateInitialBoard(): List<List<String>> {
        return List(8) { List(8) { randomCandy() } }.map { it.toMutableList() }
    }

    private fun randomCandy(): String {
        val candies = listOf("🍒", "🍋", "🍇", "🍉", "🍍", "🍓")
        return candies.random()
    }

    fun swapCandies(row1: Int, col1: Int, row2: Int, col2: Int) {
        val newBoard = _board.value.map { it.toMutableList() }.toMutableList()

        // Swap the selected candies
        val temp = newBoard[row1][col1]
        newBoard[row1][col1] = newBoard[row2][col2]
        newBoard[row2][col2] = temp

        _board.value = newBoard

        // Immediately check for and process matches
        processMatches()
    }

    private fun processMatches() {
        var board = _board.value.map { it.toMutableList() }.toMutableList()
        var totalMatched = 0

        while (true) {
            val matched = mutableSetOf<Pair<Int, Int>>()

            // Check horizontal matches (3 or more)
            for (i in board.indices) {
                var count = 1
                for (j in 1 until board[i].size) {
                    if (board[i][j] == board[i][j - 1] && board[i][j] != "") {
                        count++
                    } else {
                        if (count >= 3) {
                            for (k in j - count until j) {
                                matched.add(i to k)
                            }
                        }
                        count = 1
                    }
                }
                if (count >= 3) {
                    for (k in board[i].size - count until board[i].size) {
                        matched.add(i to k)
                    }
                }
            }

            // Check vertical matches (3 or more)
            for (j in board[0].indices) {
                var count = 1
                for (i in 1 until board.size) {
                    if (board[i][j] == board[i - 1][j] && board[i][j] != "") {
                        count++
                    } else {
                        if (count >= 3) {
                            for (k in i - count until i) {
                                matched.add(k to j)
                            }
                        }
                        count = 1
                    }
                }
                if (count >= 3) {
                    for (k in board.size - count until board.size) {
                        matched.add(k to j)
                    }
                }
            }

            if (matched.isEmpty()) break

            totalMatched += matched.size

            // Clear matched candies
            matched.forEach { (i, j) -> board[i][j] = "" }

            // Drop candies and refill
            refillBoard(board)
        }

        if (totalMatched > 0) {
            _score.value += totalMatched * 10
        }

        _board.value = board
    }

    private fun refillBoard(board: MutableList<MutableList<String>>) {
        for (j in board[0].indices) {
            val column = mutableListOf<String>()
            for (i in board.indices) {
                if (board[i][j] != "") column.add(board[i][j])
            }
            val blanks = List(board.size - column.size) { randomCandy() }
            val newColumn = blanks + column
            for (i in board.indices) {
                board[i][j] = newColumn[i]
            }
        }
    }
}
