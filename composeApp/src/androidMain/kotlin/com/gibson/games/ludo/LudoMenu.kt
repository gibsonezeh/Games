package com.gibson.games.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun LudoMainMenuScreen(
    onPlayClicked: () -> Unit,
    onExitClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF2563EB),
                        Color(0xFF60A5FA)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { showDropdownMenu = !showDropdownMenu }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }

            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        showDropdownMenu = false
                        onSettingsClicked()
                    }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    onClick = {
                        showDropdownMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Help") },
                    onClick = {
                        showDropdownMenu = false
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .widthIn(max = 420.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LUDO",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Bird Edition",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Classic board gameplay with colorful teams and custom rules.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 18.dp, bottom = 40.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                LudoMenuButton(
                    text = "PLAY",
                    backgroundColor = Color(0xFF10B981),
                    onClick = onPlayClicked
                )

                LudoMenuButton(
                    text = "SETTINGS",
                    backgroundColor = Color(0xFF3B82F6),
                    onClick = onSettingsClicked
                )

                LudoMenuButton(
                    text = "EXIT",
                    backgroundColor = Color(0xFFEF4444),
                    onClick = onExitClicked
                )
            }
        }

        Text(
            text = "Version 1.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun LudoMenuButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .widthIn(min = 220.dp, max = 320.dp)
            .padding(horizontal = 8.dp)
            .then(Modifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
