package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class CandyCrushViewModel : ViewModel() {

    private val _board = MutableStateFlow(generateInitialBoard().map { it.toMutableList() }.toMutableList())
    val board: StateFlow<List<List<String>>> = _board

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val numRows = 8
    private val numCols = 8

    init {
        processMatchesRecursively()
    }

    private fun generateInitialBoard(): List<List<String>> {
        return List(numRows) { List(numCols) { randomCandy() } }
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
        processMatchesRecursively()
    }

    private fun processMatchesRecursively() {
        val board = _board.value.map { it.toMutableList() }.toMutableList()
        val matched = mutableSetOf<Pair<Int, Int>>()

        // Horizontal matches
        for (i in 0 until numRows) {
            for (j in 0 until numCols - 2) {
                val candy = board[i][j]
                if (candy != "" && candy == board[i][j + 1] && candy == board[i][j + 2]) {
                    matched.add(i to j)
                    matched.add(i to j + 1)
                    matched.add(i to j + 2)
                }
            }
        }

        // Vertical matches
        for (j in 0 until numCols) {
            for (i in 0 until numRows - 2) {
                val candy = board[i][j]
                if (candy != "" && candy == board[i + 1][j] && candy == board[i + 2][j]) {
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
            _board.value = board
            processMatchesRecursively() // Continue checking after refill
        } else {
            _board.value = board
        }
    }

    private fun refillBoard(board: MutableList<MutableList<String>>) {
        for (j in 0 until numCols) {
            val column = mutableListOf<String>()
            for (i in 0 until numRows) {
                if (board[i][j] != "") column.add(board[i][j])
            }
            val blanks = List(numRows - column.size) { randomCandy() }
            val newColumn = blanks + column
            for (i in 0 until numRows) {
                board[i][j] = newColumn[i]
            }
        }
    }
}
