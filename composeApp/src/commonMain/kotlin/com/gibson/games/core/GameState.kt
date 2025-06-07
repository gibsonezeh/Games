package com.gibson.games.core

sealed class GameState {
    object Menu : GameState()
    object Playing : GameState()
    object GameOver : GameState()
}
