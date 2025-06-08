package com.gibson.games.gamehub.subway

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.gibson.games.core.GameLoop
import com.gibson.games.core.GameState
import com.gibson.games.core.SwipeDirection
import com.gibson.games.core.Vector2
import com.gibson.games.engine.CollisionManager
import com.gibson.games.engine.PlayerController
import com.gibson.games.engine.RoadGenerator
import com.gibson.games.model.Player
import kotlinx.coroutines.CoroutineScope

class SubwayLogic(private val scope: CoroutineScope) {

    private val gameLoop = GameLoop(scope)
    private val player = Player(Vector2(0f, 0f), Vector2(50f, 100f)) // Initial player position and size
    private val playerController = PlayerController(player)
    private val roadGenerator = RoadGenerator()

    private val _gameState = mutableStateOf(GameState(player, emptyList(), 0, false))
    val gameState: State<GameState> = _gameState

    init {
        roadGenerator.generateInitialRoad(20, 1000f) // Generate initial road with 20 obstacles over 1000 units
        startGame()
    }

    fun startGame() {
        gameLoop.startGame(onUpdate = ::updateGame)
    }

    fun stopGame() {
        gameLoop.stopGame()
    }

    fun handleSwipe(direction: SwipeDirection) {
        playerController.handleSwipe(direction)
    }

    private fun updateGame(deltaTime: Long) {
        if (_gameState.value.isGameOver) return

        playerController.update(deltaTime)
        roadGenerator.update(player.position.y, 1000f) // Update road based on player's Z position

        val currentObstacles = roadGenerator.getObstacles()
        val collision = CollisionManager.checkCollision(player, currentObstacles)

        if (collision) {
            _gameState.value = _gameState.value.copy(isGameOver = true)
            stopGame()
        } else {
            // Update score based on distance traveled
            val newScore = _gameState.value.score + (deltaTime / 100).toInt() // Example scoring
            _gameState.value = _gameState.value.copy(
                player = player,
                obstacles = currentObstacles,
                score = newScore
            )
        }
    }
}

