package com.gibson.games.ludo

import android.content.Context
import com.gibson.games.R
import android.media.SoundPool

object SoundManager {

    private var soundPool: SoundPool? = null

    private var rollSound = 0
    private var moveSound = 0
    private var captureSound = 0
    private var winSound = 0
    private var extraTurnSound = 0

    fun init(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        // 🔁 Put your sound files in res/raw/
        rollSound = soundPool!!.load(context, R.raw.dice_roll, 1)
        moveSound = soundPool!!.load(context, R.raw.token_move, 1)
        captureSound = soundPool!!.load(context, R.raw.capture, 1)
        winSound = soundPool!!.load(context, R.raw.win, 1)
        extraTurnSound = soundPool!!.load(context, R.raw.extra_turn, 1)
    }

    fun playRoll() {
        soundPool?.play(rollSound, 1f, 1f, 1, 0, 1f)
    }

    fun playMove() {
        soundPool?.play(moveSound, 1f, 1f, 1, 0, 1f)
    }

    fun playCapture() {
        soundPool?.play(captureSound, 1f, 1f, 1, 0, 1f)
    }

    fun playWin() {
        soundPool?.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playExtraTurn() {
        soundPool?.play(extraTurnSound, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
