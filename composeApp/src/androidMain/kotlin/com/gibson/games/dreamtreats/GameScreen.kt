package com.gibson.games.dreamtreats

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun DreamTreatsGameScreen() {
    var board by remember { mutableStateOf(GameLogic.createBoard()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍬 Dream Treats", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, tile ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Gray)
                            .background(getTreatColor(tile.type))
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            board = GameLogic.createBoard()
        }) {
            Text("Shuffle")
        }
    }
}

fun getTreatColor(type: TreatType): Color {
    return when (type) {
        TreatType.CUPCAKE -> Color.Magenta
        TreatType.COOKIE -> Color.Yellow
        TreatType.DONUT -> Color.Cyan
        TreatType.ICECREAM -> Color.Green
        TreatType.CANDY -> Color.Red
    }
}
