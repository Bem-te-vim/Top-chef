package com.sam.topchef.feature_import_from_tiktok.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

/**
 * Custom implementation of [Player.Listener] to provide functional callbacks for player state changes.
 */
class PlayerListener : Player.Listener {
    private var onChangePlayerStatus: ((Boolean) -> Unit)? = null

    /**
     * Sets a callback to be invoked when the player's play/pause state changes.
     * @param callback A function that receives a boolean indicating if the player is currently playing.
     */
    fun isPlaying(callback: (isPlaying: Boolean) -> Unit) {
        this.onChangePlayerStatus = callback
    }

    /**
     * Called when the value of [Player.isPlaying] changes.
     * @param isPlaying Whether the player is now playing.
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        onChangePlayerStatus?.invoke(isPlaying)
        super.onIsPlayingChanged(isPlaying)
    }

    private var onError: ((PlaybackException) -> Unit)? = null

    /**
     * Sets a callback to be invoked when a player error occurs.
     * @param callback A function that receives the [PlaybackException].
     */
    fun onPlayerError(callback: (PlaybackException) -> Unit) {
        this.onError = callback
    }

    /**
     * Called when an error occurs during playback.
     * @param error The playback error.
     */
    override fun onPlayerError(error: PlaybackException) {
        onError?.invoke(error)
        super.onPlayerError(error)
    }
}
