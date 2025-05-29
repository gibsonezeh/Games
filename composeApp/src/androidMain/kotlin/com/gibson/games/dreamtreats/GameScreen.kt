package com.gibson.games.dreamtreats

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect


@Composable
fun GameScreen() {
    val rows = 8
    val columns = 8
    val gameLogic = remember { GameLogic(rows, columns) }

    // State to track selected candy for swapping
    var selectedCandyPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Game loop for automatic matching and refilling
    LaunchedEffect(key1 = gameLogic.board) {
        while (true) {
            val matches = gameLogic.findMatches()
            if (matches.isNotEmpty()) {
                delay(300) // Delay to show matches before clearing
                gameLogic.removeMatchesAndRefill()
            } else {
                delay(100) // Small delay if no matches, to prevent tight loop
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB0E0E6)) // Light blue background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "DreamTreats", fontSize = 32.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Game Board
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Make the board square
                .background(Color(0xFF87CEEB)) // Sky blue for board background
        ) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until columns) {
                        val candy = gameLogic.board[r][c]
                        val isSelected = selectedCandyPos == Pair(r, c)
                        GameTile(
                            candy = candy,
                            isSelected = isSelected,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Each tile is square
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val tileSizePx = size.width.toFloat() / columns
                                            val clickedCol = (offset.x / tileSizePx).toInt()
                                            val clickedRow = (offset.y / tileSizePx).toInt()
                                            selectedCandyPos = Pair(r, c) // Set the initially selected candy
                                        },
                                        onDragEnd = {
                                            selectedCandyPos = null // Clear selection after drag
                                        },
                                        onDrag = { change, dragAmount ->
                                            selectedCandyPos?.let { (startRow, startCol) ->
                                                val tileSizePx = size.width.toFloat() / columns
                                                val targetCol = ((change.position.x) / tileSizePx).toInt()
                                                val targetRow = ((change.position.y) / tileSizePx).toInt()

                                                // Determine the target tile based on drag direction
                                                val deltaX = dragAmount.x
                                                val deltaY = dragAmount.y

                                                var swapRow = startRow
                                                var swapCol = startCol

                                                if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY)) {
                                                    // Horizontal drag
                                                    if (deltaX > 0) { // Right
                                                        swapCol = startCol + 1
                                                    } else { // Left
                                                        swapCol = startCol - 1
                                                    }
                                                } else {
                                                    // Vertical drag
                                                    if (deltaY > 0) { // Down
                                                        swapRow = startRow + 1
                                                    } else { // Up
                                                        swapRow = startRow - 1
                                                    }
                                                }

                                                // Only attempt swap if the target is different and valid
                                                if ((swapRow != startRow || swapCol != startCol) &&
                                                    gameLogic.isValidCoordinate(swapRow, swapCol)
                                                ) {
                                                    if (gameLogic.swapCandies(startRow, startCol, swapRow, swapCol)) {
                                                        // Swap was successful, now let the LaunchedEffect handle matches
                                                    }
                                                    selectedCandyPos = null // Clear selection after attempted swap
                                                }
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}
