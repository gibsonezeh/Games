package com.gibson.games.ludo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

fun didCaptureEnemy(
    before: BoardState,
    after: BoardState,
    currentPlayer: PlayerColor
): Boolean {
    val beforeEnemies = before.players
        .filter { it.color != currentPlayer }
        .flatMap { it.tokens }

    val afterEnemies = after.players
        .filter { it.color != currentPlayer }
        .flatMap { it.tokens }

    return beforeEnemies.any { beforeToken ->
        val afterToken = afterEnemies.firstOrNull {
            it.color == beforeToken.color && it.id == beforeToken.id
        }
        beforeToken.position != -1 && afterToken?.position == -1
    }
}

fun isStackableSafeZonePosition(position: Int, rules: GameRules): Boolean {
    val normalSafeZones = setOf(8, 21, 34, 47)
    val startingPoints = setOf(0, 13, 26, 39)

    return when {
        position in normalSafeZones -> true
        rules.startingPointIsSafeZoneForAll && position in startingPoints -> true
        rules.startingPointIsSafeZoneForColor && position in startingPoints -> true
        else -> false
    }
}

private fun getRelativeProgressForAnimation(token: Token): Int {
    return when (val pos = token.position) {
        -1 -> -1
        200 -> 56
        in 100..104 -> 51 + (pos - 100)
        in 0..51 -> {
            val start = getStartingPosition(token.color)
            val raw = (pos - start + 52) % 52
            if (raw == 51) 50 else raw
        }
        else -> -1
    }
}

private fun getPositionFromProgressForAnimation(color: PlayerColor, progress: Int): Int {
    return when {
        progress < 0 -> -1
        progress in 0..50 -> (getStartingPosition(color) + progress) % 52
        progress in 51..55 -> 100 + (progress - 51)
        progress >= 56 -> 200
        else -> -1
    }
}

fun buildMovementPath(token: Token, steps: Int): List<Int> {
    val currentProgress = getRelativeProgressForAnimation(token)
    return if (currentProgress == -1) {
        listOf(getStartingPosition(token.color))
    } else {
        val startProgress = currentProgress + 1
        val endProgress = currentProgress + steps
        (startProgress..endProgress).map { progress ->
            getPositionFromProgressForAnimation(token.color, progress)
        }
    }
}

fun getTokenCoordinates(token: Token, squareSize: Float): Offset {
    return when (token.position) {
        -1 -> {
            when (token.color) {
                PlayerColor.GREEN -> when (token.id) {
                    1 -> Offset(squareSize * 1.5f, squareSize * 1.5f)
                    2 -> Offset(squareSize * 4.5f, squareSize * 1.5f)
                    3 -> Offset(squareSize * 1.5f, squareSize * 4.5f)
                    4 -> Offset(squareSize * 4.5f, squareSize * 4.5f)
                    else -> Offset.Zero
                }

                PlayerColor.RED -> when (token.id) {
                    1 -> Offset(squareSize * 10.5f, squareSize * 1.5f)
                    2 -> Offset(squareSize * 13.5f, squareSize * 1.5f)
                    3 -> Offset(squareSize * 10.5f, squareSize * 4.5f)
                    4 -> Offset(squareSize * 13.5f, squareSize * 4.5f)
                    else -> Offset.Zero
                }

                PlayerColor.YELLOW -> when (token.id) {
                    1 -> Offset(squareSize * 1.5f, squareSize * 10.5f)
                    2 -> Offset(squareSize * 4.5f, squareSize * 10.5f)
                    3 -> Offset(squareSize * 1.5f, squareSize * 13.5f)
                    4 -> Offset(squareSize * 4.5f, squareSize * 13.5f)
                    else -> Offset.Zero
                }

                PlayerColor.BLUE -> when (token.id) {
                    1 -> Offset(squareSize * 10.5f, squareSize * 10.5f)
                    2 -> Offset(squareSize * 13.5f, squareSize * 10.5f)
                    3 -> Offset(squareSize * 10.5f, squareSize * 13.5f)
                    4 -> Offset(squareSize * 13.5f, squareSize * 13.5f)
                    else -> Offset.Zero
                }
            }
        }

        in 0..51 -> {
            val path = listOf(
                1 to 6,
                2 to 6,
                3 to 6,
                4 to 6,
                5 to 6,
                6 to 5,
                6 to 4,
                6 to 3,
                6 to 2,
                6 to 1,
                6 to 0,
                7 to 0,
                8 to 0,
                8 to 1,
                8 to 2,
                8 to 3,
                8 to 4,
                8 to 5,
                9 to 6,
                10 to 6,
                11 to 6,
                12 to 6,
                13 to 6,
                14 to 6,
                14 to 7,
                14 to 8,
                13 to 8,
                12 to 8,
                11 to 8,
                10 to 8,
                9 to 8,
                8 to 9,
                8 to 10,
                8 to 11,
                8 to 12,
                8 to 13,
                8 to 14,
                7 to 14,
                6 to 14,
                6 to 13,
                6 to 12,
                6 to 11,
                6 to 10,
                6 to 9,
                5 to 8,
                4 to 8,
                3 to 8,
                2 to 8,
                1 to 8,
                0 to 8,
                0 to 7,
                0 to 6
            )

            val (x, y) = path[token.position]
            Offset((x + 0.5f) * squareSize, (y + 0.5f) * squareSize)
        }

        in 100..104 -> {
            val homePathIndex = token.position - 100
            when (token.color) {
                PlayerColor.GREEN -> Offset(
                    (1 + homePathIndex + 0.5f) * squareSize,
                    (7 + 0.5f) * squareSize
                )

                PlayerColor.RED -> Offset(
                    (7 + 0.5f) * squareSize,
                    (1 + homePathIndex + 0.5f) * squareSize
                )

                PlayerColor.YELLOW -> Offset(
                    (7 + 0.5f) * squareSize,
                    (13 - homePathIndex + 0.5f) * squareSize
                )

                PlayerColor.BLUE -> Offset(
                    (13 - homePathIndex + 0.5f) * squareSize,
                    (7 + 0.5f) * squareSize
                )
            }
        }

        200 -> Offset(squareSize * 7.5f, squareSize * 7.5f)
        else -> Offset.Zero
    }
}

fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    drawPath(
        path = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        },
        color = color
    )
}

fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.4f
    var angle = -90.0

    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = center.x + (r * cos(Math.toRadians(angle))).toFloat()
        val y = center.y + (r * sin(Math.toRadians(angle))).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += 36.0
    }

    path.close()
    drawPath(path = path, color = color)
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.8f),
        style = Stroke(width = 2f)
    )
}
