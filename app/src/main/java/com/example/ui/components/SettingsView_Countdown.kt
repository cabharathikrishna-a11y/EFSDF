package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel

@Composable
fun SettingsCountdownAlertsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    SettingsPageScope {
        val context = LocalContext.current
        val sharedPrefs = remember { context.getSharedPreferences("countdown_settings_prefs", android.content.Context.MODE_PRIVATE) }

        var onScreenReminderEnabled by remember {
            mutableStateOf(sharedPrefs.getBoolean("on_screen_reminder", true))
        }
        var notificationReminderEnabled by remember {
            mutableStateOf(sharedPrefs.getBoolean("notification_reminder", true))
        }
        var countdownSilentModeEnabled by remember {
            mutableStateOf(sharedPrefs.getBoolean("countdown_silent_mode", false))
        }
        var countdownReminders by remember {
            mutableStateOf(
                run {
                    val jsonStr = sharedPrefs.getString("reminders_list_json", null)
                    if (!jsonStr.isNullOrEmpty()) {
                        try {
                            val arr = org.json.JSONArray(jsonStr)
                            val list = mutableListOf<CountdownReminder>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                list.add(
                                    CountdownReminder(
                                        daysBefore = obj.getInt("daysBefore"),
                                        timeString = obj.getString("timeString")
                                    )
                                )
                            }
                            list
                        } catch (e: Exception) {
                            mutableListOf(CountdownReminder(0, "09:00"))
                        }
                    } else {
                        mutableListOf(CountdownReminder(0, "09:00"))
                    }
                }
            )
        }

        fun saveCountdownSettings(onScreen: Boolean, notif: Boolean, silent: Boolean, list: List<CountdownReminder>) {
            val editor = sharedPrefs.edit()
            editor.putBoolean("on_screen_reminder", onScreen)
            editor.putBoolean("notification_reminder", notif)
            editor.putBoolean("countdown_silent_mode", silent)
            try {
                val arr = org.json.JSONArray()
                list.forEach { item ->
                    val obj = org.json.JSONObject()
                    obj.put("daysBefore", item.daysBefore)
                    obj.put("timeString", item.timeString)
                    arr.put(obj)
                }
                editor.putString("reminders_list_json", arr.toString())
            } catch (e: java.lang.Exception) {}
            editor.apply()
        }

        SettingsSubpageWorkspace(
            title = "Countdown Settings",
            description = "Configure custom reminder categories, schedules and systems.",
            onBack = onBack
        ) {
            // Warning Card description
            Text(
                text = "Warning cards calculate hours relative to deadlines and can trigger background alarms 24 hours prior.",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // On-Screen Reminders Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On-Screen Reminders", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Show reminders on application launch screen", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = onScreenReminderEnabled,
                        onCheckedChange = { onScreenReminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Reminders Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notification Reminders", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Send system-wide push notifications", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = notificationReminderEnabled,
                        onCheckedChange = { notificationReminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Silent Mode Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Silent Mode", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Displays on-screen but has no sound / vibration", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = countdownSilentModeEnabled,
                        onCheckedChange = { countdownSilentModeEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("countdown_silent_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminders Interval List Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("REMINDER INTERVAL SCHEDULES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Add customized schedules to run before target events", color = Color.Gray, fontSize = 10.sp)
                        }
                        IconButton(
                            onClick = {
                                countdownReminders = (countdownReminders + CountdownReminder(0, "09:00")).toMutableList()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WaterBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (countdownReminders.isEmpty()) {
                        Text(
                            "No active alerts configured.",
                            color = Color.DarkGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        countdownReminders.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Days Before input
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Days Before", color = Color.Gray, fontSize = 9.sp)
                                    OutlinedTextField(
                                        value = item.daysBefore.toString(),
                                        onValueChange = { newVal ->
                                            val days = newVal.toIntOrNull() ?: 0
                                            val newList = countdownReminders.toMutableList()
                                            newList[index] = newList[index].copy(daysBefore = days)
                                            countdownReminders = newList
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WaterBlue,
                                            unfocusedBorderColor = Color.DarkGray
                                        ),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    if (item.daysBefore == 0) {
                                        Text("0 = Event day", color = WaterBlue, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Time Input
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Time (HH:MM)", color = Color.Gray, fontSize = 9.sp)
                                    OutlinedTextField(
                                        value = item.timeString,
                                        onValueChange = { newVal ->
                                            val newList = countdownReminders.toMutableList()
                                            newList[index] = newList[index].copy(timeString = newVal)
                                            countdownReminders = newList
                                        },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WaterBlue,
                                            unfocusedBorderColor = Color.DarkGray
                                        ),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = {
                                        val newList = countdownReminders.toMutableList()
                                        newList.removeAt(index)
                                        countdownReminders = newList
                                    },
                                    modifier = Modifier.padding(top = 10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Reminder", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Global Save Button for Countdown Settings
            Button(
                onClick = {
                    // Persist to SharedPreferences and inform user
                    saveCountdownSettings(onScreenReminderEnabled, notificationReminderEnabled, countdownSilentModeEnabled, countdownReminders)
                    Toast.makeText(context, "Countdown Settings Saved!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_countdown_settings_btn")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("SAVE COUNTDOWN SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
