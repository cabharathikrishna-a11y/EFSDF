package com.example.ui.components

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.PremiumEffects.bouncyClick
import com.example.ui.theme.PremiumEffects.glassmorphicCard
import com.example.ui.theme.WaterBlue
import kotlinx.coroutines.delay

@Composable
fun TimerImmersiveContent(
    viewModel: AppViewModel,
    focusTimerDurationMins: Int,
    onShowFriendsDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isVerticalPhone = configuration.screenWidthDp < 600

    val isFocusPhase by viewModel.isFocusPhase.collectAsStateWithLifecycle()
    val isTimerActive by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSecondsRemaining by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val stopwatchSeconds by viewModel.stopwatchSeconds.collectAsStateWithLifecycle()
    val isStopwatchActive by viewModel.isStopwatchActive.collectAsStateWithLifecycle()
    val isTabFocusTimerSelected by viewModel.isTabFocusTimerSelected.collectAsStateWithLifecycle()
    val wasStartedFromStopwatch by viewModel.wasStartedFromStopwatch.collectAsStateWithLifecycle()
    val cumulativeSessionFocusSeconds by viewModel.cumulativeSessionFocusSeconds.collectAsStateWithLifecycle()
    val selectedTask by viewModel.attachedTask.collectAsStateWithLifecycle()
    val timerDisplayMode by viewModel.timerDisplayMode.collectAsStateWithLifecycle()

    val motivationalQuoteEnabled by viewModel.focusMotivationalQuoteEnabled.collectAsStateWithLifecycle()
    val quoteIntervalMins by viewModel.focusMotivationalQuoteIntervalMins.collectAsStateWithLifecycle()
    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()

    var areControlsVisible by remember { mutableStateOf(true) }
    var isAntiBurnCenteredByTap by remember { mutableStateOf(false) }
    var interactionCounter by remember { mutableStateOf(0) }

    val minutesElapsedTotal = (System.currentTimeMillis() / 60000).toInt()
    val periodIndex = (minutesElapsedTotal / 5) % 4

    LaunchedEffect(viewModel, motivationalQuoteEnabled, quoteIntervalMins) {
        if (motivationalQuoteEnabled) {
            if (viewModel.currentQuote.value.isEmpty()) {
                viewModel.triggerNextMotivationalQuote()
            }
            while (true) {
                delay(quoteIntervalMins * 60 * 1000L)
                viewModel.triggerNextMotivationalQuote()
            }
        }
    }

    LaunchedEffect(periodIndex) {
        isAntiBurnCenteredByTap = false
    }

    LaunchedEffect(areControlsVisible, interactionCounter) {
        if (areControlsVisible) {
            delay(10000) // 10 seconds auto-hide
            areControlsVisible = false
        }
    }

    LaunchedEffect(isFocusPhase, isTimerActive, isStopwatchActive) {
        areControlsVisible = true
        interactionCounter++
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = true)
                    var isDrag = false
                    var dragDirection: String? = null
                    val touchSlop = viewConfiguration.touchSlop
                    var totalDragX = 0f
                    var totalDragY = 0f
                    
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) {
                            break
                        }
                        
                        totalDragX = change.position.x - down.position.x
                        totalDragY = change.position.y - down.position.y
                        
                        if (!isDrag) {
                            if (kotlin.math.abs(totalDragX) > touchSlop || kotlin.math.abs(totalDragY) > touchSlop) {
                                isDrag = true
                                dragDirection = if (kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY)) {
                                    "horizontal"
                                } else {
                                    "vertical"
                                }
                            }
                        }
                        
                        if (isDrag) {
                            change.consume()
                        }
                    }
                    
                    if (isDrag) {
                        if (dragDirection == "horizontal") {
                            if (kotlin.math.abs(totalDragX) > 80f) {
                                val currentMode = viewModel.timerDisplayMode.value
                                val nextMode = if (currentMode == "digital") "flip" else "digital"
                                viewModel.setTimerDisplayMode(nextMode)
                            }
                        } else if (dragDirection == "vertical") {
                            if (kotlin.math.abs(totalDragY) > 80f) {
                                viewModel.setTimerImmersive(false)
                            }
                        }
                    } else {
                        areControlsVisible = !areControlsVisible
                        isAntiBurnCenteredByTap = true
                        interactionCounter++
                    }
                }
            }
            .padding(24.dp)
    ) {
        // Upper block: Show the focusing people emoji bubble and potential quote below it
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.85f)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FriendsFocusPill(
                viewModel = viewModel,
                onClick = onShowFriendsDetails
            )

            if (motivationalQuoteEnabled && currentQuote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Crossfade(
                    targetState = currentQuote,
                    animationSpec = androidx.compose.animation.core.tween(1500),
                    label = "quote_crossfade"
                ) { targetQuote ->
                    Text(
                        text = "\"$targetQuote\"",
                        color = Color(0xFFFFEB3B).copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Close button fixed at top right corner only
        if (areControlsVisible) {
            IconButton(
                onClick = { viewModel.setTimerImmersive(false) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .testTag("exit_immersive_btn")
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit Immersive", tint = Color.White)
            }
        }

        // Exactly Centered Timer Display
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Task Name (visible only when controls are visible)
            if (areControlsVisible) {
                val displayName = selectedTask?.title ?: "GENERAL FOCUS SPHERE"
                Text(
                    text = displayName.uppercase(),
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            val currentSeconds = if (!isFocusPhase) {
                timerSecondsRemaining
            } else if (isTabFocusTimerSelected) {
                timerSecondsRemaining
            } else {
                stopwatchSeconds
            }

            val isBlinking = !isFocusPhase || (isTabFocusTimerSelected && !isFocusPhase)

            if (timerDisplayMode == "flip") {
                RenderFlipDigits(
                    viewModel = viewModel,
                    seconds = currentSeconds,
                    isImmersive = true,
                    isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                    isBlinking = isBlinking,
                    isVerticalPhone = isVerticalPhone
                )
                if (!isFocusPhase) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("now u r in a break", color = Color(0xFF81C784), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            } else {
                if (!isFocusPhase) {
                    RenderDigitalDigits(
                        viewModel = viewModel,
                        seconds = timerSecondsRemaining,
                        isImmersive = true,
                        isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                        isBlinking = true,
                        isVerticalPhone = isVerticalPhone
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("now u r in a break", color = Color(0xFF81C784), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                } else if (isTabFocusTimerSelected) {
                    RenderDigitalDigits(
                        viewModel = viewModel,
                        seconds = timerSecondsRemaining,
                        isImmersive = true,
                        isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                        isBlinking = !isFocusPhase,
                        isVerticalPhone = isVerticalPhone
                    )
                } else {
                    RenderDigitalDigits(
                        viewModel = viewModel,
                        seconds = stopwatchSeconds,
                        isImmersive = true,
                        isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                        isBlinking = false,
                        isVerticalPhone = isVerticalPhone
                    )
                }
            }

            // Swipe instruction indicator
            if (areControlsVisible) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(0.6f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (timerDisplayMode == "digital") Color.White else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "SWIPE TO SWITCH MODE",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (timerDisplayMode == "flip") Color.White else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons row (visible only when controls are visible)
            if (areControlsVisible) {
                val selectedTag by viewModel.attachedTag.collectAsStateWithLifecycle()
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (isFocusPhase) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TagInterlinkSearchVBar(
                                selectedTag = selectedTag,
                                onClear = { viewModel.attachTagToTimer("") },
                                onClick = { viewModel.setShowTagSelectionDialog(true) },
                                modifier = Modifier.weight(1f)
                            )
                            TaskInterlinkSearchVBar(
                                selectedTask = selectedTask,
                                onClear = { viewModel.attachTaskToTimer(null) },
                                onClick = { viewModel.setShowTaskSelectionDialog(true) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFocusPhase) {
                            // Break Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        if (isTabFocusTimerSelected) {
                                            viewModel.pauseTimer()
                                            viewModel.takeBreakFromPomodoro()
                                        } else {
                                            viewModel.pauseStopwatch()
                                            viewModel.takeBreakFromStopwatch()
                                        }
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = Color(0x5581C784),
                                        backgroundColor = Color(0x334CAF50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Break", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Pause / Resume Button
                            val isActive = if (isTabFocusTimerSelected) isTimerActive else isStopwatchActive
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        if (isTabFocusTimerSelected) {
                                            if (isActive) viewModel.pauseTimer() else viewModel.startTimer()
                                        } else {
                                            if (isActive) viewModel.pauseStopwatch() else viewModel.startStopwatch()
                                        }
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = if (isActive) Color(0x33FFFFFF) else WaterBlue.copy(alpha = 0.5f),
                                        backgroundColor = if (isActive) Color(0x40222222) else WaterBlue.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isActive) "Pause" else "Resume", color = if (isActive) Color.White else WaterBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            
                            // End Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        if (isTabFocusTimerSelected) {
                                            viewModel.pauseTimer()
                                            viewModel.prepareAndShowEndSessionDialog("timer", cumulativeSessionFocusSeconds)
                                        } else {
                                            viewModel.pauseStopwatch()
                                            viewModel.prepareAndShowEndSessionDialog("stopwatch", stopwatchSeconds)
                                        }
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = Color(0x15F9325D),
                                        backgroundColor = Color(0x40C62828)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("End", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            // BREAK PHASE
                            val isBreakActive = isTimerActive
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        if (isBreakActive) {
                                            viewModel.pauseTimer()
                                        } else {
                                            viewModel.startTimer()
                                        }
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = if (isBreakActive) Color(0x33FFFFFF) else WaterBlue.copy(alpha = 0.5f),
                                        backgroundColor = if (isBreakActive) Color(0x40222222) else WaterBlue.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isBreakActive) "Pause" else "Resume", color = if (isBreakActive) Color.White else WaterBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Start Focus
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        viewModel.pauseTimer()
                                        if (isTabFocusTimerSelected) {
                                            if (wasStartedFromStopwatch) {
                                                viewModel.switchToFocusPhaseFromStopwatch()
                                                viewModel.startStopwatch()
                                            } else {
                                                viewModel.resetWorkPhaseTimer(focusTimerDurationMins)
                                                viewModel.startTimer()
                                            }
                                        } else {
                                            viewModel.switchToFocusPhase()
                                            viewModel.startStopwatch()
                                        }
                                        viewModel.setTimerImmersive(true)
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = WaterBlue.copy(alpha = 0.6f),
                                        backgroundColor = WaterBlue.copy(alpha = 0.35f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isTabFocusTimerSelected && !wasStartedFromStopwatch) "Start Pomo" else "Start Stopw", color = WaterBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // End Break
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        if (isTabFocusTimerSelected) {
                                            viewModel.skipOrEndBreak()
                                        } else {
                                            viewModel.pauseTimer()
                                            viewModel.prepareAndShowEndSessionDialog("stopwatch", stopwatchSeconds)
                                        }
                                    }
                                    .glassmorphicCard(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.5.dp,
                                        borderColor = Color(0x33C62828),
                                        backgroundColor = Color(0x22C62828)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("End", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderFlipDigits(
    viewModel: AppViewModel,
    seconds: Int,
    isImmersive: Boolean,
    isAntiBurnCenteredByTap: Boolean,
    isBlinking: Boolean = false,
    isVerticalPhone: Boolean = false
) {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60

    val antiBurnScreenEnabled by viewModel.antiBurnScreenEnabled.collectAsStateWithLifecycle()
    val minutesElapsedTotal = (System.currentTimeMillis() / 60000).toInt()
    val periodIndex = (minutesElapsedTotal / 5) % 4

    val antiBurnOffset = if (antiBurnScreenEnabled && isImmersive && !isAntiBurnCenteredByTap) {
        when (periodIndex) {
            0 -> Modifier.offset(x = (-40).dp, y = (-30).dp)
            1 -> Modifier.offset(x = (40).dp, y = (30).dp)
            2 -> Modifier.offset(x = (30).dp, y = (-40).dp)
            else -> Modifier.offset(x = (-30).dp, y = (40).dp)
        }
    } else {
        Modifier
    }

    val blinkAlpha = if (isBlinking) {
        val infiniteTransition = rememberInfiniteTransition(label = "flip_blink")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flip_blinkAlpha"
        )
        alpha
    } else {
        1.0f
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    val cardSize = if (isPortrait) {
        if (h > 0) 130.dp else 180.dp
    } else {
        if (h > 0) 95.dp else 125.dp
    }

    val fontSize = if (isPortrait) {
        if (h > 0) 75.sp else 115.sp
    } else {
        if (h > 0) 50.sp else 75.sp
    }

    Box(
        modifier = antiBurnOffset
            .alpha(blinkAlpha)
            .testTag("timer_flip_display")
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (isPortrait) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (h > 0) {
                    FlipCard(valueString = String.format(java.util.Locale.US, "%02d", h), cardSize = cardSize, fontSize = fontSize)
                }
                FlipCard(valueString = String.format(java.util.Locale.US, "%02d", m), cardSize = cardSize, fontSize = fontSize)
                FlipCard(valueString = String.format(java.util.Locale.US, "%02d", s), cardSize = cardSize, fontSize = fontSize)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (h > 0) {
                    FlipCard(valueString = String.format(java.util.Locale.US, "%02d", h), cardSize = cardSize, fontSize = fontSize)
                }
                FlipCard(valueString = String.format(java.util.Locale.US, "%02d", m), cardSize = cardSize, fontSize = fontSize)
                FlipCard(valueString = String.format(java.util.Locale.US, "%02d", s), cardSize = cardSize, fontSize = fontSize)
            }
        }
    }
}

@Composable
fun FlipCard(
    valueString: String,
    cardSize: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedContent(
        targetState = valueString,
        transitionSpec = {
            (androidx.compose.animation.slideInVertically { height -> -height } + androidx.compose.animation.fadeIn(animationSpec = tween(150)))
                .togetherWith(androidx.compose.animation.slideOutVertically { height -> height } + androidx.compose.animation.fadeOut(animationSpec = tween(150)))
        },
        label = "flip_card_anim"
    ) { animatedValue ->
        Box(
            modifier = modifier
                .size(cardSize)
                .background(Color.Black, shape = RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color(0xFF2E2E31)), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF202022),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color(0xFF0C0C0D))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF151517),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                )
            }
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-4).dp)
                        .size(width = 8.dp, height = 12.dp)
                        .background(Color.Black, shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 4.dp)
                        .size(width = 8.dp, height = 12.dp)
                        .background(Color.Black, shape = RoundedCornerShape(4.dp))
                )
            }

            Text(
                text = animatedValue,
                color = Color(0xFFECECEC),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


