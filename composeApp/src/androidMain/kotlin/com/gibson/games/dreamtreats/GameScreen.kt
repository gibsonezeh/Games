package com.gibson.games.dreamtreats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DreamTreatsGameScreen() {
    var board by remember { mutableStateOf(GameLogic.createBoard()) }
    var matches by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var score by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
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

        board.forEach { row ->
            Row {
                row.forEach { tile ->
                    val isMatched = matches.contains(tile.x to tile.y)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(2.dp)
                            .border(1.dp, Color.Gray)
                            .background(if (isMatched) Color.Yellow else Color.White),
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

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                // Detect matches
                matches = GameLogic.detectMatches(board)
                if (matches.isNotEmpty()) {
                    board = GameLogic.clearMatchesAndRefill(board, matches)
                    score += matches.size * 10
                    matches = emptySet() // Reset to allow new detection
                }
            }) {
                Text("🔍 Match")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(onClick = {
                board = GameLogic.createBoard()
                score = 0
                matches = emptySet()
            }) {
                Text("🔄 Reset")
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
