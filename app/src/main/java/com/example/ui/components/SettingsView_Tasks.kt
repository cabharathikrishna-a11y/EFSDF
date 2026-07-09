package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SettingsTasksPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    SettingsPageScope {
        val taskVibrationEnabled by viewModel.taskVibrationEnabled.collectAsState()
        val taskSilentModeEnabled by viewModel.taskSilentModeEnabled.collectAsState()
        val allDayNotificationEnabled by viewModel.allDayNotificationEnabled.collectAsState()
        val allDayNotificationTime by viewModel.allDayNotificationTime.collectAsState()
        val taskHighNotif by viewModel.taskHighNotifEnabled.collectAsState()
        val taskHighDisplay by viewModel.taskHighDisplayEnabled.collectAsState()
        val taskMediumNotif by viewModel.taskMediumNotifEnabled.collectAsState()
        val taskMediumDisplay by viewModel.taskMediumDisplayEnabled.collectAsState()
        val taskLowNotif by viewModel.taskLowNotifEnabled.collectAsState()
        val taskLowDisplay by viewModel.taskLowDisplayEnabled.collectAsState()
        val taskHighAlarmSound by viewModel.taskHighAlarmSoundEnabled.collectAsState()
        val taskMediumAlarmSound by viewModel.taskMediumAlarmSoundEnabled.collectAsState()
        val taskLowAlarmSound by viewModel.taskLowAlarmSoundEnabled.collectAsState()
        val additionalReminderTimes by viewModel.additionalReminderTimes.collectAsState()

        // Tasks Subpage
        SettingsSubpageWorkspace(
            title = "Tasks Settings",
            description = "Configure custom reminders, vibrations, priority schedules.",
            onBack = onBack
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Task Reminder Vibration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Vibrate device during custom relative reminder schedules.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = taskVibrationEnabled,
                    onCheckedChange = { viewModel.updateTaskVibrationEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("task_reminder_vibrate_switch")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Silent Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Displays on-screen but has no sound / vibration.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = taskSilentModeEnabled,
                    onCheckedChange = { viewModel.updateTaskSilentModeEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("task_reminder_silent_switch")
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Additional Reminder Offsets", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Define comma-separated relative offsets in minutes inside schedules (ex: 5, 15, 30):", color = Color.Gray, fontSize = 10.sp)
                OutlinedTextField(
                    value = additionalReminderTimes,
                    onValueChange = { viewModel.updateAdditionalReminderTimes(it) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    placeholder = { Text("e.g. 5, 15, 30", color = Color.DarkGray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("additional_reminders_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Priority Notification Settings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // High Priority
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Priority Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskHighNotif, onCheckedChange = { viewModel.updateTaskHighNotifEnabled(it) })
                                    Text("Notifications", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskHighDisplay, onCheckedChange = { viewModel.updateTaskHighDisplayEnabled(it) })
                                    Text("On-Screen", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskHighAlarmSound, onCheckedChange = { viewModel.updateTaskHighAlarmSoundEnabled(it) })
                                    Text("Alarm Sound", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Medium Priority
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Medium Priority Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskMediumNotif, onCheckedChange = { viewModel.updateTaskMediumNotifEnabled(it) })
                                    Text("Notifications", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskMediumDisplay, onCheckedChange = { viewModel.updateTaskMediumDisplayEnabled(it) })
                                    Text("On-Screen", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskMediumAlarmSound, onCheckedChange = { viewModel.updateTaskMediumAlarmSoundEnabled(it) })
                                    Text("Alarm Sound", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Low/No Priority Notification settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("No / Low Priority Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskLowNotif, onCheckedChange = { viewModel.updateTaskLowNotifEnabled(it) })
                                    Text("Notifications", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskLowDisplay, onCheckedChange = { viewModel.updateTaskLowDisplayEnabled(it) })
                                    Text("On-Screen", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = taskLowAlarmSound, onCheckedChange = { viewModel.updateTaskLowAlarmSoundEnabled(it) })
                                    Text("Alarm Sound", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("All-Day Tasks Alerts", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pending All-Day Tasks Alert", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sends a notification showing how many all-day tasks are pending.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = allDayNotificationEnabled,
                                onCheckedChange = { viewModel.updateAllDayNotificationEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = WaterBlue),
                                modifier = Modifier.testTag("all_day_notification_switch")
                            )
                        }
                        
                        if (allDayNotificationEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Alert Trigger Time", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = allDayNotificationTime,
                                onValueChange = { viewModel.updateAllDayNotificationTime(it) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                placeholder = { Text("e.g. 09:00 AM or 18:30", color = Color.DarkGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("all_day_notification_time_input")
                            )
                        }
                    }
                }
            }
        }
    }
}
