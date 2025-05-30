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
        val temp = newBoard[row1][col1]
        newBoard[row1][col1] = newBoard[row2][col2]
        newBoard[row2][col2] = temp
        _board.value = newBoard
        processMatches()
    }

    private fun processMatches() {
        val board = _board.value.map { it.toMutableList() }.toMutableList()
        val matched = mutableSetOf<Pair<Int, Int>>()

        for (i in board.indices) {
            for (j in 0..board[i].size - 3) {
                val item = board[i][j]
                if (item != "" && item == board[i][j + 1] && item == board[i][j + 2]) {
                    matched.add(i to j)
                    matched.add(i to j + 1)
                    matched.add(i to j + 2)
                }
            }
        }

        for (j in board[0].indices) {
            for (i in 0..board.size - 3) {
                val item = board[i][j]
                if (item != "" && item == board[i + 1][j] && item == board[i + 2][j]) {
                    matched.add(i to j)
                    matched.add(i + 1 to j)
                    matched.add(i + 2 to j)
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
        _board.value = board
        processMatches()
    }
}
