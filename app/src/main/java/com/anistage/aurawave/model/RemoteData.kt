package com.anistage.aurawave.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteData(
    val character: List<String>,
    val background: List<String>,
    val music: List<String>,
    val spectrum: List<String>
)