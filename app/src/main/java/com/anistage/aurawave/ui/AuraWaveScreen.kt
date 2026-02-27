package com.anistage.aurawave.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anistage.aurawave.audio.AudioEngine
import com.anistage.aurawave.model.SelectionState

@androidx.media3.common.util.UnstableApi
@Composable
fun AuraWaveScreen(
    audioEngine: AudioEngine,
    selection: SelectionState
) {

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

        // ===== BACKGROUND (FROM URL) =====
        AsyncImage(
            model = selection.background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!isReady) {

            LoadingScreen()

        } else {

            // ===== LIGHT EFFECT =====
            LightEffect(intensity = beat)

            // ===== CHARACTER (FROM URL) =====
            AsyncImage(
                model = selection.character,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f)
            )

            // ===== SPECTRUM =====
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