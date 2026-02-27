package com.anistage.aurawave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.anistage.aurawave.audio.AudioEngine
import com.anistage.aurawave.data.RemoteRepository
import com.anistage.aurawave.model.RemoteData
import com.anistage.aurawave.model.SelectionState
import com.anistage.aurawave.ui.AuraWaveScreen
import com.anistage.aurawave.ui.LoadingScreen
import com.anistage.aurawave.ui.SelectScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@androidx.media3.common.util.UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var remoteData by remember { mutableStateOf<RemoteData?>(null) }
            var selection by remember { mutableStateOf<SelectionState?>(null) }

            val engine = remember { AudioEngine(this) }

            // Fetch JSON
            LaunchedEffect(Unit) {
                remoteData = withContext(Dispatchers.IO) {
                    RemoteRepository.fetchData()
                }
            }

            when {

                remoteData == null -> LoadingScreen()

                selection == null -> SelectScreen(remoteData!!) {
                    selection = it
                }

                else -> {

                    // Play khi đã chọn
                    LaunchedEffect(selection) {
                        selection?.let {
                            engine.play(
                                it.music,
                                it.spectrum
                            )
                        }
                    }

                    AuraWaveScreen(
                        audioEngine = engine,
                        selection = selection!!
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // đảm bảo giải phóng player
    }
}