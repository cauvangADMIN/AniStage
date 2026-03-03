package com.anistage.aurawave.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anistage.aurawave.audio.AudioEngine
import com.anistage.aurawave.model.SelectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@androidx.media3.common.util.UnstableApi
@Composable
fun AuraWaveScreen(
    audioEngine: AudioEngine,
    selection: SelectionState,
    onRequestBack: () -> Unit
) {

    val isReady by audioEngine.isReady.collectAsState()
    val isPlaying by audioEngine.isPlaying.collectAsState()
    val spectrum by audioEngine.spectrumFlow.collectAsState()
    val beat by audioEngine.beatFlow.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {

                if (!isReady) return@clickable

                // Toggle dựa trên state thật
                if (isPlaying) {
                    audioEngine.pause()
                } else {
                    audioEngine.resume()
                }
            }
    ) {

        // ===== BACKGROUND =====
        AsyncImage(
            model = selection.background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!isReady) {

            LoadingScreen()

        } else {

            LightEffect(intensity = beat)

            AsyncImage(
                model = selection.character,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.95f)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                WaveformView(spectrum)
            }
        }

        // ===== BACK BUTTON =====
        Text(
            text = "← Back",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable {
                    showDialog = true
                }
        )

        // ===== CONFIRM DIALOG =====
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    // chỉ resume nếu trước đó đang play
                    if (!isPlaying) {
                        audioEngine.resume()
                    }
                },
                title = {
                    Text("Stop music?")
                },
                text = {
                    Text("Stop music and back to selection screen?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false

                            audioEngine.fadeOutAndPause()

                            scope.launch {
                                delay(400)
                                onRequestBack()
                            }
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            if (!isPlaying) {
                                audioEngine.resume()
                            }
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}