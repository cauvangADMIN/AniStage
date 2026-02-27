package com.anistage.aurawave.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import com.anistage.aurawave.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.anistage.aurawave.StageState
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun StageDoorOverlay(
    state: StageState,
    onClosed: () -> Unit,
    onOpened: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var screenHeightPx by remember { mutableStateOf(0f) }

    val offsetY = remember { Animatable(-1000f) }

    // Khi state thay đổi
    LaunchedEffect(state, screenHeightPx) {

        if (screenHeightPx == 0f) return@LaunchedEffect

        when (state) {

            StageState.Closing -> {
                offsetY.snapTo(-screenHeightPx)

                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                )

                onClosed()
            }

            StageState.Opening -> {

                offsetY.animateTo(
                    targetValue = -screenHeightPx,
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                )

                onOpened()
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                screenHeightPx = it.height.toFloat()
            }
    ) {

        Image(
            painter = painterResource(R.drawable.stage_door),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(0, offsetY.value.roundToInt())
                }
        )
    }
}