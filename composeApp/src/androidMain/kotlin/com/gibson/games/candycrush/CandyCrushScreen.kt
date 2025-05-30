package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CandyCrushScreen(onBack: () -> Unit) {
    val gridSize = 8
    val candyTypes = listOf("🍒", "🍋", "🍇", "🍓", "🍊", "🍍")
    val maxMoves = 30

    var grid by remember { mutableStateOf(generateInitialGrid(gridSize, candyTypes)) }
    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(maxMoves) }
    var gameOver by remember { mutableStateOf(false) }

    fun resetGame() {
        grid = generateInitialGrid(gridSize, candyTypes)
        score = 0
        movesLeft = maxMoves
        gameOver = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍬 Candy Crush", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: $score", fontSize = 18.sp)
        Text("Moves Left: $movesLeft", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (gameOver) {
            Text("🎮 Game Over!", fontSize = 24.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { resetGame() }) {
                Text("Restart Game")
            }
        } else {
            CandyGrid(
                grid = grid,
                onSwap = { from, to ->
                    if (movesLeft <= 0) return@CandyGrid

                    val newGrid = grid.map { it.toMutableList() }.toMutableList()
                    val temp = newGrid[from.first][from.second]
                    newGrid[from.first][from.second] = newGrid[to.first][to.second]
                    newGrid[to.first][to.second] = temp

                    val matches = detectMatches(newGrid)
                    if (matches.isNotEmpty()) {
                        grid = resolveMatches(newGrid, matches, candyTypes)
                        score += matches.size * 10
                        movesLeft--
                        if (movesLeft == 0) gameOver = true
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("🔙 Back")
        }
    }
}

@Composable
fun CandyGrid(
    grid: List<List<String>>,
    onSwap: (Pair<Int, Int>, Pair<Int, Int>) -> Unit
) {
    val gridSize = grid.size

    Column {
        for (row in 0 until gridSize) {
            Row {
                for (col in 0 until gridSize) {
                    var startOffset by remember { mutableStateOf<Offset?>(null) }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(2.dp)
                            .background(Color(0xFFF8BBD0), RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> startOffset = offset },
                                    onDragEnd = { startOffset = null },
                                    onDragCancel = { startOffset = null },
                                    onDrag = { change, dragAmount ->
                                        startOffset?.let {
                                            val direction = getDirectionFromDrag(dragAmount)
                                            val newRow = row + direction.first
                                            val newCol = col + direction.second
                                            if (newRow in 0 until gridSize && newCol in 0 until gridSize) {
                                                onSwap(Pair(row, col), Pair(newRow, newCol))
                                            }
                                            startOffset = null
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = grid[row][col], fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

fun getDirectionFromDrag(drag: Offset): Pair<Int, Int> {
    return when {
        abs(drag.x) > abs(drag.y) ->
            if (drag.x > 0) Pair(0, 1) else Pair(0, -1)
        else ->
            if (drag.y > 0) Pair(1, 0) else Pair(-1, 0)
    }
}

fun generateInitialGrid(size: Int, types: List<String>): List<List<String>> {
    var grid: List<List<String>>
    do {
        grid = List(size) {
            List(size) { types.random() }
        }
    } while (detectMatches(grid).isNotEmpty())
    return grid
}

fun detectMatches(grid: List<List<String>>): Set<Pair<Int, Int>> {
    val matched = mutableSetOf<Pair<Int, Int>>()
    val size = grid.size

    // Check rows
    for (row in 0 until size) {
        var count = 1
        for (col in 1 until size) {
            if (grid[row][col] == grid[row][col - 1]) {
                count++
            } else {
                if (count >= 3) {
                    for (i in col - count until col) {
                        matched.add(Pair(row, i))
                    }
                }
                count = 1
            }
        }
        if (count >= 3) {
            for (i in size - count until size) {
                matched.add(Pair(row, i))
            }
        }
    }

    // Check columns
    for (col in 0 until size) {
        var count = 1
        for (row in 1 until size) {
            if (grid[row][col] == grid[row - 1][col]) {
                count++
            } else {
                if (count >= 3) {
                    for (i in row - count until row) {
                        matched.add(Pair(i, col))
                    }
                }
                count = 1
            }
        }
        if (count >= 3) {
            for (i in size - count until size) {
                matched.add(Pair(i, col))
            }
        }
    }

    return matched
}

fun resolveMatches(
    grid: List<MutableList<String>>,
    matches: Set<Pair<Int, Int>>,
    types: List<String>
): List<List<String>> {
    for ((row, col) in matches) {
        grid[row][col] = ""
    }

    // Refill grid (candies fall down)
    val size = grid.size
    for (col in 0 until size) {
        val column = mutableListOf<String>()
        for (row in size - 1 downTo 0) {
            if (grid[row][col].isNotEmpty()) {
                column.add(grid[row][col])
            }
        }
        while (column.size < size) {
            column.add(types.random())
        }
        for (row in 0 until size) {
            grid[size - 1 - row][col] = column[row]
        }
    }

    return grid
}
