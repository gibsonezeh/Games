package com.gibson.games.dreamtreats

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameTile(candy: Candy, isSelected: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Card(
        modifier = modifier
            .padding(2.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.Yellow else Color.Gray
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .graphicsLayer(scaleX = scale, scaleY = scale), // Apply scaling for selection animation
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = candy.type,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
