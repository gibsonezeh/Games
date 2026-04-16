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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toDp
import kotlin.math.min

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
                    val square = boardSize / 15f

                    val boardColor = Color(0xFFF5F5F0)
                    val pathColor = Color(0xFFBDBDBD)

                    val green = Color(0xFF2ECC71)
                    val red = Color(0xFFE74C3C)
                    val yellow = Color(0xFFF1C40F)
                    val blue = Color(0xFF3498DB)
                    val black = Color.Black
                    val white = Color.White

                    drawRoundRect(
                        color = boardColor,
                        size = Size(boardSize, boardSize),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f)
                    )

                    fun drawDot(x: Int, y: Int, color: Color, radiusFactor: Float = 0.28f) {
                        drawCircle(
                            color = color,
                            radius = square * radiusFactor,
                            center = Offset(
                                (x + 0.5f) * square,
                                (y + 0.5f) * square
                            )
                        )
                    }

                    fun drawStarDot(x: Int, y: Int, color: Color) {
                        drawDot(x, y, color, 0.32f)
                        drawStar(
                            center = Offset((x + 0.5f) * square, (y + 0.5f) * square),
                            radius = square * 0.16f,
                            color = white
                        )
                    }

                    fun drawHomeSlots(startX: Float, startY: Float, color: Color) {
                        val spacing = square * 2f
                        for (i in 0..1) {
                            for (j in 0..1) {
                                val cx = startX + square * 1.5f + i * spacing
                                val cy = startY + square * 1.5f + j * spacing
                                drawCircle(
                                    color = color.copy(alpha = 0.22f),
                                    radius = square * 0.55f,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = color.copy(alpha = 0.75f),
                                    radius = square * 0.34f,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = square * 0.05f)
                                )
                            }
                        }
                    }

                    // Main neutral path
                    for (i in 0 until 6) {
                        drawDot(6, i, pathColor)
                        drawDot(7, i, pathColor)
                        drawDot(8, i, pathColor)

                        drawDot(6, i + 9, pathColor)
                        drawDot(7, i + 9, pathColor)
                        drawDot(8, i + 9, pathColor)
                    }

                    for (i in 0 until 6) {
                        drawDot(i, 6, pathColor)
                        drawDot(i, 7, pathColor)
                        drawDot(i, 8, pathColor)

                        drawDot(i + 9, 6, pathColor)
                        drawDot(i + 9, 7, pathColor)
                        drawDot(i + 9, 8, pathColor)
                    }

                    // Colored home lanes
                    for (i in 1..5) {
                        drawDot(7, i, red)
                        drawDot(7, i + 8, yellow)
                        drawDot(i, 7, green)
                        drawDot(i + 8, 7, blue)
                    }

                    // Safe zones / starting points
                    drawStarDot(1, 6, green)
                    drawStarDot(8, 1, red)
                    drawStarDot(13, 8, blue)
                    drawStarDot(6, 13, yellow)

                    // Opposite-side safe zones
                    drawStarDot(2, 8, green)
                    drawStarDot(6, 2, red)
                    drawStarDot(12, 6, blue)
                    drawStarDot(8, 12, yellow)

                    // Soft home areas
                    drawRoundRect(
                        color = green.copy(alpha = 0.18f),
                        topLeft = Offset(0f, 0f),
                        size = Size(square * 6, square * 6),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
                    )
                    drawRoundRect(
                        color = red.copy(alpha = 0.18f),
                        topLeft = Offset(square * 9, 0f),
                        size = Size(square * 6, square * 6),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
                    )
                    drawRoundRect(
                        color = yellow.copy(alpha = 0.18f),
                        topLeft = Offset(0f, square * 9),
                        size = Size(square * 6, square * 6),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
                    )
                    drawRoundRect(
                        color = blue.copy(alpha = 0.18f),
                        topLeft = Offset(square * 9, square * 9),
                        size = Size(square * 6, square * 6),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
                    )

                    // Home circles
                    drawHomeSlots(0f, 0f, green)
                    drawHomeSlots(square * 9, 0f, red)
                    drawHomeSlots(0f, square * 9, yellow)
                    drawHomeSlots(square * 9, square * 9, blue)

                    // Center plate
                    drawRoundRect(
                        color = white,
                        topLeft = Offset(square * 6, square * 6),
                        size = Size(square * 3, square * 3),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                    )
                    drawRoundRect(
                        color = Color(0xFFBDBDBD),
                        topLeft = Offset(square * 6, square * 6),
                        size = Size(square * 3, square * 3),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                        style = Stroke(width = square * 0.06f)
                    )

                    // Tokens
                    val allTokens = boardState.players.flatMap { it.tokens }
                    val groupedTokens = allTokens.groupBy { displayedPosition(it) }

                    fun drawToken(
                        centerX: Float,
                        centerY: Float,
                        color: Color,
                        isSelected: Boolean = false,
                        isMovable: Boolean = false
                    ) {
                        val tokenRadius = square * 0.35f

                        if (isMovable) {
                            drawCircle(
                                color = Color.Yellow.copy(alpha = 0.45f),
                                radius = tokenRadius * 1.3f,
                                center = Offset(centerX, centerY)
                            )
                        }

                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = tokenRadius * 1.15f,
                                center = Offset(centerX, centerY)
                            )
                        }

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
                            color = white.copy(alpha = 0.55f),
                            radius = tokenRadius * 0.28f,
                            center = Offset(
                                centerX - tokenRadius * 0.28f,
                                centerY - tokenRadius * 0.28f
                            )
                        )
                    }

                    fun drawStackedToken(
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
                                color = Color.Yellow.copy(alpha = 0.45f),
                                radius = tokenRadius * 1.3f,
                                center = center
                            )
                        }

                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = tokenRadius * 1.15f,
                                center = center
                            )
                        }

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

                        val badgeDistance = tokenRadius * 0.62f
                        val badgeRadius = tokenRadius * 0.24f
                        startAngle = -90f + sweep / 2f

                        orderedGroups.forEach { playerColor ->
                            val count = colorGroups[playerColor]?.size ?: 0
                            if (count > 1) {
                                val angleRad = Math.toRadians(startAngle.toDouble())
                                val badgeCenter = Offset(
                                    x = center.x + badgeDistance * kotlin.math.cos(angleRad).toFloat(),
                                    y = center.y + badgeDistance * kotlin.math.sin(angleRad).toFloat()
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
                            }
                            startAngle += sweep
                        }
                    }

                    groupedTokens.forEach { (position, tokensAtPosition) ->
                        val shouldUseStackedSafeZoneView =
                            position in 0..51 &&
                                tokensAtPosition.size > 1 &&
                                isStackableSafeZonePosition(position, gameRules)

                        if (!shouldUseStackedSafeZoneView) {
                            tokensAtPosition.forEach { token ->
                                val drawnToken = token.copy(position = displayedPosition(token))
                                val tokenCoords = getTokenCoordinates(drawnToken, square)
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
                            val center = getTokenCoordinates(drawnFirst, square)

                            drawStackedToken(
                                center = center,
                                squareSize = square,
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
                            x = with(density) { centerOffset.x.toDp() - 36.dp },
                            y = with(density) { centerOffset.y.toDp() - 16.dp }
                        )
                        .size(width = 72.dp, height = 32.dp)
                ) {
                    CenterDiceRoller(
                        die1Value = die1Display,
                        die2Value = die2Display,
                        animationState = centerDiceState,
                        isRolling = isRolling,
                        onClick = onCenterDiceClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
