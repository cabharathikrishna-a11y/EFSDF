package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.AppViewModel

@Composable
fun RenderDigitalDigits(
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

    val textString = if (seconds >= 3600) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", m, s)
    }

    val antiBurnScreenEnabled by viewModel.antiBurnScreenEnabled.collectAsState()
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
        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blinkAlpha"
        )
        alpha
    } else {
        1.0f
    }

    if (isVerticalPhone) {
        Column(
            modifier = antiBurnOffset.alpha(blinkAlpha).testTag("timer_digital_display"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-16).dp)
        ) {
            if (h > 0) {
                Text(
                    text = String.format("%02d", h),
                    color = Color.White,
                    fontSize = 130.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-4).sp
                )
            }
            Text(
                text = String.format("%02d", m),
                color = Color.White,
                fontSize = 130.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-4).sp
            )
            Text(
                text = String.format("%02d", s),
                color = Color.White,
                fontSize = 130.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-4).sp
            )
        }
    } else {
        Text(
            text = textString,
            color = Color.White,
            fontSize = if (isImmersive) 110.sp else 86.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-4).sp,
            modifier = antiBurnOffset
                .alpha(blinkAlpha)
                .testTag("timer_digital_display")
        )
    }
}

@Composable
fun TaskInterlinkSearchVBar(
    selectedTask: Task?,
    onClear: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val WaterBlue = Color(0xFF38BDF8)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
        border = BorderStroke(1.dp, if (selectedTask != null) WaterBlue.copy(alpha = 0.6f) else Color(0xFF232326))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (selectedTask != null) WaterBlue.copy(alpha = 0.12f) else Color(0xFF1E1E22),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTask != null) Icons.Default.Link else Icons.Default.Search,
                        contentDescription = "Link Task",
                        tint = if (selectedTask != null) WaterBlue else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (selectedTask != null) "LINKED TASK" else "TASK LINK",
                        color = Color.Gray,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = selectedTask?.title ?: "Select...",
                        color = if (selectedTask != null) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTask != null) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            if (selectedTask != null) {
                IconButton(
                    onClick = {
                        onClear()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selected task",
                        tint = Color.LightGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TagInterlinkSearchVBar(
    selectedTag: String,
    onClear: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val WaterBlue = Color(0xFF38BDF8)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
        border = BorderStroke(1.dp, if (selectedTag.isNotEmpty()) WaterBlue.copy(alpha = 0.6f) else Color(0xFF232326))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (selectedTag.isNotEmpty()) WaterBlue.copy(alpha = 0.12f) else Color(0xFF1E1E22),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Select Tag",
                        tint = if (selectedTag.isNotEmpty()) WaterBlue else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "FOCUS CATEGORY",
                        color = Color.Gray,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (selectedTag.isNotEmpty()) selectedTag else "Select...",
                        color = if (selectedTag.isNotEmpty()) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTag.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            if (selectedTag.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClear()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selected tag",
                        tint = Color.LightGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveControlTimerBar(
    viewModel: AppViewModel,
    selectedTask: Task?,
    isTimerActive: Boolean,
    sessionStartTimestamp: Long?,
    timerSecondsRemaining: Int,
    focusTimerDurationMins: Int,
    cumulativeSessionFocusSeconds: Int,
    globalTodaySeconds: Int,
    WaterBlue: Color
) {
    val selectedTag by viewModel.attachedTag.collectAsState()

    if (isTimerActive) {
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
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.pauseTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause", color = Color.White, fontSize = 13.sp)
                }
            }

            Button(
                onClick = {
                    viewModel.pauseTimer()
                    viewModel.prepareAndShowEndSessionDialog("timer", cumulativeSessionFocusSeconds)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    } else {
        if (sessionStartTimestamp == null && timerSecondsRemaining == focusTimerDurationMins * 60) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Focused Today", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = formatLiveSeconds(globalTodaySeconds), color = WaterBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.setTabFocusTimerSelected(true)
                        viewModel.setSessionStartTimestamp(com.example.util.StableTime.currentTimeMillis())
                        viewModel.startTimer()
                        viewModel.setTimerImmersive(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_timer_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Focus", modifier = Modifier.size(20.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Focus", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    }
                }
            }
        } else {
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
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.startTimer()
                        viewModel.setTimerImmersive(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        viewModel.pauseTimer()
                        viewModel.prepareAndShowEndSessionDialog("timer", cumulativeSessionFocusSeconds)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stop, contentDescription = "End", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("End", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
