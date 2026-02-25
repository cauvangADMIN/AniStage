package com.anistage.aurawave.data

import com.anistage.aurawave.model.RemoteData
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.builtins.serializer

object RemoteRepository {

    private const val URL =
        "https://raw.githubusercontent.com/cauvangADMIN/anistage-storage/refs/heads/main/data.json"

    private val client = OkHttpClient()

    suspend fun fetchData(): RemoteData {

        val request = Request.Builder().url(URL).build()
        val response = client.newCall(request).execute()

        val json = response.body?.string() ?: error("Empty response")

        return Json { ignoreUnknownKeys = true }
            .decodeFromString<RemoteData>(json)
    }
}