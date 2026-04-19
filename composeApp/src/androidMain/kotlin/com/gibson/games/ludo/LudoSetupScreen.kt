package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LudoSetupConfig(
    val mode: LudoMode,
    val playerCount: Int,
    val playerNames: List<String>
)

@Composable
fun LudoSetupScreen(
    mode: LudoMode,
    onBackClicked: () -> Unit,
    onStartGame: (LudoSetupConfig) -> Unit
) {
    val names = remember {
        mutableStateListOf("Player 1", "Player 2", "Player 3", "Player 4")
    }

    var playerCount by remember {
        mutableIntStateOf(
            when (mode) {
                LudoMode.QUICK_PLAY -> 1
                LudoMode.PLAY_VS_AI -> 2
                else -> 2
            }
        )
    }

    val title = when (mode) {
        LudoMode.QUICK_PLAY -> "Quick Play Setup"
        LudoMode.PLAY_VS_AI -> "Play vs AI Setup"
        LudoMode.PASS_AND_PLAY -> "Pass & Play Setup"
        LudoMode.BLUETOOTH -> "Bluetooth Setup"
        LudoMode.WIFI -> "Wi-Fi Setup"
        LudoMode.ONLINE -> "Online Setup"
    }

    val subtitle = when (mode) {
        LudoMode.QUICK_PLAY -> "Start instantly or rename your player"
        LudoMode.PLAY_VS_AI -> "Set your player name before the match"
        LudoMode.PASS_AND_PLAY -> "Choose player count and names"
        LudoMode.BLUETOOTH -> "Prepare player info before connection"
        LudoMode.WIFI -> "Prepare player info before network play"
        LudoMode.ONLINE -> "Prepare player info before online play"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4FA))
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (mode == LudoMode.PASS_AND_PLAY ||
                mode == LudoMode.BLUETOOTH ||
                mode == LudoMode.WIFI ||
                mode == LudoMode.ONLINE
            ) {
                item {
                    SectionTitle("Player Count")

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountCard(
                            text = "2 Players",
                            selected = playerCount == 2,
                            onClick = { playerCount = 2 }
                        )

                        CountCard(
                            text = "4 Players",
                            selected = playerCount == 4,
                            onClick = { playerCount = 4 }
                        )
                    }
                }
            }

            item {
                SectionTitle("Players")
            }

            items(playerCount) { index ->
                val label = when {
                    mode == LudoMode.PLAY_VS_AI && index == 1 -> "AI Name"
                    else -> "Player ${index + 1} Name"
                }

                OutlinedTextField(
                    value = names[index],
                    onValueChange = { names[index] = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val finalNames = names
                            .take(playerCount)
                            .mapIndexed { index, name ->
                                val trimmed = name.trim()
                                when {
                                    trimmed.isNotEmpty() -> trimmed
                                    mode == LudoMode.PLAY_VS_AI && index == 1 -> "AI"
                                    else -> "Player ${index + 1}"
                                }
                            }

                        onStartGame(
                            LudoSetupConfig(
                                mode = mode,
                                playerCount = playerCount,
                                playerNames = finalNames
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Start Game",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
private fun CountCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF10B981) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}
