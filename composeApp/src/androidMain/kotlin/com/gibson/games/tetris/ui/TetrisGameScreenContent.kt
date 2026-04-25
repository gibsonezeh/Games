package com.gibson.games.tetris.ui

import android.graphics.Paint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gibson.games.tetris.engine.*
import com.gibson.games.tetris.ui.theme.TetrisBrick
import com.gibson.games.tetris.ui.theme.TetrisBrickGhost
import com.gibson.games.tetris.ui.theme.TetrisScreenBackground
import kotlin.math.min

@Composable
fun TetrisGameScreenContent(
    state: TetrisState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(Color.Black)
            .padding(1.dp)
            .background(TetrisScreenBackground)
            .padding(10.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "tetris_text")

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "text_alpha"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val brickSize = min(
                size.width * 0.65f / state.matrixWidth,
                size.height / state.matrixHeight
            )

            drawMatrix(
                brickSize = brickSize,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )

            drawMatrixBorder(
                brickSize = brickSize,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )

            drawBricks(
                bricks = state.bricks,
                brickSize = brickSize,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )

            drawTetromino(
                tetromino = state.currentPiece,
                brickSize = brickSize,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )

            drawStatusText(
                gameStatus = state.gameStatus,
                brickSize = brickSize,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight,
                alpha = alpha
            )
        }

        TetrisScoreboard(
            nextPiece = if (state.currentPiece == Tetromino.Empty) {
                Tetromino.Empty
            } else {
                state.nextPiece.rotate()
            },
            score = state.score,
            lines = state.lines,
            level = state.level,
            isMute = state.isMute,
            isPaused = state.isPaused
        )
    }
}

@Composable
fun TetrisScoreboard(
    modifier: Modifier = Modifier,
    brickSize: Float = 35f,
    nextPiece: Tetromino,
    score: Int = 0,
    lines: Int = 0,
    level: Int = 1,
    isMute: Boolean = false,
    isPaused: Boolean = false
) {
    Row(modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(0.65f))

        Column(
            Modifier
                .fillMaxHeight()
                .weight(0.35f)
        ) {
            val textSize = 12.sp
            val margin = 12.dp

            Text("Score", fontSize = textSize, color = Color.Black)
            LedNumber(Modifier.fillMaxWidth(), score, 6)

            Spacer(modifier = Modifier.height(margin))

            Text("Lines", fontSize = textSize, color = Color.Black)
            LedNumber(Modifier.fillMaxWidth(), lines, 6)

            Spacer(modifier = Modifier.height(margin))

            Text("Level", fontSize = textSize, color = Color.Black)
            LedNumber(Modifier.fillMaxWidth(), level, 1)

            Spacer(modifier = Modifier.height(margin))

            Text("Next", fontSize = textSize, color = Color.Black)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(10.dp)
            ) {
                drawMatrix(
                    brickSize = brickSize,
                    matrixWidth = NEXT_MATRIX_WIDTH,
                    matrixHeight = NEXT_MATRIX_HEIGHT
                )

                drawTetromino(
                    tetromino = nextPiece.adjustOffset(
                        matrixWidth = NEXT_MATRIX_WIDTH,
                        matrixHeight = NEXT_MATRIX_HEIGHT
                    ),
                    brickSize = brickSize,
                    matrixWidth = NEXT_MATRIX_WIDTH,
                    matrixHeight = NEXT_MATRIX_HEIGHT
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isMute) "🔇" else "♪",
                    fontSize = 14.sp,
                    color = if (isMute) TetrisBrick else TetrisBrickGhost
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isPaused) "Ⅱ" else "▸",
                    fontSize = 14.sp,
                    color = if (isPaused) TetrisBrick else TetrisBrickGhost
                )

                Spacer(modifier = Modifier.weight(1f))

                LedClock()
            }
        }
    }
}

private fun DrawScope.drawStatusText(
    gameStatus: TetrisGameStatus,
    brickSize: Float,
    matrixWidth: Int,
    matrixHeight: Int,
    alpha: Float
) {
    val text = when (gameStatus) {
        TetrisGameStatus.Onboard -> "TETRIS"
        TetrisGameStatus.GameOver -> "GAME OVER"
        else -> null
    } ?: return

    drawContext.canvas.nativeCanvas.drawText(
        text,
        brickSize * matrixWidth / 2,
        brickSize * matrixHeight / 2,
        Paint().apply {
            color = Color.Black.copy(alpha = alpha).toArgb()
            textSize = if (text == "TETRIS") 80f else 48f
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 4f
        }
    )
}

private fun DrawScope.drawMatrix(
    brickSize: Float,
    matrixWidth: Int,
    matrixHeight: Int
) {
    for (x in 0 until matrixWidth) {
        for (y in 0 until matrixHeight) {
            drawBrick(
                brickSize = brickSize,
                x = x,
                y = y,
                color = TetrisBrickGhost
            )
        }
    }
}

private fun DrawScope.drawMatrixBorder(
    brickSize: Float,
    matrixWidth: Int,
    matrixHeight: Int
) {
    val gap = matrixWidth * brickSize * 0.05f

    drawRect(
        color = Color.Black,
        topLeft = Offset(-gap / 2, -gap / 2),
        size = Size(
            matrixWidth * brickSize + gap,
            matrixHeight * brickSize + gap
        ),
        style = Stroke(1.dp.toPx())
    )
}

private fun DrawScope.drawBricks(
    bricks: List<Brick>,
    brickSize: Float,
    matrixWidth: Int,
    matrixHeight: Int
) {
    clipRect(
        left = 0f,
        top = 0f,
        right = matrixWidth * brickSize,
        bottom = matrixHeight * brickSize
    ) {
        bricks.forEach { brick ->
            drawBrick(
                brickSize = brickSize,
                x = brick.x,
                y = brick.y,
                color = TetrisBrick
            )
        }
    }
}

private fun DrawScope.drawTetromino(
    tetromino: Tetromino,
    brickSize: Float,
    matrixWidth: Int,
    matrixHeight: Int
) {
    clipRect(
        left = 0f,
        top = 0f,
        right = matrixWidth * brickSize,
        bottom = matrixHeight * brickSize
    ) {
        tetromino.cells.forEach { cell ->
            drawBrick(
                brickSize = brickSize,
                x = cell.x,
                y = cell.y,
                color = TetrisBrick
            )
        }
    }
}

private fun DrawScope.drawBrick(
    brickSize: Float,
    x: Int,
    y: Int,
    color: Color
) {
    val actualLocation = Offset(
        x = x * brickSize,
        y = y * brickSize
    )

    val outerSize = brickSize * 0.8f
    val outerOffset = (brickSize - outerSize) / 2

    drawRect(
        color = color,
        topLeft = actualLocation + Offset(outerOffset, outerOffset),
        size = Size(outerSize, outerSize),
        style = Stroke(outerSize / 10)
    )

    val innerSize = brickSize * 0.5f
    val innerOffset = (brickSize - innerSize) / 2

    drawRect(
        color = color,
        topLeft = actualLocation + Offset(innerOffset, innerOffset),
        size = Size(innerSize, innerSize)
    )
}
