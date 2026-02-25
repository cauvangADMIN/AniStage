package com.anistage.aurawave.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@androidx.media3.common.util.UnstableApi
class AudioEngine(private val context: Context) {

    private val spectrumPlayer = PrecomputedSpectrum(context)

    val spectrumFlow = spectrumPlayer.spectrumFlow
    val beatFlow = spectrumPlayer.beatFlow

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val player = ExoPlayer.Builder(context).build()

    private var updateJob: Job? = null

    fun play(musicUrl: String, spectrumUrl: String) {

        spectrumPlayer.loadFromUrl(spectrumUrl)

        val mediaItem = MediaItem.fromUri(musicUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        _isReady.value = true

        startSpectrumSync()
    }

    private fun startSpectrumSync() {

        updateJob?.cancel()

        updateJob = CoroutineScope(Dispatchers.Main).launch {

            while (isActive) {

                if (player.isPlaying) {

                    val position = player.currentPosition
                    spectrumPlayer.update(position)
                }

                delay(16)
            }
        }
    }

    fun togglePlayback() {
        if (player.isPlaying) player.pause()
        else player.play()
    }

    fun release() {
        updateJob?.cancel()
        player.release()
    }
}