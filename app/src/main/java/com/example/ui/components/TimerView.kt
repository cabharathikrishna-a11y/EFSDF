package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.AppViewModel
import com.example.ui.theme.WaterBlue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerView(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }
    var isOverlayPermissionDismissed by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Navigation & Modal States
    val showHistoryScreen by viewModel.showHistoryScreen.collectAsStateWithLifecycle()
    var showFriendsFocusDetails by remember { mutableStateOf(false) }
    var selectedDateStr by remember { 
        mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) 
    }
    var showCalendarDialog by remember { mutableStateOf(false) }
    val showTaskSelectionDialog by viewModel.showTaskSelectionDialog.collectAsStateWithLifecycle()

    // Configuration and Dynamic States
    val focusTimerDurationMins by viewModel.focusTimerDurationMins.collectAsStateWithLifecycle()
    val isImmersive by viewModel.isTimerImmersive.collectAsStateWithLifecycle()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isTimerActive by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isFocusPhase by viewModel.isFocusPhase.collectAsStateWithLifecycle()
    val selectedTask by viewModel.attachedTask.collectAsStateWithLifecycle()
    val sessionStartTimestamp by viewModel.sessionStartTimestamp.collectAsStateWithLifecycle()
    val focusRecords by viewModel.focusRecords.collectAsStateWithLifecycle()
    val stopwatchSeconds by viewModel.stopwatchSeconds.collectAsStateWithLifecycle()
    val isStopwatchActive by viewModel.isStopwatchActive.collectAsStateWithLifecycle()
    val pendingFocusReview by viewModel.pendingFocusReview.collectAsStateWithLifecycle()
    val cumulativeSessionFocusSeconds by viewModel.cumulativeSessionFocusSeconds.collectAsStateWithLifecycle()

    // Milestone & Dialog States
    val focusRankPopup by viewModel.focusRankPopup.collectAsStateWithLifecycle()

    // Dynamically calculate focus metrics
    val completedTodaySecs = remember(focusRecords) {
        val systemTodayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        focusRecords.sumOf { com.example.util.FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) }
    }

    val pendingSecs = remember(pendingFocusReview) {
        val systemTodayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        pendingFocusReview?.let { com.example.util.FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) } ?: 0
    }

    val globalTodaySeconds = remember(completedTodaySecs, pendingSecs, isFocusPhase, cumulativeSessionFocusSeconds, stopwatchSeconds, pendingFocusReview, sessionStartTimestamp, isTimerActive, isStopwatchActive) {
        val systemTodayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val activeSecs = if (isFocusPhase && pendingFocusReview == null) {
            val startTs = sessionStartTimestamp
            if ((isTimerActive || isStopwatchActive) && startTs != null) {
                com.example.util.FocusTimerManager.getActiveSessionOverlapSeconds(startTs, systemTodayStr)
            } else {
                cumulativeSessionFocusSeconds + stopwatchSeconds
            }
        } else {
            0
        }
        completedTodaySecs + pendingSecs + activeSecs
    }

    // Display Custom Date Picker Mini Calendar Dialog
    if (showCalendarDialog) {
        MiniCalendarDialog(
            currentSelectedDateStr = selectedDateStr,
            onDateSelected = { date -> selectedDateStr = date },
            onDismissRequest = { showCalendarDialog = false }
        )
    }

    // Display Achievement rank achievements modal dialog
    focusRankPopup?.let { popupData ->
        FocusRankMilestoneDialog(
            viewModel = viewModel,
            popupData = popupData,
            onDismiss = { viewModel.dismissFocusRankPopup() }
        )
    }

    // Display Task Selection Dialog
    if (showTaskSelectionDialog) {
        TaskSelectionDialog(
            viewModel = viewModel,
            tasks = tasks,
            isTabFocusTimerSelected = viewModel.isTabFocusTimerSelected.value,
            sessionStartTimestamp = sessionStartTimestamp,
            onDismiss = { viewModel.setShowTaskSelectionDialog(false) }
        )
    }

    val showTagSelectionDialog by viewModel.showTagSelectionDialog.collectAsStateWithLifecycle()
    if (showTagSelectionDialog) {
        TagSelectionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowTagSelectionDialog(false) }
        )
    }

    // Display Friends Focus details list modal
    if (showFriendsFocusDetails) {
        FriendsFocusDetailsDialog(
            viewModel = viewModel,
            onDismiss = { showFriendsFocusDetails = false }
        )
    }

    // Centralized session timer confirm & auto-save controller
    TimerConfirmDialogController(
        viewModel = viewModel,
        focusTimerDurationMins = focusTimerDurationMins,
        selectedTask = selectedTask,
        sessionStartTimestamp = sessionStartTimestamp,
        onSessionStartTimestampChange = { viewModel.setSessionStartTimestamp(it) }
    )

    // Sync timer display seconds remaining with duration modification from Settings
    LaunchedEffect(focusTimerDurationMins) {
        if (!isTimerActive && isFocusPhase) {
            viewModel.setTimerDuration(focusTimerDurationMins)
        }
    }

    if (isImmersive) {
        TimerImmersiveContent(
            viewModel = viewModel,
            focusTimerDurationMins = focusTimerDurationMins,
            onShowFriendsDetails = { showFriendsFocusDetails = true }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(if (isTablet) 16.dp else 4.dp)
        ) {
            // Header Top Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showHistoryScreen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setShowHistoryScreen(false) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Timer",
                            tint = WaterBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back to Timer",
                            color = WaterBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { showCalendarDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF151515))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select focus date",
                            tint = WaterBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FriendsFocusPill(
                            viewModel = viewModel,
                            onClick = { showFriendsFocusDetails = true }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isBellSilent by viewModel.isBellSilentModeEnabled.collectAsStateWithLifecycle()
                        IconButton(
                            onClick = { viewModel.setBellSilentModeEnabled(!isBellSilent) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isBellSilent) Color(0xFFE53935) else Color(0xFF151515))
                                .size(32.dp)
                                .testTag("bell_silent_button")
                        ) {
                            Icon(
                                imageVector = if (isBellSilent) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                contentDescription = "Bell Silent Mode Toggle",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setTimerImmersive(true) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF151515))
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Enter Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setShowHistoryScreen(true) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF151515))
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Focus History Overview",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Draw system alert window drawing permission banner
            if (!hasOverlayPermission && !isOverlayPermissionDismissed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("overlay_permission_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Permission Info",
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overlay Widget Enabled",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Allow drawing over other apps to see a floating timer on the screen when minimized.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { isOverlayPermissionDismissed = true },
                            modifier = Modifier.size(28.dp).testTag("dismiss_overlay_permission")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "No I won't",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = showHistoryScreen,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState) width else -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> if (targetState) -width else width } + fadeOut()
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "history_transition"
            ) { targetHistory ->
                if (targetHistory) {
                    TimerHistoryView(
                        viewModel = viewModel,
                        selectedDateStr = selectedDateStr
                    )
                } else {
                    TimerLiveControlContent(
                        viewModel = viewModel,
                        isTablet = isTablet,
                        isImmersive = false,
                        isAntiBurnCenteredByTap = true,
                        globalTodaySeconds = globalTodaySeconds,
                        focusTimerDurationMins = focusTimerDurationMins
                    )
                }
            }
        }
    }
}
