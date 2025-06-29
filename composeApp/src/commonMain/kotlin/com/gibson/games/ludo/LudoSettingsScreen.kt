package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler

/**
 * Settings screen for Ludo game with proper game rules handling
 */
@Composable
fun LudoSettingsScreen(
    onBackClicked: () -> Unit,
    gameRules: GameRules = GameRules(),
    onRulesChanged: (GameRules) -> Unit = {}
) {
    var currentGameRules by remember { mutableStateOf(gameRules) }
    
    BackHandler {
        onRulesChanged(currentGameRules)
        onBackClicked()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF374151), // Dark gray
                        Color(0xFF4B5563), // Gray
                        Color(0xFF6B7280)  // Light gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onRulesChanged(currentGameRules)
                        onBackClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Game Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Settings Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Gameplay Settings Section
                SettingsSection(title = "Gameplay Rules") {
                    SettingsToggleItem(
                        title = "Require Six to Exit Base",
                        description = "A 6 is required to move a token out of the home base.",
                        isChecked = currentGameRules.requiresSixToExitBase,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(requiresSixToExitBase = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Extra Turn on Six",
                        description = "Player gets an extra turn when rolling a 6.",
                        isChecked = currentGameRules.getsExtraTurnOnSix,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(getsExtraTurnOnSix = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Three Sixes Forfeit Turn",
                        description = "Rolling three consecutive 6s forfeits the turn.",
                        isChecked = currentGameRules.getsExtraTurnOnThreeSixesForfeit,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(getsExtraTurnOnThreeSixesForfeit = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Must Play Rolled Numbers",
                        description = "Player must move a token for each number rolled.",
                        isChecked = currentGameRules.mustPlayRolledNumbers,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(mustPlayRolledNumbers = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Captured Token Returns to Base",
                        description = "A captured token returns to its home base.",
                        isChecked = currentGameRules.capturedTokenReturnsToBase,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(capturedTokenReturnsToBase = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Capture Gives Extra Turn",
                        description = "Capturing an opponent's token grants an extra turn.",
                        isChecked = currentGameRules.captureGivesExtraTurn,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(captureGivesExtraTurn = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Starting Point Safe Zone (Own Color)",
                        description = "Each player's starting point is a safe zone only for their color.",
                        isChecked = currentGameRules.startingPointIsSafeZoneForColor,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(startingPointIsSafeZoneForColor = it) 
                        }
                    )
                    SettingsToggleItem(
                        title = "Starting Point Safe Zone (All Colors)",
                        description = "All starting points are safe zones for all colors.",
                        isChecked = currentGameRules.startingPointIsSafeZoneForAll,
                        onCheckedChange = { 
                            currentGameRules = currentGameRules.copy(startingPointIsSafeZoneForAll = it) 
                        }
                    )
                }

                // Game Info Section
                SettingsSection(title = "Game Information") {
                    InfoCard(
                        title = "How to Play",
                        description = "• Roll dice to move your bird tokens\n• Get all 4 tokens to the center to win\n• Capture opponents by landing on them\n• Use safe zones to protect your tokens"
                    )
                    InfoCard(
                        title = "Bird Teams",
                        description = "🦚 Green Peacock\n🦜 Red Parrot\n🐥 Yellow Chick\n🐦 Blue Bird"
                    )
                }

                // Save Button
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        onRulesChanged(currentGameRules)
                        onBackClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Composable for a settings section header.
 */
@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

/**
 * Settings item with a toggle switch.
 */
@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF10B981),
                    checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * Information card for game rules and instructions
 */
@Composable
fun InfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}

