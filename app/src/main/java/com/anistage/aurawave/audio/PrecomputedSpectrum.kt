package com.anistage.aurawave.audio

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.min

class PrecomputedSpectrum(private val context: Context) {

    private val client = OkHttpClient()

    private var frames: List<FloatArray> = emptyList()
    private var frameRate: Int = 60
    private var totalFrames: Int = 0

    private var currentIndex = 0

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _spectrumFlow = MutableStateFlow(FloatArray(32))
    val spectrumFlow: StateFlow<FloatArray> = _spectrumFlow

    private val _beatFlow = MutableStateFlow(0f)
    val beatFlow: StateFlow<Float> = _beatFlow

    fun loadFromUrl(url: String) {

        scope.launch {

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            val jsonString = response.body?.string() ?: return@launch

            val json = JSONObject(jsonString)

            frameRate = json.getInt("frameRate")
            val dataArray = json.getJSONArray("data")

            val tempList = mutableListOf<FloatArray>()

            for (i in 0 until dataArray.length()) {

                val frameJson = dataArray.getJSONArray(i)
                val frame = FloatArray(frameJson.length())

                for (j in 0 until frameJson.length()) {
                    frame[j] = frameJson.getDouble(j).toFloat()
                }

                tempList.add(frame)
            }

            frames = tempList
            totalFrames = frames.size
        }
    }

    fun update(progressMs: Long) {

        if (totalFrames == 0) return

        val frameIndex = ((progressMs / 1000f) * frameRate)
            .toInt()
            .coerceIn(0, totalFrames - 1)

        if (frameIndex != currentIndex) {

            currentIndex = frameIndex

            val frame = frames[currentIndex]

            _spectrumFlow.value = frame

            // Bass detection (4 band đầu)
            val bass = frame.take(min(4, frame.size)).sum() / 4f
            _beatFlow.value = bass.coerceIn(0f, 1f)
        }
    }
}