// commonMain/com/gibson/games/ui/MultiGameApp.kt

package com.gibson.games.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.gibson.games.*



// --- Main App Navigation ---

/**
 * Top-level composable that manages navigation between the main menu and different games.
 */
@Composable
fun MultiGameApp() {
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (selectedGame == null) {
            GameMenu(onGameSelected = { selectedGame = it })
        } else {
            // When a game is selected, show the GameScreen.
            // The onExit lambda sets the selected game back to null, returning to the menu.
            GameScreen(game = selectedGame!!) {
                selectedGame = null
            }
        }
    }
}
