package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val soloModes = listOf(
        Triple(LudoMode.QUICK_PLAY, "Quick Play", "1 Player"),
        Triple(LudoMode.PLAY_VS_AI, "Play vs AI", "2 Players")
    )

    val multiplayerModes = listOf(
        Triple(LudoMode.PASS_AND_PLAY, "Pass & Play", "2–4 Players"),
        Triple(LudoMode.BLUETOOTH, "Bluetooth", "2–4 Players"),
        Triple(LudoMode.WIFI, "Wi-Fi", "2–4 Players"),
        Triple(LudoMode.ONLINE, "Online", "2–4 Players")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4FA))
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Ludo Modes",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose how you want to play",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionTitle("Solo")
            }

            items(soloModes.size) { index ->
                val mode = soloModes[index]
                ModeCard(
                    title = mode.second,
                    subtitle = mode.third,
                    onClick = { onModeSelected(mode.first) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionTitle("Multiplayer")
            }

            items(multiplayerModes.size) { index ->
                val mode = multiplayerModes[index]
                ModeCard(
                    title = mode.second,
                    subtitle = mode.third,
                    onClick = { onModeSelected(mode.first) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onBackClicked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B7280)
                    )
                ) {
                    Text(
                        text = "Back",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}
