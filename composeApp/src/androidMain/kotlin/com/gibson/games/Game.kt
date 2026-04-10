package com.gibson.games

import androidx.compose.ui.graphics.Color

enum class Game(
    val displayName: String,
    val description: String,
    val accentColor: Color
) {
    LUDO(
        displayName = "Ludo Game",
        description = "Play the bird-themed classic board game with custom rules and colorful gameplay.",
        accentColor = Color(0xFF10B981)
    )
}