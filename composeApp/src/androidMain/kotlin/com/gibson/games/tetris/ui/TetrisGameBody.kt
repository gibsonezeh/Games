package com.gibson.games.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gibson.games.tetris.engine.Direction
import com.gibson.games.tetris.ui.theme.TetrisBody
import com.gibson.games.tetris.ui.theme.TetrisScreenBackground

@Composable
fun TetrisGameBody(
    controls: TetrisControls = tetrisControls(),
    screen: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(TetrisBody, RoundedCornerShape(10.dp))
            .padding(top = 20.dp)
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally)) {

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(330.dp, 400.dp)
                    .padding(top = 20.dp)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(5.dp)
                    .background(TetrisBody)
            )

            Box(
                Modifier
                    .width(120.dp)
                    .height(45.dp)
                    .align(Alignment.TopCenter)
                    .background(TetrisBody)
            ) {
                Text(
                    text = "TETRIS",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(360.dp, 380.dp)
                    .padding(start = 50.dp, end = 50.dp, top = 50.dp, bottom = 30.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawScreenBorder(
                        topLeft = Offset(0f, 0f),
                        topRight = Offset(size.width, 0f),
                        bottomLeft = Offset(0f, size.height),
                        bottomRight = Offset(size.width, size.height)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(TetrisScreenBackground)
                ) {
                    screen()
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(start = 40.dp, end = 40.dp)
        ) {
            Row {
                SettingText("SOUNDS", Modifier.weight(1f))
                SettingText("PAUSE/RESUME", Modifier.weight(1f))
                SettingText("START/RESET", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row {
                GameButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp),
                    onClick = controls.onMute,
                    size = SettingButtonSize
                ) {}

                GameButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp),
                    onClick = controls.onPause,
                    size = SettingButtonSize
                ) {}

                GameButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp),
                    onClick = controls.onRestart,
                    size = SettingButtonSize
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .height(160.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                GameButton(
                    Modifier.align(Alignment.TopCenter),
                    onClick = controls.onRotate,
                    autoInvokeWhenPressed = false,
                    size = DirectionButtonSize
                ) {
                    ButtonText(it, "▲")
                }

                GameButton(
                    Modifier.align(Alignment.CenterStart),
                    onClick = { controls.onMove(Direction.Left) },
                    autoInvokeWhenPressed = true,
                    size = DirectionButtonSize
                ) {
                    ButtonText(it, "◀")
                }

                GameButton(
                    Modifier.align(Alignment.CenterEnd),
                    onClick = { controls.onMove(Direction.Right) },
                    autoInvokeWhenPressed = true,
                    size = DirectionButtonSize
                ) {
                    ButtonText(it, "▶")
                }

                GameButton(
                    Modifier.align(Alignment.BottomCenter),
                    onClick = { controls.onMove(Direction.Down) },
                    autoInvokeWhenPressed = true,
                    size = DirectionButtonSize
                ) {
                    ButtonText(it, "▼")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                GameButton(
                    Modifier.align(Alignment.CenterEnd),
                    onClick = controls.onDrop,
                    autoInvokeWhenPressed = false,
                    size = RotateButtonSize
                ) {
                    ButtonText(it, "DROP")
                }
            }
        }
    }
}

@Composable
private fun SettingText(
    text: String,
    modifier: Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.9f),
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ButtonText(
    modifier: Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 18.sp
    )
}

fun DrawScope.drawScreenBorder(
    topLeft: Offset,
    topRight: Offset,
    bottomLeft: Offset,
    bottomRight: Offset
) {
    var path = Path().apply {
        moveTo(topLeft.x, topLeft.y)
        lineTo(topRight.x, topRight.y)
        lineTo(
            topRight.x / 2 + topLeft.x / 2,
            topLeft.y + topRight.x / 2 + topLeft.x / 2
        )
        lineTo(
            topRight.x / 2 + topLeft.x / 2,
            bottomLeft.y - topRight.x / 2 + topLeft.x / 2
        )
        lineTo(bottomLeft.x, bottomLeft.y)
        close()
    }

    drawPath(path, Color.Black.copy(alpha = 0.5f))

    path = Path().apply {
        moveTo(bottomRight.x, bottomRight.y)
        lineTo(bottomLeft.x, bottomLeft.y)
        lineTo(
            topRight.x / 2 + topLeft.x / 2,
            bottomLeft.y - topRight.x / 2 + topLeft.x / 2
        )
        lineTo(
            topRight.x / 2 + topLeft.x / 2,
            topLeft.y + topRight.x / 2 + topLeft.x / 2
        )
        lineTo(topRight.x, topRight.y)
        close()
    }

    drawPath(path, Color.White.copy(alpha = 0.5f))
}

data class TetrisControls(
    val onMove: (Direction) -> Unit,
    val onRotate: () -> Unit,
    val onDrop: () -> Unit,
    val onRestart: () -> Unit,
    val onPause: () -> Unit,
    val onMute: () -> Unit
)

fun tetrisControls(
    onMove: (Direction) -> Unit = {},
    onRotate: () -> Unit = {},
    onDrop: () -> Unit = {},
    onRestart: () -> Unit = {},
    onPause: () -> Unit = {},
    onMute: () -> Unit = {}
): TetrisControls {
    return TetrisControls(
        onMove = onMove,
        onRotate = onRotate,
        onDrop = onDrop,
        onRestart = onRestart,
        onPause = onPause,
        onMute = onMute
    )
}

val DirectionButtonSize = 60.dp
val RotateButtonSize = 90.dp
val SettingButtonSize = 15.dp
