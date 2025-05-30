package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    val emojis = listOf("🍒", "🍋", "🍇", "🍎", "🍉", "🍬")
    var board by remember { mutableStateOf(generateMatchFreeBoard(gridSize, emojis)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("🍬 Candy Crush", fontSize = 24.sp)

        Board(board, onSwap = { from, to ->
            val swapped = board.toMutableList()
            val indexFrom = from.y * gridSize + from.x
            val indexTo = to.y * gridSize + to.x
            swapped[indexFrom] = board[indexTo]
            swapped[indexTo] = board[indexFrom]

            val matched = findMatches(swapped, gridSize)
            if (matched.isNotEmpty()) {
                board = collapseAndRefill(swapped, matched, gridSize, emojis)
            }
        })

        Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to Menu")
        }
    }
}

@Composable
fun Board(board: List<String>, onSwap: (from: Pos, to: Pos) -> Unit) {
    val gridSize = 8
    Column {
        for (y in 0 until gridSize) {
            Row {
                for (x in 0 until gridSize) {
                    val emoji = board[y * gridSize + x]
                    CandyCell(emoji, onSwipe = { dx, dy ->
                        val targetX = x + dx
                        val targetY = y + dy
                        if (targetX in 0 until gridSize && targetY in 0 until gridSize) {
                            onSwap(Pos(x, y), Pos(targetX, targetY))
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun CandyCell(emoji: String, onSwipe: (dx: Int, dy: Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .background(Color(0xFFFFE0B2), shape = RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val (dx, dy) = dragAmount
                    val direction = when {
                        abs(dx) > abs(dy) && dx > 0 -> 1 to 0   // right
                        abs(dx) > abs(dy) && dx < 0 -> -1 to 0  // left
                        dy > 0 -> 0 to 1                         // down
                        else -> 0 to -1                         // up
                    }
                    onSwipe(direction.first, direction.second)
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}

data class Pos(val x: Int, val y: Int)

fun findMatches(board: List<String>, gridSize: Int): Set<Int> {
    val matched = mutableSetOf<Int>()
    // Horizontal
    for (y in 0 until gridSize) {
        for (x in 0 until gridSize - 2) {
            val i = y * gridSize + x
            if (board[i] == board[i + 1] && board[i] == board[i + 2]) {
                matched.add(i)
                matched.add(i + 1)
                matched.add(i + 2)
            }
        }
    }
    // Vertical
    for (x in 0 until gridSize) {
        for (y in 0 until gridSize - 2) {
            val i = y * gridSize + x
            if (board[i] == board[i + gridSize] && board[i] == board[i + gridSize * 2]) {
                matched.add(i)
                matched.add(i + gridSize)
                matched.add(i + gridSize * 2)
            }
        }
    }
    return matched
}

fun collapseAndRefill(board: List<String>, matched: Set<Int>, gridSize: Int, emojis: List<String>): List<String> {
    val mutable = board.toMutableList()
    for (i in matched) mutable[i] = ""

    for (x in 0 until gridSize) {
        val col = (0 until gridSize).map { y -> mutable[y * gridSize + x] }
        val filtered = col.filter { it.isNotEmpty() }.toMutableList()
        while (filtered.size < gridSize) {
            filtered.add(0, emojis.random())
        }
        for (y in 0 until gridSize) {
            mutable[y * gridSize + x] = filtered[y]
        }
    }
    return mutable
}

fun generateMatchFreeBoard(gridSize: Int, emojis: List<String>): List<String> {
    while (true) {
        val board = List(gridSize * gridSize) { emojis.random() }
        if (findMatches(board, gridSize).isEmpty()) return board
    }
}
