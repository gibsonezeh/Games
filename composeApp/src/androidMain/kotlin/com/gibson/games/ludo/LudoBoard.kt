package com.gibson.games.ludo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    ) {
        val maxBoardSize = if (maxWidth < 500.dp) 340.dp else 420.dp
        val boardLimitPx = with(density) { maxBoardSize.toPx() }
        val boardSizePx = with(density) { minOf(maxWidth.toPx(), maxHeight.toPx(), boardLimitPx) }
        val squareSizePx = boardSizePx / 15f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(maxWidth = maxBoardSize, maxHeight = maxBoardSize)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardSize = min(size.width, size.height)
                    val squareSize = boardSize / 15f
                    val artScale = if (boardSize < 380f) 2.0f else 2.6f
                    val centerArtScale = if (boardSize < 380f) 1.2f else 1.5f

                    val green = Color(0xFF00B04F)
                    val blue = Color(0xFF0066CC)
                    val yellow = Color(0xFFFFD700)
                    val red = Color(0xFFC8102E)
                    val white = Color.White
                    val black = Color.Black

                    drawRoundRect(
                        color = Color(0xFFD9D9D9),
                        topLeft = Offset(-squareSize * 0.55f, -squareSize * 0.55f),
                        size = Size(boardSize + squareSize * 1.1f, boardSize + squareSize * 1.1f),
                        cornerRadius = CornerRadius(squareSize * 0.9f, squareSize * 0.9f)
                    )

                    drawRoundRect(
                        color = Color(0xFF8A8A8A),
                        topLeft = Offset(-squareSize * 0.42f, -squareSize * 0.42f),
                        size = Size(boardSize + squareSize * 0.84f, boardSize + squareSize * 0.84f),
                        cornerRadius = CornerRadius(squareSize * 0.75f, squareSize * 0.75f),
                        style = Stroke(width = squareSize * 0.22f)
                    )

                    drawRoundRect(
                        color = Color(0xFF2E2E2E),
                        topLeft = Offset(-squareSize * 0.24f, -squareSize * 0.24f),
                        size = Size(boardSize + squareSize * 0.48f, boardSize + squareSize * 0.48f),
                        cornerRadius = CornerRadius(squareSize * 0.55f, squareSize * 0.55f),
                        style = Stroke(width = squareSize * 0.08f)
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
                        center = Offset(1.5f * squareSize, 6.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )

                    drawGameSquare(8, 1, red)
                    drawStar(
                        center = Offset(8.5f * squareSize, 1.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )

                    drawGameSquare(13, 8, blue)
                    drawStar(
                        center = Offset(13.5f * squareSize, 8.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )

                    drawGameSquare(6, 13, yellow)
                    drawStar(
                        center = Offset(6.5f * squareSize, 13.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )

                    drawStar(
                        center = Offset(2.5f * squareSize, 8.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )
                    drawStar(
                        center = Offset(6.5f * squareSize, 2.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )
                    drawStar(
                        center = Offset(12.5f * squareSize, 6.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )
                    drawStar(
                        center = Offset(8.5f * squareSize, 12.5f * squareSize),
                        radius = squareSize * 0.3f,
                        color = white
                    )

                    for (i in 1..5) {
                        drawStar(
                            center = Offset(7.5f * squareSize, (i + 0.5f) * squareSize),
                            radius = squareSize * 0.3f,
                            color = red
                        )
                        drawStar(
                            center = Offset(7.5f * squareSize, (i + 8.5f) * squareSize),
                            radius = squareSize * 0.3f,
                            color = yellow
                        )
                        drawStar(
                            center = Offset((i + 0.5f) * squareSize, 7.5f * squareSize),
                            radius = squareSize * 0.3f,
                            color = green
                        )
                        drawStar(
                            center = Offset((i + 8.5f) * squareSize, 7.5f * squareSize),
                            radius = squareSize * 0.3f,
                            color = blue
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
                        drawText(
                            textMeasurer = textMeasurer,
                            text = emoji,
                            topLeft = Offset(
                                centerX - measured.size.width / 2f,
                                centerY - measured.size.height / 2f
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

                    drawEmoji("🕊️", squareSize * 7.5f, squareSize * 7.5f, (squareSize * centerArtScale).sp)
                    drawEmoji("🦚", squareSize * 3f, squareSize * 3f, (squareSize * artScale).sp)
                    drawEmoji("🦜", squareSize * 12f, squareSize * 3f, (squareSize * artScale).sp)
                    drawEmoji("🐥", squareSize * 3f, squareSize * 12f, (squareSize * artScale).sp)
                    drawEmoji("🐦", squareSize * 12f, squareSize * 12f, (squareSize * artScale).sp)

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

                if (boardState.gamePhase == GamePhase.MOVING) {
                    movableTokens.forEach { token ->
                        val animatedToken = token.copy(position = displayedPosition(token))
                        val coords = getTokenCoordinates(animatedToken, squareSizePx)

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = with(density) { coords.x.toDp() - 22.dp },
                                    y = with(density) { coords.y.toDp() - 22.dp }
                                )
                                .size(44.dp)
                                .clickable(
                                    enabled = !isAnimatingMove,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (!isAnimatingMove) onTokenClick(token)
                                }
                        )
                    }
                }

                val centerOffset = remember(squareSizePx) {
                    Offset(squareSizePx * 7.5f, squareSizePx * 7.5f)
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { centerOffset.x.toDp() - 34.dp },
                            y = with(density) { centerOffset.y.toDp() - 28.dp }
                        )
                        .size(width = 68.dp, height = 56.dp)
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
}
