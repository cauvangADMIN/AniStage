package com.anistage.aurawave.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@androidx.media3.common.util.UnstableApi
class AudioEngine(private val context: Context) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    private val spectrumPlayer = PrecomputedSpectrum(context)

    val spectrumFlow = spectrumPlayer.spectrumFlow
    val beatFlow = spectrumPlayer.beatFlow

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val player = ExoPlayer.Builder(context).build()

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var updateJob: Job? = null
    private var fadeJob: Job? = null

    init {
        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                _isReady.value = state == Player.STATE_READY
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun play(musicUrl: String, spectrumUrl: String) {

        fadeJob?.cancel()

        spectrumPlayer.loadFromUrl(spectrumUrl)

        val mediaItem = MediaItem.fromUri(musicUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        startSpectrumSync()
    }

    fun pause() {
        if (player.isPlaying) player.pause()
    }

    fun resume() {
        if (!player.isPlaying) player.play()
    }

    fun fadeOutAndPause(duration: Long = 400) {

        fadeJob?.cancel()

        fadeJob = engineScope.launch {

            val steps = 20
            val stepTime = duration / steps

            for (i in steps downTo 0) {
                val volume = i / steps.toFloat()
                player.volume = volume
                delay(stepTime)
            }

            player.pause()
            player.volume = 1f
        }
    }

    private fun startSpectrumSync() {

        updateJob?.cancel()

        updateJob = engineScope.launch {

            while (isActive) {

                if (player.isPlaying) {
                    val position = player.currentPosition
                    spectrumPlayer.update(position)
                }

                delay(16)
            }
        }
    }

    fun release() {
        fadeJob?.cancel()
        updateJob?.cancel()
        engineScope.cancel()
        player.release()
    }
}