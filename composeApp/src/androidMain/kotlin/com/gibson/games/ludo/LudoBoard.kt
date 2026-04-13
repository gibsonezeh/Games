package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun LudoBoard(
    boardState: BoardState,
    gameRules: GameRules,
    selectedToken: Token?,
    movableTokens: List<Token>,
    displayedPosition: (Token) -> Int,
    isAnimatingMove: Boolean,
    onTokenClick: (Token) -> Unit,
    centerDiceState: CenterDiceAnimState,
    die1Display: Int,
    die2Display: Int,
    isRolling: Boolean,
    onCenterDiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .sizeIn(maxWidth = 420.dp, maxHeight = 420.dp)
    ) {
        val boardSizePx = with(density) { minOf(maxWidth.toPx(), maxHeight.toPx()) }
        val squareSizePx = boardSizePx / 15f

        Box(modifier = Modifier.fillMaxSize()) {

            // ================= BOARD =================
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boardSize = min(size.width, size.height)
                val squareSize = boardSize / 15f

                val green = Color(0xFF00B04F)
                val blue = Color(0xFF0066CC)
                val yellow = Color(0xFFFFD700)
                val red = Color(0xFFC8102E)
                val white = Color.White
                val black = Color.Black
                val darkGray = Color(0xFF333333)

                drawRect(
                    color = Color(0xFFE0E0E0),
                    topLeft = Offset(-squareSize * 0.5f, -squareSize * 0.5f),
                    size = Size(boardSize + squareSize, boardSize + squareSize)
                )

                drawRect(
                    color = darkGray,
                    topLeft = Offset(-squareSize * 0.5f, -squareSize * 0.5f),
                    size = Size(boardSize + squareSize, boardSize + squareSize),
                    style = Stroke(width = 8f)
                )

                drawRect(color = white, size = Size(boardSize, boardSize))

                // Home squares
                drawRect(color = green, size = Size(squareSize * 6, squareSize * 6))
                drawRect(color = red, topLeft = Offset(squareSize * 9, 0f), size = Size(squareSize * 6, squareSize * 6))
                drawRect(color = yellow, topLeft = Offset(0f, squareSize * 9), size = Size(squareSize * 6, squareSize * 6))
                drawRect(color = blue, topLeft = Offset(squareSize * 9, squareSize * 9), size = Size(squareSize * 6, squareSize * 6))

                val centerTopLeft = Offset(squareSize * 6, squareSize * 6)

                drawRoundRect(
                    color = white,
                    topLeft = centerTopLeft,
                    size = Size(squareSize * 3, squareSize * 3),
                    cornerRadius = CornerRadius(squareSize * 0.3f)
                )

                drawRoundRect(
                    color = black,
                    topLeft = centerTopLeft,
                    size = Size(squareSize * 3, squareSize * 3),
                    cornerRadius = CornerRadius(squareSize * 0.3f),
                    style = Stroke(width = 3f)
                )

                fun drawToken(x: Float, y: Float, color: Color) {
                    drawCircle(color = color, radius = squareSize * 0.35f, center = Offset(x, y))
                }

                val tokens = boardState.players.flatMap { it.tokens }

                tokens.forEach { token ->
                    val pos = getTokenCoordinates(token.copy(position = displayedPosition(token)), squareSize)

                    drawToken(
                        pos.x,
                        pos.y,
                        when (token.color) {
                            PlayerColor.GREEN -> green
                            PlayerColor.RED -> red
                            PlayerColor.YELLOW -> yellow
                            PlayerColor.BLUE -> blue
                        }
                    )
                }
            }

            // ================= CLICKABLE TOKENS =================
            if (boardState.gamePhase == GamePhase.MOVING) {
                movableTokens.forEach { token ->
                    val coords = getTokenCoordinates(token.copy(position = displayedPosition(token)), squareSizePx)

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { coords.x.toDp() - 20.dp },
                                y = with(density) { coords.y.toDp() - 20.dp }
                            )
                            .size(40.dp)
                            .clickable(enabled = !isAnimatingMove) {
                                if (!isAnimatingMove) onTokenClick(token)
                            }
                    )
                }
            }

            // ================= CENTER DICE =================
            val centerOffset = remember(squareSizePx) {
                Offset(squareSizePx * 7.5f, squareSizePx * 7.5f)
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { centerOffset.x.toDp() - 90.dp },
                        y = with(density) { centerOffset.y.toDp() - 90.dp }
                    )
                    .size(180.dp)
            ) {
                CenterDiceRoller(
                    die1Value = die1Display,
                    die2Value = die2Display,
                    animationState = centerDiceState,
                    isRolling = isRolling,
                    onClick = onCenterDiceClick
                )
            }
        }
    }
}
