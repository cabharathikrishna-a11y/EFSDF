package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.AppViewModel
import com.example.util.AppBlockHelper
import com.example.util.GoogleDriveSyncManager
import com.example.util.GoogleContactsSyncManager
import com.example.util.GoogleTasksSyncManager
import com.example.util.GoogleFitSyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsPermissionsPage(viewModel: AppViewModel, onBack: () -> Unit) {
    SettingsSubpageWorkspace(
        title = "Permissions & API Connections",
        description = "Manage system permissions and external API authorizations.",
        onBack = onBack
    ) {
        PermissionsSettingsSection(viewModel = viewModel)
    }
}

fun hasGoogleScope(context: Context, scopeUri: String): Boolean {
    val scope = com.google.android.gms.common.api.Scope(scopeUri)
    val account = GoogleSignIn.getLastSignedInAccount(context)
    return account != null && GoogleSignIn.hasPermissions(account, scope)
}

@Composable
fun PermissionsSettingsSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    var isBatteryOptIgnored by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasUsageStatsPermission by remember { mutableStateOf(false) }
    
    var hasSystemCalendarPermission by remember { mutableStateOf(false) }
    var hasSystemContactsPermission by remember { mutableStateOf(false) }

    var hasDrivePermission by remember { mutableStateOf(false) }
    var hasGoogleContactsPermission by remember { mutableStateOf(false) }
    var hasGoogleTasksPermission by remember { mutableStateOf(false) }
    var hasGoogleFitPermission by remember { mutableStateOf(false) }
    
    var hasExactAlarmPermission by remember { mutableStateOf(false) }
    var hasNotificationListenerPermission by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val checkAllPermissions = {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryOptIgnored = pm.isIgnoringBatteryOptimizations(context.packageName)

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasOverlayPermission = Settings.canDrawOverlays(context)
        hasUsageStatsPermission = AppBlockHelper.hasUsageStatsPermission(context)
        
        hasSystemCalendarPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                                      ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                                      
        hasSystemContactsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                                      ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED

        hasDrivePermission = GoogleDriveSyncManager.hasDrivePermission(context)
        hasGoogleContactsPermission = hasGoogleScope(context, "https://www.googleapis.com/auth/contacts")
        hasGoogleTasksPermission = hasGoogleScope(context, "https://www.googleapis.com/auth/tasks")
        hasGoogleFitPermission = GoogleFitSyncManager.hasFitPermission(context)

        hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        val cn = android.content.ComponentName(context, com.example.service.NotificationBlockerService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        hasNotificationListenerPermission = flat != null && flat.contains(cn.flattenToString())
    }

    LaunchedEffect(Unit) {
        while (true) {
            checkAllPermissions()
            delay(1000)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasNotificationPermission = granted
        }
    )
    
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { _ -> checkAllPermissions() }
    )

    val authResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkAllPermissions()
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PermissionItem(
            title = "Notifications",
            description = "Required for focus timer alerts and reminders.",
            isGranted = hasNotificationPermission,
            onClick = {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (!hasNotificationPermission) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            }
        )

        PermissionItem(
            title = "Notification Blocker (Notification Access)",
            description = "Required to block and release incoming notifications during Focus phase.",
            isGranted = hasNotificationListenerPermission,
            onClick = {
                if (!hasNotificationListenerPermission) {
                    try {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("SettingsView", "Error starting notification listener settings: ${e.message}")
                    }
                }
            }
        )

        PermissionItem(
            title = "Battery Optimization",
            description = "Ignore battery optimization to keep timers running in background.",
            isGranted = isBatteryOptIgnored,
            onClick = {
                if (!isBatteryOptIgnored) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        PermissionItem(
            title = "Exact Alarms",
            description = "Required for precise timer wakeups on Android 12+.",
            isGranted = hasExactAlarmPermission,
            onClick = {
                if (!hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        PermissionItem(
            title = "Display Over Other Apps",
            description = "Required to show strict mode blocking overlays.",
            isGranted = hasOverlayPermission,
            onClick = {
                if (!hasOverlayPermission) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        PermissionItem(
            title = "Usage Access",
            description = "Required to track app usage for blocking limits.",
            isGranted = hasUsageStatsPermission,
            onClick = {
                if (!hasUsageStatsPermission) {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            }
        )
        
        PermissionItem(
            title = "System Calendar",
            description = "Required to sync events from local device calendars.",
            isGranted = hasSystemCalendarPermission,
            onClick = {
                if (!hasSystemCalendarPermission) {
                    multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                }
            }
        )
        
        PermissionItem(
            title = "System Contacts",
            description = "Required to pick contacts from the device.",
            isGranted = hasSystemContactsPermission,
            onClick = {
                if (!hasSystemContactsPermission) {
                    multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))
                }
            }
        )

        PermissionItem(
            title = "Google Drive",
            description = "Required to backup and restore app data to the cloud.",
            isGranted = hasDrivePermission,
            onClick = {
                if (!hasDrivePermission) {
                    scope.launch {
                        GoogleDriveSyncManager.getAccessToken(context) { intent ->
                            authResolutionLauncher.launch(intent)
                        }
                    }
                }
            }
        )
        
        PermissionItem(
            title = "Google Contacts",
            description = "Sync with Google Contacts API.",
            isGranted = hasGoogleContactsPermission,
            onClick = {
                if (!hasGoogleContactsPermission) {
                    scope.launch {
                        GoogleContactsSyncManager.getAccessToken(context) { intent ->
                            authResolutionLauncher.launch(intent)
                        }
                    }
                }
            }
        )
        

        PermissionItem(
            title = "Google Tasks",
            description = "Sync with Google Tasks API.",
            isGranted = hasGoogleTasksPermission,
            onClick = {
                if (!hasGoogleTasksPermission) {
                    scope.launch {
                        GoogleTasksSyncManager.getAccessToken(context) { intent ->
                            authResolutionLauncher.launch(intent)
                        }
                    }
                }
            }
        )
        
        PermissionItem(
            title = "Google Fit",
            description = "Sync with Google Fit API for health data.",
            isGranted = hasGoogleFitPermission,
            onClick = {
                if (!hasGoogleFitPermission) {
                    scope.launch {
                        GoogleFitSyncManager.getAccessToken(context) { intent ->
                            authResolutionLauncher.launch(intent)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Grant", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
