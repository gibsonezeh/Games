package com.gibson.games.zuma

enum class BallColor {
    RED, GREEN, BLUE, YELLOW, PURPLE
}

data class ZumaBall(
    val id: Int,
    val color: BallColor,
    var progress: Float // 0f → 1f along path
)
