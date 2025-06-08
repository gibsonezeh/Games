package com.gibson.games

import androidx.compose.ui.input.pointer.PointerInputScope
import com.gibson.games.core.SwipeDirection

interface SwipeDetector {
    fun detectSwipe(pointerInputScope: PointerInputScope, onSwipe: (SwipeDirection) -> Unit)
}