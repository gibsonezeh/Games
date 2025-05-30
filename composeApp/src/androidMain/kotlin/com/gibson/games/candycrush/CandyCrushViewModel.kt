// CandyCrushViewModel.kt
package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CandyCrushViewModel : ViewModel() {
    private val _board = MutableStateFlow(generateInitialBoard())
    val board: StateFlow<List<List<String>>> = _board

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private fun generateInitialBoard(): MutableList<MutableList<String>> {
        val board = MutableList(8) { MutableList(8) { randomCandy() } }
        while (hasMatches(board)) {
            processMatches(board)
        }
        return board
    }

    private fun randomCandy(): String {
        val candies = listOf("🍒", "🍋", "🍇", "🍉", "🍍", "🍓")
        return candies.random()
    }

    fun swapCandies(row1: Int, col1: Int, row2: Int, col2: Int) {
        val newBoard = _board.value.map { it.toMutableList() }.toMutableList()
        val temp = newBoard[row1][col1]
        newBoard[row1][col1] = newBoard[row2][col2]
        newBoard[row2][col2] = temp
        _board.value = newBoard
        processMatches(newBoard)
    }

    private fun hasMatches(board: List<List<String>>): Boolean {
        return detectMatches(board).isNotEmpty()
    }

    private fun detectMatches(board: List<List<String>>): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()

        for (i in board.indices) {
            for (j in 0 until board[i].size - 2) {
                val item = board[i][j]
                if (item != "" && item == board[i][j + 1] && item == board[i][j + 2]) {
                    matched.addAll(listOf(i to j, i to j + 1, i to j + 2))
                }
            }
        }

        for (j in board[0].indices) {
            for (i in 0 until board.size - 2) {
                val item = board[i][j]
                if (item != "" && item == board[i + 1][j] && item == board[i + 2][j]) {
                    matched.addAll(listOf(i to j, i + 1 to j, i + 2 to j))
                }
            }
        }

        return matched
    }

    private fun processMatches(board: MutableList<MutableList<String>>) {
        val matches = detectMatches(board)
        if (matches.isNotEmpty()) {
            matches.forEach { (i, j) -> board[i][j] = "" }
            _score.value += matches.size * 10
            refillBoard(board)
        }
        _board.value = board
    }

    private fun refillBoard(board: MutableList<MutableList<String>>) {
        for (j in board[0].indices) {
            val column = board.map { it[j] }.filter { it != "" }.toMutableList()
            val blanks = MutableList(board.size - column.size) { randomCandy() }
            val newColumn = blanks + column
            for (i in board.indices) {
                board[i][j] = newColumn[i]
            }
        }
        if (hasMatches(board)) {
            processMatches(board)
        }
    }
}
