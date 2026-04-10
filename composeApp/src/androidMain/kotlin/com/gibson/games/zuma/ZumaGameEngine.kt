package com.gibson.games.zuma

import kotlin.random.Random

class ZumaGameEngine {

    private var nextId = 0

    fun createInitialState(): ZumaGameState {
        val chain = ZumaChain()

        // create starting balls
        repeat(10) {
            chain.balls.add(
                ZumaBall(
                    id = nextId++,
                    color = randomColor(),
                    progress = it * 0.05f
                )
            )
        }

        return ZumaGameState(chain = chain)
    }

    fun update(state: ZumaGameState): ZumaGameState {
        if (state.isGameOver) return state

        val newBalls = state.chain.balls.map {
            it.copy(progress = it.progress + 0.002f)
        }.toMutableList()

        val reachedEnd = newBalls.any { it.progress >= 1f }

        return state.copy(
            chain = ZumaChain(newBalls),
            isGameOver = reachedEnd
        )
    }

    private fun randomColor(): BallColor {
        return BallColor.values().random()
    }
}
