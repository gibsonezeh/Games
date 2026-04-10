package com.gibson.games.zuma

data class ZumaGameState(
    val chain: ZumaChain = ZumaChain(),
    val shooter: ZumaShooter = ZumaShooter(
        currentBall = BallColor.RED,
        nextBall = BallColor.BLUE
    ),
    val score: Int = 0,
    val isGameOver: Boolean = false
)
