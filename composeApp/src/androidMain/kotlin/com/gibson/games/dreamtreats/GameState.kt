package com.gibson.games.dreamtreats

data class GameState(
    val board: List<List<GameTile>>,
    val score: Int = 0,
    val moves: Int = 30
)
