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
import kotlinx.coroutines.delay // For potential future animations
import kotlin.math.abs
import kotlin.math.roundToInt

// Assuming GameLogic and GameTile/TreatType are in the same package
// or correctly imported if placed in commonMain/shared modules.

@Composable
fun DreamTreatsGameScreen() {
    var board by remember { mutableStateOf(GameLogic.createBoard()) }
    var score by remember { mutableStateOf(0) }
    var selectedTile by remember { mutableStateOf<GameTile?>(null) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }

    // Function to process matches and update board state
    // Consider making this a suspend function if adding delays/animations
    fun processMatches() {
        var currentBoard = board
        var totalScoreIncrease = 0
        while (true) {
            val matches = GameLogic.detectMatches(currentBoard)
            if (matches.isEmpty()) break
            totalScoreIncrease += matches.size * 10 // Simple scoring
            currentBoard = GameLogic.clearMatchesAndRefill(currentBoard, matches)
            // Add delay here if animating refills
        }
        if (totalScoreIncrease > 0) {
            board = currentBoard
            score += totalScoreIncrease
        }
    }

    // Initial match check on launch
    LaunchedEffect(Unit) {
        processMatches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)) // Light cyan background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🍬 Dream Treats 🍭",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00796B)), // Teal color
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.h6.copy(color = Color(0xFFAD1457)), // Pink color
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Game Board
        Box(modifier = Modifier.aspectRatio(1f).border(2.dp, Color.Gray)) {
            val tileSize = 48.dp // Define tile size for calculations
            Column {
                board.forEachIndexed { y, row ->
                    Row {
                        row.forEachIndexed { x, tile ->
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .padding(1.dp)
                                    .background(if (tile.isMatched) Color.LightGray else Color.White) // Indicate matched tiles briefly if needed
                                    .border(1.dp, Color(0xFFBDBDBD)) // Lighter border
                                    .pointerInput(tile.id) { // Use tile.id for stable key
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                selectedTile = tile
                                                dragStartOffset = offset
                                                currentDragOffset = offset
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                currentDragOffset += dragAmount
                                                // Optional: Add visual feedback during drag
                                            },
                                            onDragEnd = {
                                                val dragDistance = currentDragOffset - dragStartOffset
                                                val startTile = selectedTile
                                                selectedTile = null // Reset selected tile

                                                if (startTile != null && (abs(dragDistance.x) > tileSize.toPx() * 0.4 || abs(dragDistance.y) > tileSize.toPx() * 0.4)) {
                                                    val (dx, dy) = dragDistance
                                                    val (targetX, targetY) = when {
                                                        abs(dx) > abs(dy) && dx > 0 -> startTile.x + 1 to startTile.y // Right
                                                        abs(dx) > abs(dy) && dx < 0 -> startTile.x - 1 to startTile.y // Left
                                                        abs(dy) > abs(dx) && dy > 0 -> startTile.x to startTile.y + 1 // Down
                                                        abs(dy) > abs(dx) && dy < 0 -> startTile.x to startTile.y - 1 // Up
                                                        else -> startTile.x to startTile.y // No significant direction
                                                    }

                                                    if (targetX != startTile.x || targetY != startTile.y) { // Check if target is different
                                                        if (GameLogic.isSwapValid(board, startTile.x, startTile.y, targetX, targetY)) {
                                                            // Perform the swap
                                                            val mutableBoard = board.map { it.toMutableList() }.toMutableList()
                                                            val temp = mutableBoard[startTile.y][startTile.x]
                                                            mutableBoard[startTile.y][startTile.x] = mutableBoard[targetY][targetX].copy(x = startTile.x, y = startTile.y)
                                                            mutableBoard[targetY][targetX] = temp.copy(x = targetX, y = targetY)
                                                            board = mutableBoard // Update state to trigger recomposition

                                                            // Process matches after swap
                                                            processMatches()
                                                        } else {
                                                            // Optional: Add feedback for invalid swap (e.g., shake animation)
                                                        }
                                                    }
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
    }
}

// Helper function to get emoji for a treat type
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

