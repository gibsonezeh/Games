package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

@Composable
fun CandyCrushScreen(onBack: () -> Unit, viewModel: CandyCrushViewModel = viewModel()) {
    val board = remember { mutableStateListOf<List<String>>() }

    // Update board when ViewModel changes
    LaunchedEffect(viewModel.board) {
        board.clear()
        board.addAll(viewModel.board)
    }

    // Auto-refresh board on recomposition
    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.board }.collect {
            board.clear()
            board.addAll(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍬 Candy Crush", style = MaterialTheme.typography.headlineMedium)
        Text("Score: ${viewModel.score}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        for (row in board.indices) {
            Row {
                for (col in board[row].indices) {
                    CandyCell(
                        emoji = board[row][col],
                        onSwipe = { direction ->
                            val (newRow, newCol) = getSwipeTarget(row, col, direction)
                            if (newRow in board.indices && newCol in board[row].indices) {
                                viewModel.swapCandies(row, col, newRow, newCol)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBack) {
            Text("⬅ Back to Menu")
        }
    }
}

@Composable
fun CandyCell(
    emoji: String,
    onSwipe: (Direction) -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (dx, dy) = dragAmount
                    if (abs(dx) > abs(dy)) {
                        if (dx > 0) onSwipe(Direction.RIGHT) else onSwipe(Direction.LEFT)
                    } else {
                        if (dy > 0) onSwipe(Direction.DOWN) else onSwipe(Direction.UP)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 24.sp)
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }

fun getSwipeTarget(row: Int, col: Int, direction: Direction): Pair<Int, Int> {
    return when (direction) {
        Direction.UP -> row - 1 to col
        Direction.DOWN -> row + 1 to col
        Direction.LEFT -> row to col - 1
        Direction.RIGHT -> row to col + 1
    }
}
