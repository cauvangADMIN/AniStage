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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SelectScreen(
    data: RemoteData,
    onFinish: (SelectionState) -> Unit
) {

    var step by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(0) }

    val selection = remember { SelectionState() }

    val items = when (step) {
        0 -> data.character
        1 -> data.music
        else -> data.background
    }

    val currentName = items.getOrNull(selectedIndex)
        ?.substringAfterLast("/")
        ?.substringBefore(".")

    val title = when (step) {
        0 -> "CHARACTER SELECT"
        1 -> "MUSIC SELECT"
        else -> "BACKGROUND SELECT"
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

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
            targetState = selectedIndex,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(500))
            }
        ) { index ->

            AsyncImage(
                model = items[index],
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
        AnimatedContent(
            targetState = currentName,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(400))
            }
        ) { name ->

            Text(
                text = name ?: "",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)      // 👈 quan trọng
                    .offset(
                        x = (LocalConfiguration.current.screenWidthDp * 0.45f).dp,
                        y = (LocalConfiguration.current.screenHeightDp * 0.12f).dp
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