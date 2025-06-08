package com.gibson.games



import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.PointerEventType
import com.gibson.games.core.SwipeDirection
import kotlin.math.abs

class AndroidSwipeDetector() : SwipeDetector {

    override fun detectSwipe(pointerInputScope: PointerInputScope, onSwipe: (SwipeDirection) -> Unit) {
        with(pointerInputScope) {
            var startX = 0f
            var startY = 0f

            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull()

                    if (change != null) {
                        when (event.type) {
                            PointerEventType.Press -> {
                                startX = change.position.x
                                startY = change.position.y
                            }

                            PointerEventType.Release -> {
                                if (change.changedToUp()) {
                                    val endX = change.position.x
                                    val endY = change.position.y

                                    val diffX = endX - startX
                                    val diffY = endY - startY

                                    if (abs(diffX) > abs(diffY)) {
                                        if (abs(diffX) > SWIPE_THRESHOLD) {
                                            if (diffX > 0) {
                                                onSwipe(SwipeDirection.RIGHT)
                                            } else {
                                                onSwipe(SwipeDirection.LEFT)
                                            }
                                        }
                                    } else {
                                        if (abs(diffY) > SWIPE_THRESHOLD) {
                                            if (diffY > 0) {
                                                onSwipe(SwipeDirection.DOWN)
                                            } else {
                                                onSwipe(SwipeDirection.UP)
                                            }
                                        }
                                    }
                                    change.consumePositionChange()
                                }
                            }

                            else -> { /* Do nothing for other event types */
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SWIPE_THRESHOLD = 50f
    }
}

 fun getSwipeDetector(): SwipeDetector = AndroidSwipeDetector()
