package com.example.ui.components

import android.widget.Toast
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsTimerConfigurationPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    SettingsPageScope {
        val focusTimerDurationMins by viewModel.focusTimerDurationMins.collectAsState()
        val breakDurationMins by viewModel.breakDurationMins.collectAsState()
        val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
        val showOverlayEnabled by viewModel.showOverlayEnabled.collectAsState()
        val floatingTimerSize by viewModel.floatingTimerSize.collectAsState()
        val keepNotificationEnabled by viewModel.keepNotificationEnabled.collectAsState()
        val dailyFocusHoursTarget by viewModel.dailyFocusHoursTarget.collectAsState()
        val focusMotivationalQuoteEnabled by viewModel.focusMotivationalQuoteEnabled.collectAsState()
        val focusMotivationalQuoteIntervalMins by viewModel.focusMotivationalQuoteIntervalMins.collectAsState()

        val timerSoundEnabled by viewModel.timerSoundEnabled.collectAsState()
        val timerAutoStartBreak by viewModel.timerAutoStartBreak.collectAsState()
        val timerAutoStartPomo by viewModel.timerAutoStartPomo.collectAsState()
        val stopwatchBreakDurationMinutes by viewModel.stopwatchBreakDurationMinutes.collectAsState()
        val autoStartStopwatchAfterBreak by viewModel.autoStartStopwatchAfterBreak.collectAsState()
        val antiBurnScreenEnabled by viewModel.antiBurnScreenEnabled.collectAsState()
        val batterySaverModeEnabled by viewModel.batterySaverModeEnabled.collectAsState()
        val shareFocusDetailsEnabled by viewModel.shareFocusDetailsEnabled.collectAsState()
        val shareFocusHistoryEnabled by viewModel.shareFocusHistoryEnabled.collectAsState()

        val isTimerRunning by viewModel.isTimerRunning.collectAsState()
        val isStopwatchActive by viewModel.isStopwatchActive.collectAsState()
        val isFocusPhase by viewModel.isFocusPhase.collectAsState()
        val isStrictBlocked = (isTimerRunning && isFocusPhase) || isStopwatchActive

        // Timer Configuration Page
        SettingsSubpageWorkspace(
            title = "Timer Configuration",
            description = "Configure Pomodoro sessions and custom indicators.",
            onBack = onBack
        ) {
            var focusInputText by remember(focusTimerDurationMins) { mutableStateOf(focusTimerDurationMins.toString()) }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Focus Session Duration (mins)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = focusInputText,
                    onValueChange = { newValue ->
                        focusInputText = newValue
                        val parsed = newValue.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.updateTimerDuration(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_duration_input")
                )
            }

            var breakInputText by remember(breakDurationMins) { mutableStateOf(breakDurationMins.toString()) }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Default Rest Break Period (mins)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = breakInputText,
                    onValueChange = { newValue ->
                        breakInputText = newValue
                        val parsed = newValue.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.updateBreakDuration(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("break_duration_input")
                )
            }

            var stopwatchBreakInputText by remember(stopwatchBreakDurationMinutes) { mutableStateOf(stopwatchBreakDurationMinutes.toString()) }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Stopwatch Rest Break Period (mins)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = stopwatchBreakInputText,
                    onValueChange = { newValue ->
                        stopwatchBreakInputText = newValue
                        val parsed = newValue.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.setStopwatchBreakDuration(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stopwatch_break_duration_input")
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
                Column {
                    Text("Tactile Vibration Alerts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Vibrate device when timer completes", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.updateVibrationEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("timer_vibration_switch")
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
                    Text("Anti-Burn Screen Protection", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Slowly shifts digits to protect active AMOLED/OLED displays.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = antiBurnScreenEnabled,
                    onCheckedChange = { viewModel.updateAntiBurnScreenEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("anti_burn_switch")
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
                    Text("Battery Saver Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Disables background sync/alerts on close. Resumes timer precisely on app launch.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = batterySaverModeEnabled,
                    onCheckedChange = { viewModel.updateBatterySaverModeEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("battery_saver_switch")
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
                    Text("Share Focus Details", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Share active focus details with friends on the cloud.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = shareFocusDetailsEnabled,
                    onCheckedChange = { viewModel.updateShareFocusDetailsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("share_focus_details_switch")
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
                    Text("Share Focus Session History", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Share focus history records to Firebase. Only focus time is shared if off.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = shareFocusHistoryEnabled,
                    onCheckedChange = { viewModel.updateShareFocusHistoryEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("share_focus_history_switch")
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
                    Text("Motivational Quote Overlay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Display study, money, and life change quotes in full-screen mode.", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = focusMotivationalQuoteEnabled,
                    onCheckedChange = { viewModel.updateFocusMotivationalQuoteEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("focus_motivational_quote_switch")
                )
            }

            if (focusMotivationalQuoteEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quote Rotation Interval", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Specify interval for rotating the motivational quote (minutes).", color = Color.Gray, fontSize = 10.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (focusMotivationalQuoteIntervalMins > 1) {
                                    viewModel.updateFocusMotivationalQuoteIntervalMins(focusMotivationalQuoteIntervalMins - 1)
                                }
                            },
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1F1F1F))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Decrease Interval",
                                tint = if (focusMotivationalQuoteIntervalMins > 1) Color.White else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$focusMotivationalQuoteIntervalMins min",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                if (focusMotivationalQuoteIntervalMins < 60) {
                                    viewModel.updateFocusMotivationalQuoteIntervalMins(focusMotivationalQuoteIntervalMins + 1)
                                }
                            },
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1F1F1F))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Increase Interval",
                                tint = if (focusMotivationalQuoteIntervalMins < 60) Color.White else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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
                    Text("Daily Focus Target (hours)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Target daily focus hours logged (1–20 hours).", color = Color.Gray, fontSize = 10.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (dailyFocusHoursTarget > 1) {
                                viewModel.updateDailyFocusHoursTarget(dailyFocusHoursTarget - 1)
                            }
                        },
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1F1F1F))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Decrease Focus Target",
                            tint = if (dailyFocusHoursTarget > 1) Color.White else Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$dailyFocusHoursTarget",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = {
                            if (dailyFocusHoursTarget < 20) {
                                viewModel.updateDailyFocusHoursTarget(dailyFocusHoursTarget + 1)
                            }
                        },
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1F1F1F))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Increase Focus Target",
                            tint = if (dailyFocusHoursTarget < 20) Color.White else Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
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
                    Text("Display Overlay Widget", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Floating widget when leaving app", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = showOverlayEnabled,
                    onCheckedChange = { viewModel.updateShowOverlayEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("overlay_switch")
                )
            }

            if (showOverlayEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C0C0C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Floating Timer Size", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Choose size (Small, Medium, Large) for the floating widget", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("small", "medium", "large").forEach { size ->
                            val isSelected = floatingTimerSize == size
                            val label = size.replaceFirstChar { it.uppercase() }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) WaterBlue else Color(0xFF1E1E1F),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.updateFloatingTimerSize(size) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
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
                    Text("Persistent Notification", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Status bar/lockscreen visibility helper", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = keepNotificationEnabled,
                    onCheckedChange = { viewModel.updateKeepNotificationEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("notification_switch")
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
                    Text("Timer End Sound Alerts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Play high-quality alerting sound when segments end", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = timerSoundEnabled,
                    onCheckedChange = { viewModel.setTimerSoundEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("timer_sound_switch")
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
                    Text("Auto Start Break After Focus", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Instantly trigger rest period when study focus timer completes", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = timerAutoStartBreak,
                    onCheckedChange = { viewModel.setTimerAutoStartBreak(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("timer_autostart_break_switch")
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
                    Text("Auto Start Focus After Break", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Instantly trigger next focus session when rest break completes", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = timerAutoStartPomo,
                    onCheckedChange = { viewModel.setTimerAutoStartPomo(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("timer_autostart_pomo_switch")
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
                    Text("Auto Start Stopwatch After Break", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Automatically resume stopwatch when stopwatch break completes", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = autoStartStopwatchAfterBreak,
                    onCheckedChange = { viewModel.setAutoStartStopwatchAfterBreak(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = WaterBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("stopwatch_autostart_after_break_switch")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

            val strictPrefs = remember { context.getSharedPreferences("strict_mode_prefs", android.content.Context.MODE_PRIVATE) }
            var strictModeEnabled by remember {
                mutableStateOf(strictPrefs.getBoolean("strict_mode_enabled", true))
            }
            var blockedApps by remember {
                mutableStateOf(strictPrefs.getStringSet("blocked_packages", emptySet()) ?: emptySet())
            }
            var searchAppQuery by remember { mutableStateOf("") }

            var hasPermissionState by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                while (true) {
                    hasPermissionState = com.example.util.AppBlockHelper.hasUsageStatsPermission(context)
                    kotlinx.coroutines.delay(2000)
                }
            }

            // FOCUS TAGS MANAGER
            val focusTags by viewModel.focusTags.collectAsState()
            var newTagInput by remember { mutableStateOf("") }
            var editingTagIndex by remember { mutableStateOf<Int?>(null) }
            var editingTagInput by remember { mutableStateOf("") }

            Text(
                text = "FOCUS TAGS MANAGER",
                color = WaterBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize your focus categories. These tags will be available for you to classify your focused Pomodoro or Stopwatch session blocks.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    // Add / Edit Row
                    if (editingTagIndex == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTagInput,
                                onValueChange = { newTagInput = it },
                                label = { Text("New Tag", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedContainerColor = Color(0xFF151515),
                                    unfocusedContainerColor = Color(0xFF151515)
                                ),
                                modifier = Modifier.weight(1f).height(52.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    val trimmed = newTagInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        viewModel.addFocusTag(trimmed)
                                        newTagInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editingTagInput,
                                onValueChange = { editingTagInput = it },
                                label = { Text("Edit Tag", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WaterBlue,
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedContainerColor = Color(0xFF151515),
                                    unfocusedContainerColor = Color(0xFF151515)
                                ),
                                modifier = Modifier.weight(1f).height(52.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    val trimmed = editingTagInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        viewModel.updateFocusTag(editingTagIndex!!, trimmed)
                                        editingTagIndex = null
                                        editingTagInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    editingTagIndex = null
                                    editingTagInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222), contentColor = Color.LightGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    }

                    // Tags List
                    if (focusTags.isEmpty()) {
                        Text("No custom tags added yet.", color = Color.DarkGray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            focusTags.forEachIndexed { idx, tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF151515), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tag, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingTagIndex = idx
                                                editingTagInput = tag
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Tag", tint = WaterBlue, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteFocusTag(idx)
                                                if (editingTagIndex == idx) {
                                                    editingTagIndex = null
                                                    editingTagInput = ""
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Tag", tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

            Text(
                text = "STRICT FOCUS SECURE GATE",
                color = WaterBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isStrictBlocked) {
                        Text(
                            text = "⚠️ Strict Mode settings are locked while the focus timer or stopwatch is running.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Strict Mode Trigger", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Intercept chosen apps when focus timer/stopwatch runs.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = strictModeEnabled,
                            enabled = !isStrictBlocked,
                            onCheckedChange = { isEnabled ->
                                if (isStrictBlocked) {
                                    Toast.makeText(context, "Cannot change strict mode settings while a timer or stopwatch is running!", Toast.LENGTH_SHORT).show()
                                } else {
                                    strictModeEnabled = isEnabled
                                    strictPrefs.edit().putBoolean("strict_mode_enabled", isEnabled).apply()
                                    if (isEnabled && !hasPermissionState) {
                                        Toast.makeText(context, "Usage Permission is required for Strict Mode!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = WaterBlue,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray,
                                disabledCheckedTrackColor = WaterBlue.copy(alpha = 0.5f),
                                disabledUncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (strictModeEnabled) {
                        if (!hasPermissionState) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open settings automatically. Please search Settings for Usage Access.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Authorize Usage Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = searchAppQuery,
                            onValueChange = { searchAppQuery = it },
                            enabled = !isStrictBlocked,
                            placeholder = { Text("Filter package or name...", color = Color.DarkGray, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = Color(0xFF222222),
                                disabledTextColor = Color.LightGray.copy(alpha = 0.5f),
                                disabledBorderColor = Color(0xFF222222).copy(alpha = 0.5f)
                            )
                        )

                        val launchableAppsList = remember {
                            try {
                                val pm = context.packageManager
                                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                                    addCategory(Intent.CATEGORY_LAUNCHER)
                                }
                                pm.queryIntentActivities(intent, 0)
                                    .map { resolveInfo ->
                                        val label = resolveInfo.loadLabel(pm).toString()
                                        val pkg = resolveInfo.activityInfo.packageName
                                        Pair(label, pkg)
                                    }
                                    .distinctBy { it.second }
                                    .filter { it.second != context.packageName }
                                    .sortedBy { it.first.lowercase() }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }

                        val filteredList = launchableAppsList.filter {
                            it.first.contains(searchAppQuery, ignoreCase = true) || 
                            it.second.contains(searchAppQuery, ignoreCase = true)
                        }

                        Text(
                            text = "BLOCK LIST (${blockedApps.size} apps selected)",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color.Black, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (filteredList.isEmpty()) {
                                    Text(
                                        text = "No packages match query",
                                        color = Color.DarkGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                } else {
                                    filteredList.forEach { (label, pkg) ->
                                        val isBlocked = blockedApps.contains(pkg)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = !isStrictBlocked) {
                                                    val updated = blockedApps.toMutableSet()
                                                    if (isBlocked) {
                                                        updated.remove(pkg)
                                                    } else {
                                                        updated.add(pkg)
                                                    }
                                                    blockedApps = updated
                                                    strictPrefs.edit().putStringSet("blocked_packages", updated).apply()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                AppListIcon(pkg = pkg)
                                            }
                                            Checkbox(
                                                checked = isBlocked,
                                                enabled = !isStrictBlocked,
                                                onCheckedChange = { checked ->
                                                    if (!isStrictBlocked) {
                                                        val updated = blockedApps.toMutableSet()
                                                        if (checked == true) {
                                                            updated.add(pkg)
                                                        } else {
                                                            updated.remove(pkg)
                                                        }
                                                        blockedApps = updated
                                                        strictPrefs.edit().putStringSet("blocked_packages", updated).apply()
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = WaterBlue,
                                                    checkmarkColor = Color.Black,
                                                    uncheckedColor = Color.Gray
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
