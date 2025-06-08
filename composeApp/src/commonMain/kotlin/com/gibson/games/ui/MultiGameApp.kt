// commonMain/com/gibson/games/ui/MultiGameApp.kt

package com.gibson.games.ui

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.gibson.games.*

@Composable
fun MultiGameApp() {
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (selectedGame == null) {
            GameMenu(onGameSelected = { selectedGame = it })
        } else {
            GameScreen(game = selectedGame!!) {
                selectedGame = null // Go back to main menu
            }
        }
    }
}
