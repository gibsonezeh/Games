package com.gibson.games.zuma

import androidx.compose.ui.graphics.Color

fun BallColor.toColor(): Color = when (this) {
    BallColor.RED -> Color.Red
    BallColor.GREEN -> Color.Green
    BallColor.BLUE -> Color.Blue
    BallColor.YELLOW -> Color.Yellow
    BallColor.PURPLE -> Color.Magenta
}
