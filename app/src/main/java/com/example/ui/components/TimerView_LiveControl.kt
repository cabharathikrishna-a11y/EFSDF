package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel

@Composable
fun TimerLiveControlContent(
    viewModel: AppViewModel,
    isTablet: Boolean,
    isImmersive: Boolean,
    isAntiBurnCenteredByTap: Boolean,
    globalTodaySeconds: Int,
    focusTimerDurationMins: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val WaterBlue = Color(0xFF38BDF8)

    val isTimerActive by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSecondsRemaining by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val isFocusPhase by viewModel.isFocusPhase.collectAsStateWithLifecycle()
    val cumulativeSessionFocusSeconds by viewModel.cumulativeSessionFocusSeconds.collectAsStateWithLifecycle()
    val isInBreakMode = !isFocusPhase

    val stopwatchSeconds by viewModel.stopwatchSeconds.collectAsStateWithLifecycle()
    val isStopwatchActive by viewModel.isStopwatchActive.collectAsStateWithLifecycle()
    val isTabFocusTimerSelected by viewModel.isTabFocusTimerSelected.collectAsStateWithLifecycle()
    val wasStartedFromStopwatch by viewModel.wasStartedFromStopwatch.collectAsStateWithLifecycle()

    val isStopwatchOnOrActive = isStopwatchActive || stopwatchSeconds > 0
    val isTimerOnOrActive = isTimerActive || (timerSecondsRemaining < focusTimerDurationMins * 60)

    val waterReminderEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle()
    var soundPlayingNotification by remember { mutableStateOf<String?>(null) }
    val selectedTask by viewModel.attachedTask.collectAsStateWithLifecycle()
    val sessionStartTimestamp by viewModel.sessionStartTimestamp.collectAsStateWithLifecycle()

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = if (isTablet) Color(0xFF101010) else Color.Black),
        border = if (isTablet) BorderStroke(1.dp, Color(0xFF222222)) else null,
        shape = if (isTablet) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 18.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Toggles Focus vs Stopwatch
            if (!isTimerOnOrActive && !isStopwatchOnOrActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF151515))
                        .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(32.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (isTabFocusTimerSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setTabFocusTimerSelected(true) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Promodora", color = if (isTabFocusTimerSelected) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (!isTabFocusTimerSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setTabFocusTimerSelected(false) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Stopwatch", color = if (!isTabFocusTimerSelected) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Numeric display unified for both Timer and Stopwatch
            Box(
                modifier = Modifier.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isTabFocusTimerSelected || isInBreakMode) {
                        RenderDigitalDigits(
                            viewModel = viewModel,
                            seconds = timerSecondsRemaining,
                            isImmersive = isImmersive,
                            isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                            isBlinking = isInBreakMode
                        )
                        Text(
                            text = if (isTimerActive) {
                                if (isInBreakMode) "now u r in a break" else "KEEP FOCUSING"
                            } else {
                                if (isInBreakMode) "now u r in a break" else "STOPPED"
                            },
                            color = if (isTimerActive) {
                                if (isInBreakMode) Color(0xFF81C784) else WaterBlue
                            } else {
                                if (isInBreakMode) Color(0xFF81C784) else Color.Gray
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        RenderDigitalDigits(
                            viewModel = viewModel,
                            seconds = stopwatchSeconds,
                            isImmersive = isImmersive,
                            isAntiBurnCenteredByTap = isAntiBurnCenteredByTap,
                            isBlinking = false
                        )
                        Text(
                            text = if (isStopwatchActive) "KEEP FOCUSING" else "STOPPED",
                            color = if (isStopwatchActive) WaterBlue else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound playing visuals
            if (soundPlayingNotification != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WaterBlue),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Active Alarm", tint = WaterBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(soundPlayingNotification ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Control Actions Bar delegated to subcomponents
            if (!isFocusPhase) {
                LiveControlBreakBar(
                    viewModel = viewModel,
                    context = context,
                    wasStartedFromStopwatch = wasStartedFromStopwatch,
                    isTimerActive = isTimerActive,
                    stopwatchSeconds = stopwatchSeconds,
                    WaterBlue = WaterBlue
                )
            } else if (isTabFocusTimerSelected) {
                LiveControlTimerBar(
                    viewModel = viewModel,
                    selectedTask = selectedTask,
                    isTimerActive = isTimerActive,
                    sessionStartTimestamp = sessionStartTimestamp,
                    timerSecondsRemaining = timerSecondsRemaining,
                    focusTimerDurationMins = focusTimerDurationMins,
                    cumulativeSessionFocusSeconds = cumulativeSessionFocusSeconds,
                    globalTodaySeconds = globalTodaySeconds,
                    WaterBlue = WaterBlue
                )
            } else {
                LiveControlStopwatchBar(
                    viewModel = viewModel,
                    selectedTask = selectedTask,
                    isStopwatchActive = isStopwatchActive,
                    sessionStartTimestamp = sessionStartTimestamp,
                    stopwatchSeconds = stopwatchSeconds,
                    globalTodaySeconds = globalTodaySeconds,
                    WaterBlue = WaterBlue
                )
            }
        }
    }
}
