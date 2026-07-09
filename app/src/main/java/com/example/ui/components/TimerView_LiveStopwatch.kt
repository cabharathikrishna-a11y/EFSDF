package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.AppViewModel

@Composable
fun LiveControlBreakBar(
    viewModel: AppViewModel,
    context: Context,
    wasStartedFromStopwatch: Boolean,
    isTimerActive: Boolean,
    stopwatchSeconds: Int,
    WaterBlue: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (wasStartedFromStopwatch) {
            Button(
                onClick = {
                    viewModel.pauseTimer()
                    viewModel.switchToFocusPhaseFromStopwatch()
                    viewModel.startStopwatch()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume Stopwatch", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val onClickAction = if (isTimerActive) { { viewModel.pauseTimer() } } else { { viewModel.startTimer() } }
            val btnBg = if (isTimerActive) Color.White.copy(alpha = 0.15f) else WaterBlue
            val btnFg = if (isTimerActive) Color.White else Color.Black
            val iconRes = if (isTimerActive) Icons.Default.Pause else Icons.Default.PlayArrow

            Button(
                onClick = onClickAction,
                colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = btnFg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(iconRes, contentDescription = "Toggle", tint = btnFg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTimerActive) "Pause" else "Resume", color = btnFg, fontSize = 13.sp, fontWeight = if (isTimerActive) FontWeight.Normal else FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = {
                if (wasStartedFromStopwatch) {
                    viewModel.pauseTimer()
                    viewModel.prepareAndShowEndSessionDialog("stopwatch", stopwatchSeconds)
                } else {
                    viewModel.skipOrEndBreak()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stop, contentDescription = "End", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "End Break", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LiveControlStopwatchBar(
    viewModel: AppViewModel,
    selectedTask: Task?,
    isStopwatchActive: Boolean,
    sessionStartTimestamp: Long?,
    stopwatchSeconds: Int,
    globalTodaySeconds: Int,
    WaterBlue: Color
) {
    val selectedTag by viewModel.attachedTag.collectAsState()

    if (isStopwatchActive) {
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
                onClick = { viewModel.pauseStopwatch() },
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
                    viewModel.pauseStopwatch()
                    viewModel.prepareAndShowEndSessionDialog("stopwatch", stopwatchSeconds)
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
        if (sessionStartTimestamp == null && stopwatchSeconds == 0) {
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
                        viewModel.setTabFocusTimerSelected(false)
                        viewModel.setSessionStartTimestamp(com.example.util.StableTime.currentTimeMillis())
                        viewModel.startStopwatch()
                        viewModel.setTimerImmersive(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_stopwatch_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Stopwatch", modifier = Modifier.size(20.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Stopwatch", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
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
                        viewModel.startStopwatch()
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
                        viewModel.pauseStopwatch()
                        viewModel.prepareAndShowEndSessionDialog("stopwatch", stopwatchSeconds)
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
