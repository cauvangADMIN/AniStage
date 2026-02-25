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

            // Fetch JSON từ GitHub
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

                    val engine = remember {
                        AudioEngine(this)
                    }

                    // Play khi đã chọn xong
                    LaunchedEffect(selection) {
                        engine.play(
                            selection!!.music,
                            selection!!.spectrum
                        )
                    }

                    AuraWaveScreen(engine)
                }
            }
        }
    }
}