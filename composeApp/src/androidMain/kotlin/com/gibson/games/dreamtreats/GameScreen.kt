package com.gibson.games.dreamtreats

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DreamTreatsGameScreen() {
    var board by remember { mutableStateOf(GameLogic.createBoard()) }

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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        board.forEach { row ->
            Row {
                row.forEach { tile ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(2.dp)
                            .border(1.dp, MaterialTheme.colors.primary)
                            .background(MaterialTheme.colors.surface),
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

        Button(onClick = {
            board = GameLogic.createBoard()
        }) {
            Text("🔀 Shuffle")
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
