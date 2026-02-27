package com.anistage.aurawave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.media3.common.util.UnstableApi
import com.anistage.aurawave.audio.AudioEngine
import com.anistage.aurawave.data.RemoteRepository
import com.anistage.aurawave.model.RemoteData
import com.anistage.aurawave.model.SelectionState
import com.anistage.aurawave.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// ================= STAGE STATE =================

enum class StageState {
    Idle,
    Closing,
    Opening
}

enum class StageMode {
    None,
    Entering,
    Exiting
}

@UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // ================= STATE =================

            var remoteData by remember { mutableStateOf<RemoteData?>(null) }
            var selection by remember { mutableStateOf<SelectionState?>(null) }
            var pendingSelection by remember { mutableStateOf<SelectionState?>(null) }

            var stageState by remember { mutableStateOf(StageState.Idle) }
            var stageMode by remember { mutableStateOf(StageMode.None) }

            val engine = remember { AudioEngine(this) }

            // ===== PRELOAD STAGE DOOR IMAGE =====
            val stagePainter = painterResource(R.drawable.stage_door)

            LaunchedEffect(Unit) {
                stagePainter.intrinsicSize
            }

            // ================= FETCH REMOTE DATA =================

            LaunchedEffect(Unit) {
                remoteData = withContext(Dispatchers.IO) {
                    RemoteRepository.fetchData()
                }
            }

            // ================= UI ROOT =================

            Box(modifier = Modifier.fillMaxSize()) {

                when {

                    remoteData == null -> {
                        LoadingScreen()
                    }

                    selection == null -> {
                        SelectScreen(remoteData!!) { selected ->
                            pendingSelection = selected
                            stageMode = StageMode.Entering
                            stageState = StageState.Closing
                        }
                    }

                    else -> {
                        AuraWaveScreen(
                            audioEngine = engine,
                            selection = selection!!,
                            onRequestBack = {
                                stageMode = StageMode.Exiting
                                stageState = StageState.Closing
                            }
                        )
                    }
                }

                // ================= STAGE DOOR OVERLAY =================

                if (stageState != StageState.Idle) {

                    StageDoorOverlay(
                        painter = stagePainter,
                        state = stageState,

                        onClosed = {

                            when (stageMode) {

                                // ===== ENTER FLOW =====
                                StageMode.Entering -> {
                                    selection = pendingSelection
                                    selection?.let {
                                        engine.play(
                                            it.music,
                                            it.spectrum
                                        )
                                    }
                                }

                                // ===== EXIT FLOW =====
                                StageMode.Exiting -> {
                                    engine.release()
                                    selection = null
                                }

                                else -> {}
                            }
                        },

                        onOpened = {
                            stageState = StageState.Idle
                            stageMode = StageMode.None
                        }
                    )
                }
            }

            // ================= AUTO OPEN LOGIC =================

            LaunchedEffect(stageMode, selection) {

                when (stageMode) {

                    // ===== ENTER: đợi nhạc 3s =====
                    StageMode.Entering -> {
                        selection?.let {
                            delay(3000)
                            stageState = StageState.Opening
                        }
                    }

                    // ===== EXIT: đợi SelectScreen load + 3s =====
                    StageMode.Exiting -> {
                        if (selection == null) {
                            delay(3000)
                            stageState = StageState.Opening
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // đảm bảo giải phóng player
        // engine.release() nếu muốn tuyệt đối an toàn
    }
}