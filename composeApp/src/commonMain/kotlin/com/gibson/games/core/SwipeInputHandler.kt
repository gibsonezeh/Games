package com.gibson.games.core

object SwipeInputHandler {

    private var swipeListener: ((SwipeDirection) -> Unit)? = null

    fun registerListener(listener: (SwipeDirection) -> Unit) {
        swipeListener = listener
    }

    fun unregisterListener() {
        swipeListener = null
    }

    fun handleSwipe(direction: SwipeDirection) {
        swipeListener?.invoke(direction)
    }
}
