package com.gibson.games.candycrush

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CandyCrushScreen(onBack: () -> Unit = {}) {
    val game = remember { CandyCrushGame() }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var trigger by remember { mutableStateOf(0) } // triggers recomposition

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🍬 Candy Crush Clone", fontSize = 24.sp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(64) { index ->
                val x = index % 8
                val y = index / 8
                val candy = game.get(x, y)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(if (selectedIndex == index) Color.LightGray else Color.White)
                        .clickable {
                            if (selectedIndex == null) {
                                selectedIndex = index
                            } else {
                                val prev = selectedIndex!!
                                val px = prev % 8
                                val py = prev / 8
                                if (game.swap(px, py, x, y)) {
                                    game.processMatches()
                                }
                                selectedIndex = null
                                trigger++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = candy, fontSize = 24.sp)
                }
            }
        }

        Button(onClick = {
            game.reshuffle()
            selectedIndex = null
            trigger++
        }) {
            Text("🔄 Shuffle Board")
        }

        Button(onClick = onBack) {
            Text("⬅️ Back")
        }
    }
}
