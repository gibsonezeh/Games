package com.gibson.games

import android.content.Context
import android.media.MediaPlayer
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    @JavascriptInterface
    fun playSound(sound: String) {
        val resId = context.resources.getIdentifier(sound, "raw", context.packageName)
        if (resId != 0) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.start()
        } else {
            Toast.makeText(context, "Sound not found: $sound", Toast.LENGTH_SHORT).show()
        }
    }
}
