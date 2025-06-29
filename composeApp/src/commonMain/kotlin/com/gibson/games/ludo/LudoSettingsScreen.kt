package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.gibson.games.ludo.GameRules

/**
 * Settings screen for Ludo game
 */
@Composable
fun LudoSettingsScreen(onBackClicked: () -> Unit) {
    var gameRules by remember { mutableStateOf(GameRules()) }
    BackHandler {
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
                    onClick = onBackClicked
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Settings Sections
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gameplay Settings Section
                SettingsSection(title = "Gameplay") {
                    SettingsToggleItem(
                        title = "Require Six to Exit Base",
                        description = "A 6 is required to move a token out of the home base.",
                        isChecked = gameRules.requiresSixToExitBase,
                        onCheckedChange = { gameRules = gameRules.copy(requiresSixToExitBase = it) }
                    )
                    SettingsToggleItem(
                        title = "Extra Turn on Six",
                        description = "Player gets an extra turn when rolling a 6.",
                        isChecked = gameRules.getsExtraTurnOnSix,
                        onCheckedChange = { gameRules = gameRules.copy(getsExtraTurnOnSix = it) }
                    )
                    SettingsToggleItem(
                        title = "Three Sixes Forfeit Turn",
                        description = "Rolling three consecutive 6s forfeits the turn.",
                        isChecked = gameRules.getsExtraTurnOnThreeSixesForfeit,
                        onCheckedChange = { gameRules = gameRules.copy(getsExtraTurnOnThreeSixesForfeit = it) }
                    )
                    SettingsToggleItem(
                        title = "Must Play Rolled Numbers",
                        description = "Player must move a token for each number rolled.",
                        isChecked = gameRules.mustPlayRolledNumbers,
                        onCheckedChange = { gameRules = gameRules.copy(mustPlayRolledNumbers = it) }
                    )
                    SettingsToggleItem(
                        title = "Captured Token Returns to Base",
                        description = "A captured token returns to its home base.",
                        isChecked = gameRules.capturedTokenReturnsToBase,
                        onCheckedChange = { gameRules = gameRules.copy(capturedTokenReturnsToBase = it) }
                    )
                    SettingsToggleItem(
                        title = "Capture Gives Extra Turn",
                        description = "Capturing an opponent's token grants an extra turn.",
                        isChecked = gameRules.captureGivesExtraTurn,
                        onCheckedChange = { gameRules = gameRules.copy(captureGivesExtraTurn = it) }
                    )
                    SettingsToggleItem(
                        title = "Capture Sends to Home",
                        description = "Captured token goes directly to home (finished).",
                        isChecked = gameRules.captureSendsToHome,
                        onCheckedChange = { gameRules = gameRules.copy(captureSendsToHome = it) }
                    )
                    SettingsToggleItem(
                        title = "Starting Point is Safe Zone (Color Specific)",
                        description = "Each player's starting point is a safe zone only for their color.",
                        isChecked = gameRules.startingPointIsSafeZoneForColor,
                        onCheckedChange = { gameRules = gameRules.copy(startingPointIsSafeZoneForColor = it) }
                    )
                    SettingsToggleItem(
                        title = "Starting Point is Safe Zone (All Colors)",
                        description = "All starting points are safe zones for all colors.",
                        isChecked = gameRules.startingPointIsSafeZoneForAll,
                        onCheckedChange = { gameRules = gameRules.copy(startingPointIsSafeZoneForAll = it) }
                    )
                    SettingsItem(
                        title = "Game Speed",
                        description = "Normal",
                        onClick = { /* TODO: Implement speed selection */ }
                    )
                    
                    SettingsItem(
                        title = "Difficulty",
                        description = "Medium",
                        onClick = { /* TODO: Implement difficulty selection */ }
                    )
                }

                // Sound Settings Section
                SettingsSection(title = "Sound") {
                    SettingsToggleItem(
                        title = "Sound Effects",
                        description = "Enable or disable in-game sound effects.",
                        isChecked = true, // TODO: Implement actual sound setting
                        onCheckedChange = { /* TODO: Implement sound setting */ }
                    )
                    SettingsToggleItem(
                        title = "Background Music",
                        description = "Enable or disable background music.",
                        isChecked = false, // TODO: Implement actual music setting
                        onCheckedChange = { /* TODO: Implement music setting */ }
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
            modifier = Modifier.padding(bottom = 8.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                color = Color.Gray
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * Settings item without toggle (for selection items)
 */
@Composable
fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                color = Color(0xFF60A5FA)
            )
        }
        
        TextButton(onClick = onClick) {
            Text(
                text = "Change",
                color = Color(0xFF60A5FA)
            )
        }
    }
}

