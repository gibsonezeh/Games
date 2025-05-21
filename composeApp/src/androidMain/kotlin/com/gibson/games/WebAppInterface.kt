package com.gibson.games;

import android.content.Context;
import android.media.MediaPlayer;
import android.webkit.JavascriptInterface;

/**
 * WebAppInterface provides a bridge between JavaScript in the WebView and native Android code
 * for handling sound effects in the Ludo game.
 */
public class WebAppInterface {
    private Context context;
    private MediaPlayer diceRollPlayer;
    private MediaPlayer pieceMovePlayer;
    private MediaPlayer pieceCapturePlayer;
    private MediaPlayer pieceHomePlayer;
    private MediaPlayer gameWinPlayer;
    private MediaPlayer backgroundMusicPlayer;
    private boolean soundEnabled = true;

    /**
     * Constructor for WebAppInterface
     * @param context The Android context from the activity
     */
    public WebAppInterface(Context context) {
        this.context = context;
        initSoundPlayers();
    }

    /**
     * Initialize all MediaPlayer instances for the different sound effects
     */
    private void initSoundPlayers() {
        // Initialize MediaPlayer instances for each sound
        // Note: Replace R.raw.* with your actual resource IDs
        diceRollPlayer = MediaPlayer.create(context, R.raw.dice_roll);
        pieceMovePlayer = MediaPlayer.create(context, R.raw.piece_move);
        pieceCapturePlayer = MediaPlayer.create(context, R.raw.piece_capture);
        pieceHomePlayer = MediaPlayer.create(context, R.raw.piece_home);
        gameWinPlayer = MediaPlayer.create(context, R.raw.game_win);
        backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music);
        
        // Set background music to loop
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setLooping(true);
            backgroundMusicPlayer.setVolume(0.3f, 0.3f);
        }
    }

    /**
     * Play the dice roll sound
     */
    @JavascriptInterface
    public void playDiceRollSound() {
        if (!soundEnabled) return;
        playSound(diceRollPlayer);
    }

    /**
     * Play the piece movement sound
     */
    @JavascriptInterface
    public void playPieceMoveSound() {
        if (!soundEnabled) return;
        playSound(pieceMovePlayer);
    }

    /**
     * Play the piece capture sound (when a piece is stepped on and sent back to base)
     */
    @JavascriptInterface
    public void playPieceCaptureSound() {
        if (!soundEnabled) return;
        playSound(pieceCapturePlayer);
    }

    /**
     * Play the sound for a piece reaching home
     */
    @JavascriptInterface
    public void playPieceHomeSound() {
        if (!soundEnabled) return;
        playSound(pieceHomePlayer);
    }

    /**
     * Play the game win sound
     */
    @JavascriptInterface
    public void playGameWinSound() {
        if (!soundEnabled) return;
        playSound(gameWinPlayer);
    }

    /**
     * Play or pause the background music
     * @param play True to play, false to pause
     */
    @JavascriptInterface
    public void playBackgroundMusic(boolean play) {
        if (!soundEnabled) return;
        
        if (backgroundMusicPlayer != null) {
            try {
                if (play) {
                    if (!backgroundMusicPlayer.isPlaying()) {
                        backgroundMusicPlayer.start();
                    }
                } else {
                    if (backgroundMusicPlayer.isPlaying()) {
                        backgroundMusicPlayer.pause();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Enable or disable all sounds
     * @param enabled True to enable sounds, false to disable
     */
    @JavascriptInterface
    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        
        // If disabling sound, stop background music
        if (!enabled && backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
        
        // If enabling sound, start background music
        if (enabled && backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }

    /**
     * Helper method to play a sound using MediaPlayer
     * @param player The MediaPlayer instance to use
     */
    private void playSound(MediaPlayer player) {
        if (player != null) {
            try {
                // Reset to start if already playing
                if (player.isPlaying()) {
                    player.seekTo(0);
                } else {
                    player.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Release all MediaPlayer resources when no longer needed
     * Should be called from Activity's onDestroy method
     */
    public void release() {
        releasePlayer(diceRollPlayer);
        releasePlayer(pieceMovePlayer);
        releasePlayer(pieceCapturePlayer);
        releasePlayer(pieceHomePlayer);
        releasePlayer(gameWinPlayer);
        releasePlayer(backgroundMusicPlayer);
    }

    /**
     * Helper method to release a MediaPlayer
     * @param player The MediaPlayer to release
     */
    private void releasePlayer(MediaPlayer player) {
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
