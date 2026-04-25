package com.gibson.games.tetris.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.gibson.games.R

enum class TetrisSound {
    Clean,
    Drop,
    Move,
    Rotate,
    Start
}

class TetrisSoundManager(
    context: Context
) {
    private val soundPool: SoundPool
    private val soundMap: Map<TetrisSound, Int>

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMap = mapOf(
            TetrisSound.Clean to soundPool.load(context, R.raw.clean, 1),
            TetrisSound.Drop to soundPool.load(context, R.raw.drop, 1),
            TetrisSound.Move to soundPool.load(context, R.raw.move, 1),
            TetrisSound.Rotate to soundPool.load(context, R.raw.rotate, 1),
            TetrisSound.Start to soundPool.load(context, R.raw.start, 1)
        )
    }

    fun play(
        sound: TetrisSound,
        isMute: Boolean
    ) {
        if (isMute) return

        val soundId = soundMap[sound] ?: return

        soundPool.play(
            soundId,
            1f,
            1f,
            1,
            0,
            1f
        )
    }

    fun release() {
        soundPool.release()
    }
}
