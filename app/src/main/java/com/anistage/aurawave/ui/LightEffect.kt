package com.anistage.aurawave.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max

@Composable
fun LightEffect(intensity: Float) {

    var lastIntensity by remember { mutableStateOf(0f) }
    var flashTrigger by remember { mutableStateOf(false) }

    val flash = remember { Animatable(0f) }

    LaunchedEffect(intensity) {

        // detect spike (beat)
        if (intensity - lastIntensity > 0.08f) {
            flashTrigger = true
        }

        lastIntensity = intensity

        if (flashTrigger) {
            flash.animateTo(1f, tween(40))
            flash.animateTo(0f, tween(220))
            flashTrigger = false
        }
    }

    val glow = (intensity * 0.6f).coerceIn(0f, 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = flash.value),   // flash mạnh
                        Color.White.copy(alpha = glow),          // glow nền
                        Color.Transparent
                    ),
                    radius = 1200f
                )
            )
    )
}
