package com.gibson.games.candycrush

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class CandyCrushViewModel : ViewModel() {

    private val rows = 8
    private val cols = 8
    private val candyTypes = listOf("🍒", "🍇", "🍋", "🍊", "🍏", "🍬")

    var board: List<MutableList<String>> = List(rows) { MutableList(cols) { "" } }
        private set

    var score: Int = 0
        private set

    init {
        generateInitialBoard()
        processAutoCrushLoop()
    }

    private fun generateInitialBoard() {
        do {
            board = List(rows) {
                MutableList(cols) {
                    candyTypes.random()
                }
            }
        } while (detectMatches().isNotEmpty())
    }

    fun swapCandies(row1: Int, col1: Int, row2: Int, col2: Int) {
        if (!isValidSwap(row1, col1, row2, col2)) return

        val temp = board[row1][col1]
        board[row1][col1] = board[row2][col2]
        board[row2][col2] = temp

        val matches = detectMatches()
        if (matches.isNotEmpty()) {
            crushMatches(matches)
            refillBoard()
            processAutoCrushLoop()
        } else {
            // Swap back if no match
            board[row2][col2] = board[row1][col1]
            board[row1][col1] = temp
        }
    }

    private fun isValidSwap(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val dr = abs(r1 - r2)
        val dc = abs(c1 - c2)
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1)
    }

    private fun detectMatches(): Set<Pair<Int, Int>> {
        val matches = mutableSetOf<Pair<Int, Int>>()

        // Horizontal check
        for (r in 0 until rows) {
            var count = 1
            for (c in 1 until cols) {
                if (board[r][c] == board[r][c - 1] && board[r][c].isNotEmpty()) {
                    count++
                } else {
                    if (count >= 3) {
                        for (i in 0 until count) {
                            matches.add(r to c - 1 - i)
                        }
                    }
                    count = 1
                }
            }
            if (count >= 3) {
                for (i in 0 until count) {
                    matches.add(r to cols - 1 - i)
                }
            }
        }

        // Vertical check
        for (c in 0 until cols) {
            var count = 1
            for (r in 1 until rows) {
                if (board[r][c] == board[r - 1][c] && board[r][c].isNotEmpty()) {
                    count++
                } else {
                    if (count >= 3) {
                        for (i in 0 until count) {
                            matches.add(r - 1 - i to c)
                        }
                    }
                    count = 1
                }
            }
            if (count >= 3) {
                for (i in 0 until count) {
                    matches.add(rows - 1 - i to c)
                }
            }
        }

        return matches
    }

    private fun crushMatches(matches: Set<Pair<Int, Int>>) {
        for ((r, c) in matches) {
            board[r][c] = ""
            score += 10
        }
    }

    private fun refillBoard() {
        for (c in 0 until cols) {
            var emptyRow = rows - 1
            for (r in rows - 1 downTo 0) {
                if (board[r][c].isNotEmpty()) {
                    board[emptyRow][c] = board[r][c]
                    if (emptyRow != r) board[r][c] = ""
                    emptyRow--
                }
            }
            for (r in emptyRow downTo 0) {
                board[r][c] = candyTypes.random()
            }
        }
    }

    fun processAutoCrushLoop() {
        viewModelScope.launch {
            var hasMatch: Boolean
            do {
                delay(250)
                val matches = detectMatches()
                hasMatch = matches.isNotEmpty()
                if (hasMatch) {
                    crushMatches(matches)
                    refillBoard()
                }
            } while (hasMatch)
        }
    }
}
