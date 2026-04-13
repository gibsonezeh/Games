package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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

                drawRect(
                    color = green,
                    topLeft = Offset(0f, 0f),
                    size = Size(squareSize * 6, squareSize * 6)
                )
                drawRect(
                    color = black,
                    topLeft = Offset(0f, 0f),
                    size = Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )

                drawRect(
                    color = red,
                    topLeft = Offset(squareSize * 9, 0f),
                    size = Size(squareSize * 6, squareSize * 6)
                )
                drawRect(
                    color = black,
                    topLeft = Offset(squareSize * 9, 0f),
                    size = Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )

                drawRect(
                    color = yellow,
                    topLeft = Offset(0f, squareSize * 9),
                    size = Size(squareSize * 6, squareSize * 6)
                )
                drawRect(
                    color = black,
                    topLeft = Offset(0f, squareSize * 9),
                    size = Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )

                drawRect(
                    color = blue,
                    topLeft = Offset(squareSize * 9, squareSize * 9),
                    size = Size(squareSize * 6, squareSize * 6)
                )
                drawRect(
                    color = black,
                    topLeft = Offset(squareSize * 9, squareSize * 9),
                    size = Size(squareSize * 6, squareSize * 6),
                    style = Stroke(width = 3f)
                )

                val centerTopLeft = Offset(squareSize * 6, squareSize * 6)
                val centerSize = Size(squareSize * 3, squareSize * 3)

                drawRoundRect(
                    color = white,
                    topLeft = centerTopLeft,
                    size = centerSize,
                    cornerRadius = CornerRadius(squareSize * 0.3f)
                )
                drawRoundRect(
                    color = black,
                    topLeft = centerTopLeft,
                    size = centerSize,
                    cornerRadius = CornerRadius(squareSize * 0.3f),
                    style = Stroke(width = 3f)
                )

                fun drawGameSquare(x: Int, y: Int, color: Color, hasBorder: Boolean = true) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x * squareSize, y * squareSize),
                        size = Size(squareSize, squareSize)
                    )
                    if (hasBorder) {
                        drawRect(
                            color = black,
                            topLeft = Offset(x * squareSize, y * squareSize),
                            size = Size(squareSize, squareSize),
                            style = Stroke(width = 2f)
                        )
                    }
                }

                for (i in 0 until 6) {
                    drawGameSquare(6, i, white)
                    drawGameSquare(7, i, if (i == 1) red else white)
                    drawGameSquare(8, i, white)

                    drawGameSquare(6, i + 9, white)
                    drawGameSquare(7, i + 9, if (i == 4) yellow else white)
                    drawGameSquare(8, i + 9, white)
                }

                for (i in 0 until 6) {
                    drawGameSquare(i, 6, white)
                    drawGameSquare(i, 7, if (i == 1) green else white)
                    drawGameSquare(i, 8, white)

                    drawGameSquare(i + 9, 6, white)
                    drawGameSquare(i + 9, 7, if (i == 4) blue else white)
                    drawGameSquare(i + 9, 8, white)
                }

                drawGameSquare(1, 6, green)
                drawStar(
                    Offset(1.5f * squareSize, 6.5f * squareSize),
                    squareSize * 0.3f,
                    Color.White
                )

                drawGameSquare(8, 1, red)
                drawStar(
                    Offset(8.5f * squareSize, 1.5f * squareSize),
                    squareSize * 0.3f,
                    Color.White
                )

                drawGameSquare(13, 8, blue)
                drawStar(
                    Offset(13.5f * squareSize, 8.5f * squareSize),
                    squareSize * 0.3f,
                    Color.White
                )

                drawGameSquare(6, 13, yellow)
                drawStar(
                    Offset(6.5f * squareSize, 13.5f * squareSize),
                    squareSize * 0.3f,
                    Color.White
                )

                for (i in 1..5) {
                    drawStar(
                        Offset(7.5f * squareSize, (i + 0.5f) * squareSize),
                        squareSize * 0.3f,
                        red
                    )
                    drawStar(
                        Offset(7.5f * squareSize, (i + 8.5f) * squareSize),
                        squareSize * 0.3f,
                        yellow
                    )
                    drawStar(
                        Offset((i + 0.5f) * squareSize, 7.5f * squareSize),
                        squareSize * 0.3f,
                        green
                    )
                    drawStar(
                        Offset((i + 8.5f) * squareSize, 7.5f * squareSize),
                        squareSize * 0.3f,
                        blue
                    )
                }

                fun DrawScope.drawEmoji(
                    emoji: String,
                    centerX: Float,
                    centerY: Float,
                    fontSize: TextUnit,
                    color: Color = Color.Black
                ) {
                    val measured = textMeasurer.measure(
                        text = AnnotatedString(emoji),
                        style = TextStyle(fontSize = fontSize, color = color)
                    )
                    val textWidth = measured.size.width.toFloat()
                    val textHeight = measured.size.height.toFloat()

                    drawText(
                        textMeasurer = textMeasurer,
                        text = emoji,
                        topLeft = Offset(
                            centerX - textWidth / 2f,
                            centerY - textHeight / 2f
                        ),
                        style = TextStyle(fontSize = fontSize, color = color)
                    )
                }

                fun drawToken(
                    centerX: Float,
                    centerY: Float,
                    color: Color,
                    isSelected: Boolean = false,
                    isMovable: Boolean = false
                ) {
                    val tokenRadius = squareSize * 0.35f

                    if (isMovable) {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = 0.5f),
                            radius = tokenRadius * 1.3f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    if (isSelected) {
                        drawCircle(
                            color = Color.White,
                            radius = tokenRadius * 1.2f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = tokenRadius,
                        center = Offset(centerX + 2f, centerY + 2f)
                    )

                    drawCircle(
                        color = color,
                        radius = tokenRadius,
                        center = Offset(centerX, centerY)
                    )

                    drawCircle(
                        color = black,
                        radius = tokenRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = white.copy(alpha = 0.6f),
                        radius = tokenRadius * 0.3f,
                        center = Offset(
                            centerX - tokenRadius * 0.3f,
                            centerY - tokenRadius * 0.3f
                        )
                    )
                }

                fun DrawScope.drawStackedToken(
                    center: Offset,
                    squareSize: Float,
                    tokens: List<Token>,
                    isMovable: Boolean,
                    isSelected: Boolean
                ) {
                    val tokenRadius = squareSize * 0.35f
                    val topLeft = Offset(center.x - tokenRadius, center.y - tokenRadius)
                    val arcSize = Size(tokenRadius * 2f, tokenRadius * 2f)

                    val colorGroups = tokens.groupBy { it.color }
                    val orderedGroups = buildList {
                        if (colorGroups.containsKey(PlayerColor.GREEN)) add(PlayerColor.GREEN)
                        if (colorGroups.containsKey(PlayerColor.RED)) add(PlayerColor.RED)
                        if (colorGroups.containsKey(PlayerColor.YELLOW)) add(PlayerColor.YELLOW)
                        if (colorGroups.containsKey(PlayerColor.BLUE)) add(PlayerColor.BLUE)
                    }

                    fun colorFor(playerColor: PlayerColor): Color {
                        return when (playerColor) {
                            PlayerColor.GREEN -> green
                            PlayerColor.RED -> red
                            PlayerColor.YELLOW -> yellow
                            PlayerColor.BLUE -> blue
                        }
                    }

                    if (isMovable) {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = 0.5f),
                            radius = tokenRadius * 1.35f,
                            center = center
                        )
                    }

                    if (isSelected) {
                        drawCircle(
                            color = Color.White,
                            radius = tokenRadius * 1.2f,
                            center = center
                        )
                    }

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = tokenRadius,
                        center = Offset(center.x + 2f, center.y + 2f)
                    )

                    val sweep = 360f / orderedGroups.size
                    var startAngle = -90f

                    orderedGroups.forEach { playerColor ->
                        drawArc(
                            color = colorFor(playerColor),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                        startAngle += sweep
                    }

                    drawCircle(
                        color = black,
                        radius = tokenRadius,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = white.copy(alpha = 0.25f),
                        radius = tokenRadius * 0.95f,
                        center = Offset(
                            center.x - tokenRadius * 0.08f,
                            center.y - tokenRadius * 0.08f
                        ),
                        style = Stroke(width = 1.5f)
                    )

                    val badgeDistance = tokenRadius * 0.62f
                    val badgeRadius = tokenRadius * 0.24f
                    startAngle = -90f + sweep / 2f

                    orderedGroups.forEach { playerColor ->
                        val count = colorGroups[playerColor]?.size ?: 0
                        if (count > 1) {
                            val angleRad = Math.toRadians(startAngle.toDouble())
                            val badgeCenter = Offset(
                                x = center.x + (badgeDistance * cos(angleRad)).toFloat(),
                                y = center.y + (badgeDistance * sin(angleRad)).toFloat()
                            )

                            drawCircle(
                                color = white,
                                radius = badgeRadius,
                                center = badgeCenter
                            )
                            drawCircle(
                                color = black,
                                radius = badgeRadius,
                                center = badgeCenter,
                                style = Stroke(width = 1.5f)
                            )

                            val badgeText = count.toString()
                            val measured = textMeasurer.measure(
                                text = AnnotatedString(badgeText),
                                style = TextStyle(
                                    fontSize = (squareSize * 0.18f).sp,
                                    color = black,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = badgeText,
                                topLeft = Offset(
                                    badgeCenter.x - measured.size.width / 2f,
                                    badgeCenter.y - measured.size.height / 2f
                                ),
                                style = TextStyle(
                                    fontSize = (squareSize * 0.18f).sp,
                                    color = black,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        startAngle += sweep
                    }
                }

                drawEmoji("🕊️", squareSize * 7.5f, squareSize * 7.5f, (squareSize * 2f).sp)
                drawEmoji("🦚", squareSize * 3f, squareSize * 3f, (squareSize * 3.5f).sp)
                drawEmoji("🦜", squareSize * 12f, squareSize * 3f, (squareSize * 3.5f).sp)
                drawEmoji("🐥", squareSize * 3f, squareSize * 12f, (squareSize * 3.5f).sp)
                drawEmoji("🐦", squareSize * 12f, squareSize * 12f, (squareSize * 3.5f).sp)

                val allTokens = boardState.players.flatMap { it.tokens }
                val groupedTokens = allTokens.groupBy { displayedPosition(it) }

                groupedTokens.forEach { (position, tokensAtPosition) ->
                    val shouldUseStackedSafeZoneView =
                        position in 0..51 &&
                            tokensAtPosition.size > 1 &&
                            isStackableSafeZonePosition(position, gameRules)

                    if (!shouldUseStackedSafeZoneView) {
    tokensAtPosition.forEach { token ->
        val drawnToken = token.copy(position = displayedPosition(token))
        val tokenCoords = getTokenCoordinates(drawnToken, squareSize)
        val isSelected =
            selectedToken?.id == token.id &&
                selectedToken?.color == token.color
        val isMovable = movableTokens.any {
            it.id == token.id && it.color == token.color
        }

        drawToken(
            centerX = tokenCoords.x,
            centerY = tokenCoords.y,
            color = when (token.color) {
                PlayerColor.GREEN -> green
                PlayerColor.RED -> red
                PlayerColor.YELLOW -> yellow
                PlayerColor.BLUE -> blue
            },
            isSelected = isSelected,
            isMovable = isMovable
        )
    }
} else {
    val drawnFirst = tokensAtPosition.first().copy(
        position = displayedPosition(tokensAtPosition.first())
    )
    val center = getTokenCoordinates(drawnFirst, squareSize)

    drawStackedToken(
        center = center,
        squareSize = squareSize,
        tokens = tokensAtPosition,
        isMovable = tokensAtPosition.any { token ->
            movableTokens.any {
                it.id == token.id && it.color == token.color
            }
        },
        isSelected = tokensAtPosition.any { token ->
            selectedToken?.id == token.id &&
                selectedToken?.color == token.color
        }
    )
                    }
                }
            }
        }
