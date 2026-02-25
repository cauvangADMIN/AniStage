package com.anistage.aurawave.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anistage.aurawave.R
import com.anistage.aurawave.audio.AudioEngine

@androidx.media3.common.util.UnstableApi
@Composable
fun AuraWaveScreen(audioEngine: AudioEngine) {

    val isReady by audioEngine.isReady.collectAsState()
    val spectrum by audioEngine.spectrumFlow.collectAsState()
    val beat by audioEngine.beatFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                audioEngine.togglePlayback()
            }
    ) {

        // Background
        Image(
            painter = painterResource(R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!isReady) {

            LoadingScreen()

        } else {

            // Light Effect
            LightEffect(intensity = beat)

            // Character
            Image(
                painter = painterResource(R.drawable.character),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f),   // 👈 điều chỉnh nếu muốn cao hơn
                contentScale = ContentScale.Fit
            )

            // Spectrum footer
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                WaveformView(spectrum)
            }
        }
    }
}
