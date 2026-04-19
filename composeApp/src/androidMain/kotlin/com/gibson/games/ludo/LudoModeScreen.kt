package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LudoMode {
    QUICK_PLAY,
    PLAY_VS_AI,
    PASS_AND_PLAY,
    BLUETOOTH,
    WIFI,
    ONLINE
}

@Composable
fun LudoModeScreen(
    onBackClicked: () -> Unit,
    onModeSelected: (LudoMode) -> Unit
) {
    val modes = listOf(
        LudoModeUi(
            mode = LudoMode.QUICK_PLAY,
            title = "Quick Play",
            subtitle = "1 Player",
            color = Color(0xFF3B82F6)
        ),
        LudoModeUi(
            mode = LudoMode.PLAY_VS_AI,
            title = "Play vs AI",
            subtitle = "2 Players",
            color = Color(0xFFEF4444)
        ),
        LudoModeUi(
            mode = LudoMode.PASS_AND_PLAY,
            title = "Pass & Play",
            subtitle = "2–4 Players",
            color = Color(0xFFA855F7)
        ),
        LudoModeUi(
            mode = LudoMode.BLUETOOTH,
            title = "Bluetooth",
            subtitle = "Nearby multiplayer",
            color = Color(0xFF06B6D4)
        ),
        LudoModeUi(
            mode = LudoMode.WIFI,
            title = "Wi-Fi",
            subtitle = "Same network play",
            color = Color(0xFF10B981)
        ),
        LudoModeUi(
            mode = LudoMode.ONLINE,
            title = "Online",
            subtitle = "Play from anywhere",
            color = Color(0xFFF59E0B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B4FAF),
                        Color(0xFF1D4ED8),
                        Color(0xFF2563EB)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎲",
                            fontSize = 40.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Ludo",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Choose how to play",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(modes.size) { index ->
                val item = modes[index]
                ModeActionCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    color = item.color,
                    onClick = { onModeSelected(item.mode) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onBackClicked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = "Back",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private data class LudoModeUi(
    val mode: LudoMode,
    val title: String,
    val subtitle: String,
    val color: Color
)

@Composable
private fun ModeActionCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
