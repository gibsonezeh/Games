package com.gibson.games.platform.input

import androidx.compose.ui.input.pointer.PointerInputScope
import com.gibson.games.core.SwipeDirection

expect interface SwipeDetector {
    fun detectSwipe(pointerInputScope: PointerInputScope, onSwipe: (SwipeDirection) -> Unit)
}
