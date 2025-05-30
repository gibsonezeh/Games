package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CandyCrushScreen(onBack: () -> Unit) {
    val gridSize = 8
    val emojis = listOf("🍒", "🍋", "🍇", "🍉", "🍍")
    var grid by remember { mutableStateOf(generateInitialGrid(gridSize, emojis)) }

    LaunchedEffect(grid) {
        while (removeMatches(grid).isNotEmpty()) {
            grid = dropCandies(grid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍓🍒 Fruit Crush", fontSize = 24.sp, modifier = Modifier.padding(8.dp))

        for (row in 0 until gridSize) {
            Row {
                for (col in 0 until gridSize) {
                    val emoji = grid[row][col]
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(2.dp)
                            .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val (dx, dy) = dragAmount
                                    val targetRow = row + when {
                                        abs(dy) > abs(dx) && dy < 0 -> -1
                                        abs(dy) > abs(dx) && dy > 0 -> 1
                                        else -> 0
                                    }
                                    val targetCol = col + when {
                                        abs(dx) > abs(dy) && dx < 0 -> -1
                                        abs(dx) > abs(dy) && dx > 0 -> 1
                                        else -> 0
                                    }

                                    if (targetRow in 0 until gridSize && targetCol in 0 until gridSize) {
                                        val newGrid = grid.map { it.toMutableList() }.toMutableList()
                                        val temp = newGrid[row][col]
                                        newGrid[row][col] = newGrid[targetRow][targetCol]
                                        newGrid[targetRow][targetCol] = temp

                                        if (removeMatches(newGrid).isNotEmpty()) {
                                            grid = newGrid
                                            while (removeMatches(grid).isNotEmpty()) {
                                                grid = dropCandies(grid)
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("🔙 Back to Menu")
        }
    }
}

fun generateInitialGrid(size: Int, emojis: List<String>): List<MutableList<String>> {
    val grid = MutableList(size) {
        MutableList(size) { emojis.random() }
    }
    while (removeMatches(grid).isNotEmpty()) {
        val matches = removeMatches(grid)
        for ((r, c) in matches) {
            grid[r][c] = emojis.random()
        }
    }
    return grid
}

fun removeMatches(grid: List<MutableList<String>>): List<Pair<Int, Int>> {
    val matches = mutableSetOf<Pair<Int, Int>>()

    // Check rows
    for (r in grid.indices) {
        for (c in 0 until grid[r].size - 2) {
            val candy = grid[r][c]
            if (candy == grid[r][c + 1] && candy == grid[r][c + 2]) {
                matches.add(Pair(r, c))
                matches.add(Pair(r, c + 1))
                matches.add(Pair(r, c + 2))
            }
        }
    }

    // Check columns
    for (c in grid[0].indices) {
        for (r in 0 until grid.size - 2) {
            val candy = grid[r][c]
            if (candy == grid[r + 1][c] && candy == grid[r + 2][c]) {
                matches.add(Pair(r, c))
                matches.add(Pair(r + 1, c))
                matches.add(Pair(r + 2, c))
            }
        }
    }

    for ((r, c) in matches) {
        grid[r][c] = ""
    }

    return matches.toList()
}

fun dropCandies(grid: List<MutableList<String>>): List<MutableList<String>> {
    val size = grid.size
    val emojis = listOf("🍒", "🍋", "🍇", "🍉", "🍍")
    for (c in 0 until size) {
        val column = mutableListOf<String>()
        for (r in size - 1 downTo 0) {
            if (grid[r][c].isNotEmpty()) {
                column.add(grid[r][c])
            }
        }
        while (column.size < size) {
            column.add(emojis.random())
        }
        for (r in size - 1 downTo 0) {
            grid[r][c] = column[size - 1 - r]
        }
    }
    return grid
}
