package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class CandyCrushViewModel : ViewModel() {
    private val _board = MutableStateFlow(generateSafeBoard())
    val board: StateFlow<List<List<String>>> = _board

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val candies = listOf("🍒", "🍋", "🍇", "🍉", "🍍", "🍓")

    private fun randomCandy(): String = candies.random()

    private fun generateSafeBoard(): List<List<String>> {
        val board = MutableList(8) { MutableList(8) { "" } }

        for (i in 0..7) {
            for (j in 0..7) {
                var candy: String
                do {
                    candy = randomCandy()
                } while (
                    (j >= 2 && candy == board[i][j - 1] && candy == board[i][j - 2]) ||
                    (i >= 2 && candy == board[i - 1][j] && candy == board[i - 2][j])
                )
                board[i][j] = candy
            }
        }

        return board
    }

    fun swapCandies(row1: Int, col1: Int, row2: Int, col2: Int) {
        if (!isInBounds(row1, col1) || !isInBounds(row2, col2)) return

        val newBoard = _board.value.map { it.toMutableList() }.toMutableList()
        val temp = newBoard[row1][col1]
        newBoard[row1][col1] = newBoard[row2][col2]
        newBoard[row2][col2] = temp

        _board.value = newBoard
        processMatches()
    }

    private fun isInBounds(i: Int, j: Int) = i in 0..7 && j in 0..7

    private fun processMatches() {
        val board = _board.value.map { it.toMutableList() }
        val matched = mutableSetOf<Pair<Int, Int>>()

        // Horizontal
        for (i in board.indices) {
            for (j in 0..5) {
                val c = board[i][j]
                if (c != "" && c == board[i][j + 1] && c == board[i][j + 2]) {
                    matched += listOf(i to j, i to j + 1, i to j + 2)
                }
            }
        }

        // Vertical
        for (j in board[0].indices) {
            for (i in 0..5) {
                val c = board[i][j]
                if (c != "" && c == board[i + 1][j] && c == board[i + 2][j]) {
                    matched += listOf(i to j, i + 1 to j, i + 2 to j)
                }
            }
        }

        if (matched.isNotEmpty()) {
            matched.forEach { (i, j) -> board[i][j] = "" }
            _score.value += matched.size * 10
            refillBoard(board)
        }

        _board.value = board
    }

    private fun refillBoard(board: MutableList<MutableList<String>>) {
        for (j in board[0].indices) {
            val column = board.map { it[j] }.filter { it != "" }
            val missing = 8 - column.size
            val newColumn = List(missing) { randomCandy() } + column
            for (i in 0..7) board[i][j] = newColumn[i]
        }
        _board.value = board
        processMatches()
    }
}
