package com.anistage.aurawave.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun WaveformView(spectrum: FloatArray) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {

        if (spectrum.isEmpty()) return@Canvas

        val barCount = spectrum.size
        val barWidth = size.width / barCount
        val maxHeight = size.height

        for (i in 0 until barCount) {

            val amplitude = spectrum[i].coerceIn(0f, 1f)

            val barHeight = maxHeight * amplitude

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = i * barWidth,
                    y = maxHeight - barHeight
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = barWidth * 0.7f,
                    height = max(4f, barHeight)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    x = 6f,
                    y = 6f
                )
            )
        }
    }
}
