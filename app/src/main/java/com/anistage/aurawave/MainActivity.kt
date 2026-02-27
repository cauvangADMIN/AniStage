package com.anistage.aurawave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.anistage.aurawave.audio.AudioEngine
import com.anistage.aurawave.data.RemoteRepository
import com.anistage.aurawave.model.RemoteData
import com.anistage.aurawave.model.SelectionState
import com.anistage.aurawave.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class StageState {
    Idle,
    Closing,
    Opening
}

@UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var remoteData by remember { mutableStateOf<RemoteData?>(null) }
            var selection by remember { mutableStateOf<SelectionState?>(null) }
            var pendingSelection by remember { mutableStateOf<SelectionState?>(null) }

            var stageState by remember { mutableStateOf(StageState.Idle) }

            val engine = remember { AudioEngine(this) }

            // ================= FETCH REMOTE =================
            LaunchedEffect(Unit) {
                remoteData = withContext(Dispatchers.IO) {
                    RemoteRepository.fetchData()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {

                when {

                    remoteData == null -> {
                        LoadingScreen()
                    }

                    selection == null -> {
                        SelectScreen(remoteData!!) { selected ->
                            pendingSelection = selected
                            stageState = StageState.Closing
                        }
                    }

                    else -> {
                        AuraWaveScreen(
                            audioEngine = engine,
                            selection = selection!!
                        )
                    }
                }

                // ================= STAGE DOOR OVERLAY =================
                if (stageState != StageState.Idle) {

                    StageDoorOverlay(
                        state = stageState,

                        // Khi cửa đóng hoàn tất
                        onClosed = {

                            selection = pendingSelection

                            selection?.let {
                                engine.play(
                                    it.music,
                                    it.spectrum
                                )
                            }
                        },

                        // Khi cửa mở hoàn tất
                        onOpened = {
                            stageState = StageState.Idle
                        }
                    )
                }
            }

            // ================= AUTO OPEN AFTER 3s =================
            LaunchedEffect(selection) {
                selection?.let {
                    delay(3000)
                    stageState = StageState.Opening
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // đảm bảo giải phóng player
        // nếu cần:
        // engine.release()
    }
}