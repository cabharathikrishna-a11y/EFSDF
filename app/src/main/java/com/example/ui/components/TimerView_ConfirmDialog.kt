package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Task
import com.example.ui.AppViewModel
import com.example.ui.theme.WaterBlue
import kotlinx.coroutines.delay

@Composable
fun TimerConfirmDialogController(
    viewModel: AppViewModel,
    focusTimerDurationMins: Int,
    selectedTask: Task?,
    sessionStartTimestamp: Long?,
    onSessionStartTimestampChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val showElapsedTimeDialog by viewModel.showElapsedTimeDialog.collectAsStateWithLifecycle()
    val editHoursInput by viewModel.editHoursInput.collectAsStateWithLifecycle()
    val editMinutesInput by viewModel.editMinutesInput.collectAsStateWithLifecycle()
    val editSecondsInput by viewModel.editSecondsInput.collectAsStateWithLifecycle()
    val stopSessionType by viewModel.stopSessionType.collectAsStateWithLifecycle()
    val stoppedElapsedSeconds by viewModel.stoppedElapsedSeconds.collectAsStateWithLifecycle()
    val focusNotesInput by viewModel.focusNotesInput.collectAsStateWithLifecycle()
    val pendingFocusReview by viewModel.pendingFocusReview.collectAsStateWithLifecycle()

    var originalAutoSavedSeconds by remember { mutableStateOf(0) }
    var originalAutoSavedMinutes by remember { mutableStateOf(0) }
    var originalAutoSavedTask by remember { mutableStateOf<Task?>(null) }
    var isAutoSavedSessionActive by remember { mutableStateOf(false) }
    var autoSavedRecordId by remember { mutableStateOf<String?>(null) }

    // Centralized pending focus review effect
    LaunchedEffect(pendingFocusReview) {
        val review = pendingFocusReview
        if (review != null && !showElapsedTimeDialog) {
            val rSeconds = review.durationSeconds
            viewModel.setEditHoursInput(rSeconds / 3600)
            viewModel.setEditMinutesInput((rSeconds % 3600) / 60)
            viewModel.setEditSecondsInput(rSeconds % 60)
            viewModel.setStopSessionType("timer") 
            viewModel.setStoppedElapsedSeconds(rSeconds)
            
            onSessionStartTimestampChange(com.example.util.StableTime.currentTimeMillis() - (rSeconds * 1000L))
            
            isAutoSavedSessionActive = true
            autoSavedRecordId = review.id
            originalAutoSavedSeconds = rSeconds
            originalAutoSavedMinutes = maxOf(1, (rSeconds + 30) / 60)
            originalAutoSavedTask = selectedTask

            viewModel.setShowElapsedTimeDialog(true)
            viewModel.setTimerImmersive(false)
            viewModel.clearPendingFocusReview()
        }
    }

    // Centralized auto-save effect
    LaunchedEffect(showElapsedTimeDialog, stoppedElapsedSeconds) {
        if (showElapsedTimeDialog) {
            val totalSeconds = stoppedElapsedSeconds
            if (totalSeconds > 0 && !isAutoSavedSessionActive) {
                val finalMinutes = if (totalSeconds > 0) maxOf(1, (totalSeconds + 30) / 60) else 0
                viewModel.addFocusMinutes(finalMinutes)
                
                if (stopSessionType == "timer" && finalMinutes >= focusTimerDurationMins) {
                    viewModel.incrementTodayPomos()
                }

                val formatter = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault())
                val startStr = sessionStartTimestamp?.let { formatter.format(java.util.Date(it)) }
                    ?: formatter.format(java.util.Date(System.currentTimeMillis() - totalSeconds * 1000L))
                val endStr = formatter.format(java.util.Date())
                val taskName = selectedTask?.title ?: "Focus Session"

                val record = viewModel.addFocusRecord(startStr, endStr, taskName, finalMinutes, focusNotesInput.trim(), totalSeconds, tag = viewModel.attachedTag.value)
                autoSavedRecordId = record.id

                if (selectedTask != null) {
                    val updated = selectedTask.copy(actualMinutes = selectedTask.actualMinutes + finalMinutes)
                    viewModel.updateTask(updated)
                    viewModel.attachTaskToTimer(updated)
                    originalAutoSavedTask = selectedTask
                } else {
                    originalAutoSavedTask = null
                }

                originalAutoSavedSeconds = totalSeconds
                originalAutoSavedMinutes = finalMinutes
                isAutoSavedSessionActive = true
            }
        }
    }

    if (showElapsedTimeDialog) {
        fun discardElapsedTimeSession() {
            if (isAutoSavedSessionActive) {
                val recordId = autoSavedRecordId
                if (recordId != null) {
                    val records = viewModel.focusRecords.value
                    val originalRecord = records.find { it.id == recordId }
                    if (originalRecord != null) {
                        val durationMinutes = originalRecord.durationMinutes
                        
                        // 1. Subtract the focus minutes
                        viewModel.addFocusMinutes(-durationMinutes)
                        
                        // 2. Subtract task minutes if any task was attached
                        val task = originalAutoSavedTask
                        if (task != null) {
                            val updated = task.copy(actualMinutes = maxOf(0, task.actualMinutes - durationMinutes))
                            viewModel.updateTask(updated)
                            viewModel.attachTaskToTimer(updated)
                        }
                        
                        // 3. Revert Pomodoro count if applicable
                        if (stopSessionType == "timer" && durationMinutes >= focusTimerDurationMins) {
                            viewModel.decrementTodayPomos()
                        }
                        
                        // 4. Delete the record by ID
                        viewModel.deleteFocusRecordById(recordId)
                    }
                }
            }

            com.example.util.FocusTimerManager.recordSessionCompleteOrReset(isSaving = false)

            if (stopSessionType == "timer") {
                viewModel.resetTimer(saveSession = false)
            } else {
                viewModel.resetStopwatch(saveSession = false)
            }
            viewModel.clearPendingFocusReview()
            onSessionStartTimestampChange(null)
            viewModel.setShowElapsedTimeDialog(false)
            viewModel.setFocusNotesInput("")
            viewModel.setTimerImmersive(false)

            isAutoSavedSessionActive = false
            autoSavedRecordId = null
            originalAutoSavedSeconds = 0
            originalAutoSavedMinutes = 0
            originalAutoSavedTask = null
        }

        fun saveAndCloseElapsedTimeSession() {
            val totalSeconds = editHoursInput * 3600 + editMinutesInput * 60 + editSecondsInput
            val finalMinutes = if (totalSeconds > 0) maxOf(1, (totalSeconds + 30) / 60) else 0

            if (isAutoSavedSessionActive) {
                val recordId = autoSavedRecordId
                if (recordId != null) {
                    val records = viewModel.focusRecords.value
                    val originalRecord = records.find { it.id == recordId }
                    if (originalRecord != null) {
                        val updatedRecord = originalRecord.copy(
                            durationMinutes = finalMinutes,
                            durationSeconds = totalSeconds,
                            notes = focusNotesInput.trim()
                        )
                        viewModel.updateFocusRecordById(recordId, updatedRecord)
                        
                        val diffMinutes = finalMinutes - originalAutoSavedMinutes
                        if (diffMinutes != 0) {
                            viewModel.addFocusMinutes(diffMinutes)
                        }
                        
                        val task = originalAutoSavedTask
                        if (task != null) {
                            val updated = task.copy(actualMinutes = task.actualMinutes + diffMinutes)
                            viewModel.updateTask(updated)
                            viewModel.attachTaskToTimer(updated)
                        }
                        
                        if (stopSessionType == "timer") {
                            val wasPomo = originalAutoSavedMinutes >= focusTimerDurationMins
                            val isPomo = finalMinutes >= focusTimerDurationMins
                            if (!wasPomo && isPomo) {
                                viewModel.incrementTodayPomos()
                            }
                        }
                    }
                }
            } else {
                if (totalSeconds > 0) {
                    viewModel.addFocusMinutes(finalMinutes)
                    if (stopSessionType == "timer" && finalMinutes >= focusTimerDurationMins) {
                        viewModel.incrementTodayPomos()
                    }
                    val formatter = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault())
                    val startStr = sessionStartTimestamp?.let { formatter.format(java.util.Date(it)) }
                        ?: formatter.format(java.util.Date(System.currentTimeMillis() - totalSeconds * 1000L))
                    val endStr = formatter.format(java.util.Date())
                    val taskName = selectedTask?.title ?: "Focus Session"

                    viewModel.addFocusRecord(startStr, endStr, taskName, finalMinutes, focusNotesInput.trim(), totalSeconds, tag = viewModel.attachedTag.value)

                    if (selectedTask != null) {
                        val updated = selectedTask.copy(actualMinutes = selectedTask.actualMinutes + finalMinutes)
                        viewModel.updateTask(updated)
                        viewModel.attachTaskToTimer(updated)
                    }
                }
            }

            // Preserve start time and pause ranges before wiping out current session tracking
            com.example.util.FocusTimerManager.recordSessionCompleteOrReset(isSaving = true)

            if (stopSessionType == "timer") {
                viewModel.resetTimer(saveSession = false)
            } else {
                viewModel.resetStopwatch(saveSession = false)
            }
            viewModel.clearPendingFocusReview()
            onSessionStartTimestampChange(null)
            viewModel.setShowElapsedTimeDialog(false)
            viewModel.setFocusNotesInput("")
            viewModel.setTimerImmersive(false)

            isAutoSavedSessionActive = false
            autoSavedRecordId = null
            originalAutoSavedSeconds = 0
            originalAutoSavedMinutes = 0
            originalAutoSavedTask = null

            com.example.util.FocusTimerManager.setGlobalVerificationFocusedTimeSeconds(totalSeconds)
            com.example.util.FocusTimerManager.setGlobalVerificationRevisedTotalMinutes(com.example.util.FocusTimerManager.getTodayFocusMinutes())
            com.example.util.FocusTimerManager.setGlobalVerificationRevisedTotalSeconds(com.example.util.FocusTimerManager.getTodayFocusSeconds())
            if (com.example.util.FocusTimerManager.verifiedSessionStartMs.value == null) {
                com.example.util.FocusTimerManager.setVerifiedSessionStartMs(System.currentTimeMillis() - totalSeconds * 1000L)
            }
            com.example.util.FocusTimerManager.setShowGlobalVerificationDialog(true)
        }

        Dialog(onDismissRequest = { saveAndCloseElapsedTimeSession() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirmation Needed",
                        tint = WaterBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Confirm Focused Time",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val formattedDurationString = if (stoppedElapsedSeconds >= 3600) {
                        "${stoppedElapsedSeconds / 3600}h ${(stoppedElapsedSeconds % 3600) / 60}m ${stoppedElapsedSeconds % 60}s"
                    } else if (stoppedElapsedSeconds >= 60) {
                        "${stoppedElapsedSeconds / 60}m ${stoppedElapsedSeconds % 60}s"
                    } else {
                        "${stoppedElapsedSeconds}s"
                    }
                    Text(
                        text = "Total Session Focus: $formattedDurationString",
                        color = WaterBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Do you confirm you focused for this much time?",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Hours Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hours", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = { viewModel.setEditHoursInput(editHoursInput + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = Color.White)
                            }
                            Text(
                                text = "$editHoursInput",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { if (editHoursInput > 0) viewModel.setEditHoursInput(editHoursInput - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Colon 1
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(50.dp))
                            Text(":", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Minutes Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minutes", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = { if (editMinutesInput < 59) viewModel.setEditMinutesInput(editMinutesInput + 1) else viewModel.setEditMinutesInput(0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = Color.White)
                            }
                            Text(
                                text = String.format("%02d", editMinutesInput),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { if (editMinutesInput > 0) viewModel.setEditMinutesInput(editMinutesInput - 1) else viewModel.setEditMinutesInput(59) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Colon 2
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(50.dp))
                            Text(":", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Seconds Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Seconds", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = { if (editSecondsInput < 59) viewModel.setEditSecondsInput(editSecondsInput + 1) else viewModel.setEditSecondsInput(0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = Color.White)
                            }
                            Text(
                                text = String.format("%02d", editSecondsInput),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { if (editSecondsInput > 0) viewModel.setEditSecondsInput(editSecondsInput - 1) else viewModel.setEditSecondsInput(59) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = focusNotesInput,
                        onValueChange = { viewModel.setFocusNotesInput(it) },
                        label = { Text("What did you focus on?", fontSize = 10.sp) },
                        placeholder = { Text("List tasks, thoughts, or reflections here...", fontSize = 12.sp, color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedBorderColor = WaterBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { discardElapsedTimeSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { saveAndCloseElapsedTimeSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Yes, Record", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
