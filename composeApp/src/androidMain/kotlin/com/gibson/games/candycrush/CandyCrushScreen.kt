package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CandyCrushScreen(onBack: () -> Unit) {
    val viewModel: CandyCrushViewModel = viewModel()
    val board by viewModel.board.collectAsState()
    val score by viewModel.score.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍬 Candy Crush Clone", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: $score", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, candy ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    val (dx, dy) = dragAmount
                                    val (toRow, toCol) = when {
                                        dx > 30 -> rowIndex to colIndex + 1
                                        dx < -30 -> rowIndex to colIndex - 1
                                        dy > 30 -> rowIndex + 1 to colIndex
                                        dy < -30 -> rowIndex - 1 to colIndex
                                        else -> return@detectDragGestures
                                    }
                                    viewModel.swapCandies(rowIndex, colIndex, toRow, toCol)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(candy, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Back to Menu")
        }
    }
}
