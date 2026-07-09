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
fun SettingsHabitsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    SettingsPageScope {
        val habitSilentModeEnabled by viewModel.habitSilentModeEnabled.collectAsState()
        val habitOnScreen by viewModel.habitOnScreenReminderEnabled.collectAsState()
        val habitNotif by viewModel.habitNotifReminderEnabled.collectAsState()

        SettingsSubpageWorkspace(
            title = "Habits Tracker Settings",
            description = "Configure streaks, reminders and habit intervals.",
            onBack = onBack
        ) {
            Text("Streak recorders calculate active records continuously. Midnight resets evaluate complete boxes.", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Habit Notification Core", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("On-Screen Reminders", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Display reminders in application dashboard", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = habitOnScreen,
                            onCheckedChange = { viewModel.updateHabitOnScreenReminderEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f))
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System Notifications", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Send push notifications at scheduled times", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = habitNotif,
                            onCheckedChange = { viewModel.updateHabitNotifReminderEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f))
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Silent Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Displays on-screen but has no sound / vibration.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = habitSilentModeEnabled,
                            onCheckedChange = { viewModel.updateHabitSilentModeEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("habit_silent_mode_switch")
                        )
                    }
                }
            }
        }
    }
}
