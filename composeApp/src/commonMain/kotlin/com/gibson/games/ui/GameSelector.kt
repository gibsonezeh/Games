package com.gibson.games.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gibson.games.core.GameType
import com.gibson.games.gamehub.subway.SubwayScreen

@Composable
fun GameSelector() {
    var selectedGame by remember { mutableStateOf<GameType?>(null) }

    when (selectedGame) {
        GameType.SUBWAY -> SubwayScreen()
        null -> GameList { selectedGame = it }
    }
}

@Composable
fun GameList(onGameSelected: (GameType) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = { onGameSelected(GameType.SUBWAY) }) {
            Text("🚇 Subway Runner")
        }

        // Future game placeholder
        Button(onClick = { /* onGameSelected(GameType.FUTURE_GAME) */ }) {
            Text("🕹️ Future Game")
        }
    }
}
