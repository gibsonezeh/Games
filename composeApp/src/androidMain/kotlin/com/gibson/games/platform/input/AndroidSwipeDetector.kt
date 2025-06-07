
package com.gibson.games.platform.input

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import com.gibson.games.core.SwipeDirection

class AndroidSwipeDetector(
    private val onSwipe: (SwipeDirection) -> Unit
) : View.OnTouchListener {

    private var downX = 0f
    private var downY = 0f

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val upX = event.x
                val upY = event.y

                val deltaX = upX - downX
                val deltaY = upY - downY

                if (abs(deltaX) > abs(deltaY)) {
                    // Horizontal swipe
                    if (deltaX > 100) {
                        onSwipe(SwipeDirection.RIGHT)
                    } else if (deltaX < -100) {
                        onSwipe(SwipeDirection.LEFT)
                    }
                } else {
                    // Vertical swipe
                    if (deltaY > 100) {
                        onSwipe(SwipeDirection.DOWN)
                    } else if (deltaY < -100) {
                        onSwipe(SwipeDirection.UP)
                    }
                }
                return true
            }
        }
        return false
    }
}
