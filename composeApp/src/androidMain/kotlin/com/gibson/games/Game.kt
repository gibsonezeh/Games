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
    ),

    ZUMA(
        displayName = "Zuma Game",
        description = "Shoot colored balls, match 3, and stop the chain before it reaches the end.",
        accentColor = Color(0xFFEF4444) // 🔴 red/orange vibe for action
    )
}
