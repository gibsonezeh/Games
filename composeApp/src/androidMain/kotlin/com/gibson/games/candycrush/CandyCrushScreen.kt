// CandyCrushScreen.kt
package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CandyCrushScreen(viewModel: CandyCrushViewModel, onBack: () -> Unit) {
    val board by viewModel.board.collectAsState()
    val score by viewModel.score.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍬 Candy Crush", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Score: $score", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        for (i in board.indices) {
            Row {
                for (j in board[i].indices) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    val (dx, dy) = dragAmount
                                    val direction = when {
                                        dx > 50 -> Pair(0, 1)
                                        dx < -50 -> Pair(0, -1)
                                        dy > 50 -> Pair(1, 0)
                                        dy < -50 -> Pair(-1, 0)
                                        else -> null
                                    }
                                    direction?.let { (di, dj) ->
                                        val newRow = i + di
                                        val newCol = j + dj
                                        if (newRow in board.indices && newCol in board[i].indices) {
                                            viewModel.swapCandies(i, j, newRow, newCol)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = board[i][j], fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back to Menu")
        }
    }
}
