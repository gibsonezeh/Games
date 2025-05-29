package com.gibson.games.candycrush

import androidx.compose.foundation.Canvas 
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures 
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text 
import androidx.compose.runtime.* 
import androidx.compose.ui.Modifier 
import androidx.compose.ui.geometry.Offset 
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput 
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp 
import kotlin.math.absoluteValue 
import kotlin.random.Random

@Composable fun CandyCrushScreen(onBack: () -> Unit) {
    val gridSize = 8 
    val candyTypes = listOf("🍒", "🍋", "🍇", "🍎", "🍉", "🍬") 
    val cellSize = 48.dp val grid = remember { mutableStateListOf<MutableList<String>>() }

LaunchedEffect(Unit) {
    // Initialize grid with random candies
    grid.clear()
    repeat(gridSize) {
        grid.add(MutableList(gridSize) { candyTypes.random() })
    }
    removeMatches(grid, candyTypes)
}

Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
) {
    Text("🍬 Candy Crush", fontSize = 24.sp)

    Spacer(Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .size((cellSize + 2.dp) * gridSize)
            .background(Color.LightGray)
    ) {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val candy = grid[row][col]
                Box(
                    modifier = Modifier
                        .offset(
                            x = col * (cellSize + 2.dp),
                            y = row * (cellSize + 2.dp)
                        )
                        .size(cellSize)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val (dx, dy) = dragAmount
                                if (dx.absoluteValue > dy.absoluteValue) {
                                    val newCol = col + if (dx > 0) 1 else -1
                                    if (newCol in 0 until gridSize) {
                                        swapAndCheck(grid, row, col, row, newCol, candyTypes)
                                    }
                                } else {
                                    val newRow = row + if (dy > 0) 1 else -1
                                    if (newRow in 0 until gridSize) {
                                        swapAndCheck(grid, row, col, newRow, col, candyTypes)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = candy, fontSize = 24.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Text(
        "⬅️ Back",
        color = Color.Blue,
        modifier = Modifier
            .clickable { onBack() }
            .padding(8.dp)
    )
}

}

fun swapAndCheck( grid: MutableList<MutableList<String>>, r1: Int, c1: Int, r2: Int, c2: Int, candyTypes: List<String> ) { 
    val temp = grid[r1][c1] grid[r1][c1] = grid[r2][c2] grid[r2][c2] = temp

if (!removeMatches(grid, candyTypes)) {
    // No match → swap back
    grid[r2][c2] = grid[r1][c1]
    grid[r1][c1] = temp
}

}

fun removeMatches( grid: MutableList<MutableList<String>>, candyTypes: List<String> ): Boolean {
    val size = grid.size 
    var matchFound = false 
    val toClear = mutableSetOf<Pair<Int, Int>>()

// Horizontal
for (row in 0 until size) {
    for (col in 0 until size - 2) {
        val c = grid[row][col]
        if (c != "" && c == grid[row][col + 1] && c == grid[row][col + 2]) {
            toClear.add(Pair(row, col))
            toClear.add(Pair(row, col + 1))
            toClear.add(Pair(row, col + 2))
        }
    }
}

// Vertical
for (col in 0 until size) {
    for (row in 0 until size - 2) {
        val c = grid[row][col]
        if (c != "" && c == grid[row + 1][col] && c == grid[row + 2][col]) {
            toClear.add(Pair(row, col))
            toClear.add(Pair(row + 1, col))
            toClear.add(Pair(row + 2, col))
        }
    }
}

if (toClear.isNotEmpty()) {
    matchFound = true
    for ((r, c) in toClear) {
        grid[r][c] = ""
    }
    dropCandies(grid, candyTypes)
    removeMatches(grid, candyTypes) // Recursive until all matches are gone
}

return matchFound

}

fun dropCandies( grid: MutableList<MutableList<String>>, candyTypes: List<String> ) { 
    val size = grid.size for (col in 0 until size) { 
        val stack = mutableListOf<String>() for (row in size - 1 downTo 0) { 
            val candy = grid[row][col] 
            if (candy != "") stack.add(candy)
        } for (row in size - 1 downTo 0) { 
            grid[row][col] = if (stack.isNotEmpty()) stack.removeAt(0)
            else candyTypes.random() 
        } 
    }
}

