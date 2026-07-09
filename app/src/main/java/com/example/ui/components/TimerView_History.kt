package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.FocusRecord
import com.example.util.FocusTimerManager

@Composable
fun TimerHistoryView(
    viewModel: AppViewModel,
    selectedDateStr: String,
    modifier: Modifier = Modifier
) {
    // State for editing focus session logs
    var editingLogId by remember { mutableStateOf<String?>(null) }
    var showEditLogDialog by remember { mutableStateOf(false) }

    var editTaskTitle by remember { mutableStateOf("") }
    var editStartTime by remember { mutableStateOf("") }
    var editEndTime by remember { mutableStateOf("") }
    var editDurationMins by remember { mutableStateOf("") }
    var editDateString by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }
    var editTag by remember { mutableStateOf("") }

    // SEPARATE OVERVIEW AND FOCUS HISTORY PAGE
    var historySubTab by remember { mutableStateOf(0) } // 0 = Focus History, 1 = System Audit Logs
    val auditLogs by FocusTimerManager.systemLogs.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    val focusRecords by viewModel.focusRecords.collectAsStateWithLifecycle()
    val isFocusPhase by viewModel.isFocusPhase.collectAsStateWithLifecycle()
    val cumulativeSessionFocusSeconds by viewModel.cumulativeSessionFocusSeconds.collectAsStateWithLifecycle()
    val stopwatchSeconds by viewModel.stopwatchSeconds.collectAsStateWithLifecycle()
    val pendingFocusReview by viewModel.pendingFocusReview.collectAsStateWithLifecycle()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val sessionStartTimestamp by viewModel.sessionStartTimestamp.collectAsStateWithLifecycle()
    val isTimerActive by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isStopwatchActive by viewModel.isStopwatchActive.collectAsStateWithLifecycle()

    val WaterBlue = Color(0xFF38BDF8)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subtab Selection Segmented Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F12), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { historySubTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (historySubTab == 0) Color(0xFF1E1E24) else Color.Transparent,
                    contentColor = if (historySubTab == 0) Color.White else Color.Gray
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Focus History", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { historySubTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (historySubTab == 1) Color(0xFF1E1E24) else Color.Transparent,
                    contentColor = if (historySubTab == 1) Color.White else Color.Gray
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("System Audit Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (historySubTab == 1) {
            // Render System Audit Logs Terminal View
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D10)),
                border = BorderStroke(1.dp, Color(0xFF1A1A22)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System Security & Audit Engine", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                            Text("Verifying timer calculations, events, and cloud state saves", color = Color.Gray, fontSize = 9.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    viewModel.triggerManualAlignmentCheck()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24), contentColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Align Cloud", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.clearAuditLogs()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221111), contentColor = Color(0xFFFF5555)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Clear Logs", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A22)))

                    if (auditLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No audit logs recorded yet.", color = Color.DarkGray, fontSize = 11.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            auditLogs.forEach { log ->
                                val catColor = when (log.category) {
                                    "BUTTON_PRESS" -> Color(0xFF00ACC1) // Cyan
                                    "FIREBASE_SYNC" -> Color(0xFFFB8C00) // Orange
                                    "STATE_RESTORE" -> Color(0xFF8E24AA) // Purple
                                    "ALARM" -> Color(0xFFE53935) // Red
                                    "SYSTEM" -> Color(0xFF43A047) // Green
                                    else -> Color.Gray
                                }
                                val timeStr = java.text.SimpleDateFormat("hh:mm:ss.SSS a", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131317), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF1F1F24), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.event.uppercase(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.category,
                                            color = catColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier
                                                .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.details,
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "TIMESTAMP: $timeStr",
                                        color = Color.DarkGray,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Daily Chronological Focus Timeline Grid
            DailyFocusTimelineChrono(focusRecords = focusRecords, selectedDateStr = selectedDateStr)

            val localLiveAddedSeconds = remember(isFocusPhase, cumulativeSessionFocusSeconds, stopwatchSeconds, pendingFocusReview) {
                val running = if (isFocusPhase && pendingFocusReview == null) {
                    (cumulativeSessionFocusSeconds + stopwatchSeconds)
                } else 0
                val pending = pendingFocusReview?.durationSeconds ?: ((pendingFocusReview?.durationMinutes ?: 0) * 60)
                running + pending
            }

            // Focus Activity Summary Breakdown Card
            FocusSummaryCard(
                focusRecords = focusRecords,
                todayStr = selectedDateStr,
                totalFocusMinutes = totalFocusMinutes,
                liveAddedMinutes = localLiveAddedSeconds / 60,
                liveAddedSeconds = localLiveAddedSeconds
            )

            val completedSecs = remember(focusRecords, selectedDateStr) {
                focusRecords.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, selectedDateStr) }
            }

            val pendingSecs = remember(pendingFocusReview, selectedDateStr) {
                pendingFocusReview?.let { FocusTimerManager.getOverlapSecondsForDate(it, selectedDateStr) } ?: 0
            }

            val myTodaySeconds = remember(completedSecs, pendingSecs, selectedDateStr, isFocusPhase, sessionStartTimestamp, pendingFocusReview, isTimerActive, isStopwatchActive) {
                val activeSecs = if (isFocusPhase && pendingFocusReview == null) {
                    if ((isTimerActive || isStopwatchActive) && sessionStartTimestamp != null) {
                        FocusTimerManager.getActiveSessionOverlapSeconds(sessionStartTimestamp!!, selectedDateStr)
                    } else {
                        cumulativeSessionFocusSeconds + stopwatchSeconds
                    }
                } else {
                    0
                }
                completedSecs + pendingSecs + activeSecs
            }

            // Friends Focus Details Table / Leaderboard
            FriendsFocusLeaderboardTable(
                viewModel = viewModel,
                selectedDateStr = selectedDateStr,
                myTodaySeconds = myTodaySeconds
            )

            // Session Logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
                border = BorderStroke(1.dp, Color(0xFF222222)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Session Log history", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("${focusRecords.size} sessions", color = Color.Gray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        focusRecords.forEachIndexed { index, record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Bullet
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(WaterBlue)
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = record.taskTitle,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (record.tag.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(WaterBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, WaterBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            ) {
                                                Text(
                                                    text = record.tag,
                                                    color = WaterBlue,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Start - End timestamps
                                    Text(
                                        text = "${record.startTime} - ${record.endTime}",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }

                                // Duration pill badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF222222))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${record.durationMinutes} min",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Pencil edit icon
                                IconButton(
                                    onClick = {
                                        editingLogId = record.id
                                        editTaskTitle = record.taskTitle
                                        editStartTime = record.startTime
                                        editEndTime = record.endTime
                                        editDurationMins = record.durationMinutes.toString()
                                        editDateString = record.dateString
                                        editNotes = record.notes
                                        editTag = record.tag
                                        showEditLogDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Log",
                                        tint = WaterBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to Edit Focus Session Details
    if (showEditLogDialog && editingLogId != null) {
        AlertDialog(
            onDismissRequest = { showEditLogDialog = false },
            title = {
                Text(
                    "Edit Focus Session",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            containerColor = Color(0xFF161616),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Modify the details for this focus history session:", color = Color.Gray, fontSize = 11.sp)

                    Text("Task / Tag Title", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editTaskTitle,
                        onValueChange = { editTaskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = WaterBlue,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedContainerColor = Color(0xFF0F0F0F),
                            unfocusedContainerColor = Color(0xFF0F0F0F)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Time", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editStartTime,
                                onValueChange = { editStartTime = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF444444),
                                    focusedContainerColor = Color(0xFF0F0F0F),
                                    unfocusedContainerColor = Color(0xFF0F0F0F)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Time", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editEndTime,
                                onValueChange = { editEndTime = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF444444),
                                    focusedContainerColor = Color(0xFF0F0F0F),
                                    unfocusedContainerColor = Color(0xFF0F0F0F)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Duration (Mins)", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editDurationMins,
                                onValueChange = { editDurationMins = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF444444),
                                    focusedContainerColor = Color(0xFF0F0F0F),
                                    unfocusedContainerColor = Color(0xFF0F0F0F)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date (yyyy-MM-dd)", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editDateString,
                                onValueChange = { editDateString = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF444444),
                                    focusedContainerColor = Color(0xFF0F0F0F),
                                    unfocusedContainerColor = Color(0xFF0F0F0F)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Text("Tag / Category", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editTag,
                        onValueChange = { editTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = WaterBlue,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedContainerColor = Color(0xFF0F0F0F),
                            unfocusedContainerColor = Color(0xFF0F0F0F)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Notes", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = WaterBlue,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedContainerColor = Color(0xFF0F0F0F),
                            unfocusedContainerColor = Color(0xFF0F0F0F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minsParsed = editDurationMins.trim().toIntOrNull() ?: 0
                        val updated = FocusRecord(
                            startTime = editStartTime.trim(),
                            endTime = editEndTime.trim(),
                            taskTitle = editTaskTitle.trim(),
                            durationMinutes = minsParsed,
                            dateString = editDateString.trim(),
                            notes = editNotes.trim(),
                            durationSeconds = minsParsed * 60,
                            tag = editTag.trim(),
                            id = editingLogId!!
                        )
                        viewModel.updateFocusRecordById(editingLogId!!, updated)
                        showEditLogDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteFocusRecordById(editingLogId!!)
                            showEditLogDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showEditLogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222), contentColor = Color.LightGray)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
