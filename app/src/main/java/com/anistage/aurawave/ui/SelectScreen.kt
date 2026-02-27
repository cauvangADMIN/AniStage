package com.anistage.aurawave.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.anistage.aurawave.model.RemoteData
import com.anistage.aurawave.model.SelectionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SelectScreen(
    data: RemoteData,
    onFinish: (SelectionState) -> Unit
) {
    var step by remember { mutableStateOf(0) }

    val items = when (step) {
        0 -> data.character
        1 -> data.music
        else -> data.background
    }

    var selectedIndex by remember { mutableStateOf(0) }
    val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val selection = remember { SelectionState() }
    val currentName = items.getOrNull(selectedIndex)
        ?.substringAfterLast("/")
        ?.substringBefore(".")

    var visibleName by remember { mutableStateOf(currentName) }
    var isVisible by remember { mutableStateOf(true) }

    val title = when (step) {
        0 -> "CHARACTER SELECT"
        1 -> "MUSIC SELECT"
        else -> "BACKGROUND SELECT"
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentName) {
        if (currentName != visibleName) {
            isVisible = false
            delay(200)   // đợi exit xong
            visibleName = currentName
            isVisible = true
        }
    }
    LaunchedEffect(items.size) {
        if (selectedIndex > items.lastIndex) {
            selectedIndex = 0
        }
    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
        .background(Color.Black)) {

        // ================= BACKGROUND BLUR =================
        AsyncImage(
            model = items.getOrNull(selectedIndex),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .alpha(0.35f)
        )

        // ================= VIGNETTE =================
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        radius = 1200f
                    )
                )
        )

        // ================= CHARACTER (FIXED POSITION) =================
        AnimatedContent(
            targetState = safeIndex,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(500))
            }
        ) { index ->

            AsyncImage(
                model = items.getOrNull(index),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)   // 👈 giữ nhân vật trong màn hình
                    .scale(1.15f)
            )
        }

        // ================= CINEMATIC NAME (TOP RIGHT ZONE) =================
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = tween(350)
            ) + fadeIn(tween(350)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it / 2 },
                animationSpec = tween(300)
            ) + fadeOut(tween(200))
        ) {

            Text(
                text = visibleName ?: "",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = maxWidth * 0.45f,
                        top = maxHeight * 0.12f
                    )
            )
        }

        // ================= SELECTOR SECTION =================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // LEFT ARROW
                    Text(
                        text = "<",
                        fontSize = 48.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                if (selectedIndex > 0) {
                                    selectedIndex--
                                }
                            }
                    )

                    // CAROUSEL
                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.width(640.dp)
                    ) {

                        itemsIndexed(items) { index, url ->

                            val isSelected = index == selectedIndex
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 0.9f,
                                animationSpec = tween(300)
                            )

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .scale(scale)
                                    .border(
                                        width = if (isSelected) 4.dp else 0.dp,
                                        color = Color(0xFFFF2ED1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedIndex = index
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.TopCenter,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // RIGHT ARROW
                    Text(
                        text = ">",
                        fontSize = 48.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                if (selectedIndex < items.lastIndex) {
                                    selectedIndex++
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ================= CENTER TITLE =================
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF2ED1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            // ================= RETURN / CONFIRM =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "RETURN",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        if (step > 0) step--
                    }
                )

                Text(
                    text = if (step == 2) "ON STAGE !!" else "CONFIRM",
                    color = Color(0xFFFF2ED1),
                    modifier = Modifier.clickable {

                        when (step) {
                            0 -> selection.character = items[selectedIndex]
                            1 -> {
                                selection.music = items[selectedIndex]
                                selection.spectrum = data.spectrum[selectedIndex]
                            }
                            2 -> selection.background = items[selectedIndex]
                        }

                        if (step < 2) {
                            step++
                            selectedIndex = 0
                        } else {
                            onFinish(selection)
                        }
                    }
                )
            }
        }
    }
}