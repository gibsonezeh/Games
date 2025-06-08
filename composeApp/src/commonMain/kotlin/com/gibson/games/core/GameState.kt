package com.gibson.games.core

import com.gibson.games.model.Player
import com.gibson.games.model.Obstacle

data class GameState(
    val player: Player,
    val obstacles: List<Obstacle>,
    val score: Int,
    val isGameOver: Boolean
)

