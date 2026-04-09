package com.gibson.games.ludo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LudoSettingsScreen(
    onBackClicked: () -> Unit,
    gameRules: GameRules = GameRules(),
    onRulesChanged: (GameRules) -> Unit = {}
) {
    var currentGameRules by remember(gameRules) { mutableStateOf(gameRules) }

    fun saveAndGoBack() {
        onRulesChanged(currentGameRules)
        onBackClicked()
    }

    BackHandler {
        saveAndGoBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF374151),
                        Color(0xFF4B5563),
                        Color(0xFF6B7280)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { saveAndGoBack() }
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
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Gameplay Rules") {
                SettingsToggleItem(
                    title = "Require Six to Exit Base",
                    description = "A token can leave base only with an actual die value of 6. Total = 6 does not count.",
                    isChecked = currentGameRules.requiresSixToExitBase,
                    onCheckedChange = {
                        currentGameRules = currentGameRules.copy(
                            requiresSixToExitBase = it
                        )
                    }
                )

                SettingsToggleItem(
                    title = "Extra Turn on Double Six",
                    description = "A player gets another round only when Die 1 = 6 and Die 2 = 6 in the same roll.",
                    isChecked = currentGameRules.getsExtraTurnOnDoubleSix,
                    onCheckedChange = {
                        currentGameRules = currentGameRules.copy(
                            getsExtraTurnOnDoubleSix = it
                        )
                    }
                )

                SettingsToggleItem(
                    title = "Legacy Extra Turn on Six",
                    description = "Compatibility toggle kept in rules data. Current gameplay should use the Double Six rule above.",
                    isChecked = currentGameRules.getsExtraTurnOnSix,
                    onCheckedChange = {
                        currentGameRules = currentGameRules.copy(
                            getsExtraTurnOnSix = it
                        )
                    }
                )

                SettingsToggleItem(
                    title = "Three Sixes Forfeit Turn",
                    description = "Rolling three consecutive six-heavy turns can forfeit the turn.",
                    isChecked = currentGameRules.getsExtraTurnOnThreeSixesForfeit,
                    onCheckedChange = {
                        currentGameRules = currentGameRules.copy(
                            getsExtraTurnOnThreeSixesForfeit = it
                        )
                    }
                )

                SettingsToggleItem(
                    title = "Must Play Rolled Numbers",
                    description = "The player should use available dice values whenever a valid move exists.",
                    isChecked = currentGameRules.mustPlayRolledNumbers,
                    onCheckedChange = {
                        currentGameRules = currentGameRules.copy(
                            mustPlayRolledNumbers = it
                        )
                    }
                )

                SettingsChoiceSection(
                    title = "Captured Token Penalty",
                    description = "Choose what happens to the token that gets captured."
                ) {
                    SettingsChoiceItem(
                        title = "Return to Base",
                        description = "Captured token goes back to base.",
                        isSelected = currentGameRules.capturePenalty == CapturePenalty.RETURN_TO_BASE,
                        onClick = {
                            currentGameRules = currentGameRules.copy(
                                capturePenalty = CapturePenalty.RETURN_TO_BASE
                            )
                        }
                    )

                    SettingsChoiceItem(
                        title = "Move Back 5 Steps",
                        description = "Captured token moves backward by 5 steps instead of returning to base.",
                        isSelected = currentGameRules.capturePenalty == CapturePenalty.MOVE_BACK_5,
                        onClick = {
                            currentGameRules = currentGameRules.copy(
                                capturePenalty = CapturePenalty.MOVE_BACK_5
                            )
                        }
                    )
                }

                SettingsChoiceSection(
                    title = "Capturing Token Reward",
                    description = "Choose what the token that captures another one receives."
                ) {
                    SettingsChoiceItem(
                        title = "No Reward",
                        description = "Token stays where it captured and the game continues normally.",
                        isSelected = currentGameRules.captureReward == CaptureReward.NONE,
                        onClick = {
                            currentGameRules = currentGameRules.copy(
                                captureReward = CaptureReward.NONE
                            )
                        }
                    )

                    SettingsChoiceItem(
                        title = "Extra Turn",
                        description = "Capturing grants another dice roll.",
                        isSelected = currentGameRules.captureReward == CaptureReward.EXTRA_TURN,
                        onClick = {
                            currentGameRules = currentGameRules.copy(
                                captureReward = CaptureReward.EXTRA_TURN
                            )
                        }
                    )

                    SettingsChoiceItem(
                        title = "Go Home",
                        description = "The capturing token goes directly to the center as a bonus.",
                        isSelected = currentGameRules.captureReward == CaptureReward.GO_HOME,
                        onClick = {
                            currentGameRules = currentGameRules.copy(
                                captureReward = CaptureReward.GO_HOME
                            )
                        }
                    )
                }

                SettingsToggleItem(
                    title = "Starting Point Safe Zone (Own Color)",
                    description = "Only the owner of that starting point is safe on it.",
                    isChecked = currentGameRules.startingPointIsSafeZoneForColor,
                    onCheckedChange = { isChecked ->
                        currentGameRules = if (isChecked) {
                            currentGameRules.copy(
                                startingPointIsSafeZoneForColor = true,
                                startingPointIsSafeZoneForAll = false
                            )
                        } else {
                            currentGameRules.copy(
                                startingPointIsSafeZoneForColor = false
                            )
                        }
                    }
                )

                SettingsToggleItem(
                    title = "Starting Point Safe Zone (All Colors)",
                    description = "Any player is safe on any starting point.",
                    isChecked = currentGameRules.startingPointIsSafeZoneForAll,
                    onCheckedChange = { isChecked ->
                        currentGameRules = if (isChecked) {
                            currentGameRules.copy(
                                startingPointIsSafeZoneForAll = true,
                                startingPointIsSafeZoneForColor = false
                            )
                        } else {
                            currentGameRules.copy(
                                startingPointIsSafeZoneForAll = false
                            )
                        }
                    }
                )
            }

            SettingsSection(title = "Game Information") {
                InfoCard(
                    title = "How to Play",
                    description =
                        "• Roll two dice each turn\n" +
                            "• You may play Die 1, Die 2, or the Total when valid\n" +
                            "• A token leaves base only with an actual die value of 6\n" +
                            "• Total = 6 cannot bring a token out of base\n" +
                            "• If Double Six is enabled, 6 + 6 gives another round\n" +
                            "• Capture rules can be customized in settings\n" +
                            "• Get all 4 tokens to the center to win"
                )

                InfoCard(
                    title = "Bird Teams",
                    description =
                        "🦚 Green Peacock\n" +
                            "🦜 Red Parrot\n" +
                            "🐥 Yellow Chick\n" +
                            "🐦 Blue Bird"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { saveAndGoBack() },
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
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

@Composable
fun SettingsChoiceSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.78f),
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        content()
    }
}

@Composable
fun SettingsChoiceItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF10B981).copy(alpha = 0.22f)
            } else {
                Color.White.copy(alpha = 0.10f)
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSelected) "✓" else "○",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

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
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF10B981),
                    checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )
        }
    }
}

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
            containerColor = Color(0xFF3B82F6).copy(alpha = 0.20f)
        ),
        shape = RoundedCornerShape(10.dp)
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
                color = Color.White.copy(alpha = 0.90f),
                lineHeight = 20.sp
            )
        }
    }
}
