package com.gibson.games;

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class SoundType {
    DICE_ROLL,
    PIECE_MOVE,
    PIECE_CAPTURE,
    PIECE_HOME, // Piece reaches its final home spot
    GAME_WIN,
    BACKGROUND_MUSIC
}

class SoundManager(private val context: Context) {

    private var backgroundMusicPlayer: MediaPlayer? = null
    private val soundPlayers = mutableMapOf<SoundType, MediaPlayer?>()
    private val soundResIds = mapOf(
        SoundType.DICE_ROLL to R.raw.dice_roll, // Assuming you have these in res/raw
        SoundType.PIECE_MOVE to R.raw.piece_move,
        SoundType.PIECE_CAPTURE to R.raw.piece_capture,
        SoundType.PIECE_HOME to R.raw.piece_home,
        SoundType.GAME_WIN to R.raw.game_win,
        SoundType.BACKGROUND_MUSIC to R.raw.background_music 
    )

    fun loadSounds() {
        // Pre-load short sound effects for responsiveness if needed, or create on demand.
        // MediaPlayer creation can be a bit heavy to do repeatedly for very short sounds.
        // For simplicity here, we create on demand, but for high-frequency sounds, SoundPool is better.
    }

    fun playSound(soundType: SoundType) {
        if (soundType == SoundType.BACKGROUND_MUSIC) {
            playBackgroundMusic()
            return
        }
        
        soundResIds[soundType]?.let { resId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val mp = MediaPlayer.create(context, resId)
                    mp?.setOnCompletionListener { it.release() } // Release when done
                    mp?.start()
                } catch (e: Exception) {
                    // Log error, e.g., file not found, decoding error
                    Log.e("SoundManager", "Error playing sound $soundType: ${e.message}")
                }
            }
        }
    }

    fun playBackgroundMusic() {
        if (backgroundMusicPlayer == null) {
            soundResIds[SoundType.BACKGROUND_MUSIC]?.let {
                backgroundMusicPlayer = MediaPlayer.create(context, it)
                backgroundMusicPlayer?.isLooping = true
                backgroundMusicPlayer?.setVolume(0.5f, 0.5f) // Adjust volume as needed
            }
        }
        try {
            if (backgroundMusicPlayer?.isPlaying == false) {
                backgroundMusicPlayer?.start()
            }
        } catch (e: IllegalStateException) {
            Log.e("SoundManager", "Error starting background music: ${e.message}")
            // Attempt to reset and restart
            try {
                 backgroundMusicPlayer?.reset()
                 soundResIds[SoundType.BACKGROUND_MUSIC]?.let {
                    backgroundMusicPlayer?.setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$it"))
                    backgroundMusicPlayer?.prepare()
                    backgroundMusicPlayer?.start()
                 }
            } catch (e2: Exception) {
                Log.e("SoundManager", "Error resetting/restarting background music: ${e2.message}")
            }
        }
    }

    fun pauseBackgroundMusic() {
        if (backgroundMusicPlayer?.isPlaying == true) {
            backgroundMusicPlayer?.pause()
        }
    }

    fun stopBackgroundMusic() {
        if (backgroundMusicPlayer?.isPlaying == true) {
            backgroundMusicPlayer?.stop()
        }
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }

    fun release() {
        soundPlayers.values.forEach { it?.release() }
        soundPlayers.clear()
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }
}

// Placeholder for R.raw.x - In a real Android project, these would be generated.
// You need to add actual sound files to your project under res/raw directory.
object R {
    object raw {
        const val dice_roll = 0 // Replace with actual resource ID
        const val piece_move = 0
        const val piece_capture = 0
        const val piece_home = 0
        const val game_win = 0
        const val background_music = 0
    }
}

// Basic Logcat-like logging for use in files where Android Log is not directly available
// In an Android ViewModel or Activity, you"d use android.util.Log
object Log {
    fun e(tag: String, message: String) {
        println("ERROR/$tag: $message")
    }
     fun d(tag: String, message: String) {
        println("DEBUG/$tag: $message")
    }
}

