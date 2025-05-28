package com.gibson.games.dreamtreats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun DreamTreatsGameScreen() {
    var board by remember { mutableStateOf(GameLogic.createBoard()) }
    var score by remember { mutableStateOf(0) }

    fun tryMatch() {
        while (true) {
            val matches = GameLogic.detectMatches(board)
            if (matches.isEmpty()) break
            board = GameLogic.clearMatchesAndRefill(board, matches)
            score += matches.size * 10
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🍬 Dream Treats",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        board.forEachIndexed { y, row ->
            Row {
                row.forEachIndexed { x, tile ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(2.dp)
                            .border(1.dp, Color.Gray)
                            .background(Color.White)
                            .pointerInput(tile) {
                                detectDragGestures(
                                    onDragEnd = { tryMatch() },
                                    onDrag = { change, dragAmount ->
                                        val (dx, dy) = dragAmount
                                        if (abs(dx) > 30 || abs(dy) > 30) {
                                            val direction = when {
                                                abs(dx) > abs(dy) && dx > 0 -> Pair(1, 0)    // Right
                                                abs(dx) > abs(dy) && dx < 0 -> Pair(-1, 0)   // Left
                                                abs(dy) > abs(dx) && dy > 0 -> Pair(0, 1)    // Down
                                                abs(dy) > abs(dx) && dy < 0 -> Pair(0, -1)   // Up
                                                else -> null
                                            }

                                            direction?.let { (dxDir, dyDir) ->
                                                val nx = x + dxDir
                                                val ny = y + dyDir
                                                if (nx in 0 until GameLogic.BOARD_SIZE && ny in 0 until GameLogic.BOARD_SIZE) {
                                                    // Swap tiles
                                                    val mutableBoard = board.map { it.toMutableList() }.toMutableList()
                                                    val temp = mutableBoard[y][x].type
                                                    mutableBoard[y][x].type = mutableBoard[ny][nx].type
                                                    mutableBoard[ny][nx].type = temp
                                                    board = mutableBoard
                                                }
                                            }

                                            change.consume()
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getTreatEmoji(tile.type),
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun getTreatEmoji(type: TreatType): String {
    return when (type) {
        TreatType.CUPCAKE -> "🧁"
        TreatType.COOKIE -> "🍪"
        TreatType.DONUT -> "🍩"
        TreatType.ICECREAM -> "🍨"
        TreatType.CANDY -> "🍬"
        TreatType.CAKE -> "🍰"
        TreatType.CHOCOLATE -> "🍫"
        TreatType.SHAVED_ICE -> "🍧"
        TreatType.LOLLIPOP -> "🍭"
        TreatType.PIE -> "🥧"
    }
}
