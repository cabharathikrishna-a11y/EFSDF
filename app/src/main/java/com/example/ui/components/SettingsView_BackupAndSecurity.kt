package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.util.AppLockHelper
import com.example.util.AppBlockHelper
import com.example.util.GoogleDriveSyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.content.Context
import android.app.Activity
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun LifeOSBackupSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf<String?>(null) }
    
    // Google Drive Integration State
    var hasDrivePermission by remember { mutableStateOf(GoogleDriveSyncManager.hasDrivePermission(context)) }
    var isOperating by remember { mutableStateOf(false) }
    var gdMessage by remember { mutableStateOf<String?>(null) }
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var lastSyncTs by remember { mutableStateOf(prefs.getLong("gd_all_last_sync_timestamp", 0L)) }

    var optTasks by remember { mutableStateOf(prefs.getBoolean("backup_option_tasks", true)) }
    var optHabits by remember { mutableStateOf(prefs.getBoolean("backup_option_habits", true)) }
    var optJournal by remember { mutableStateOf(prefs.getBoolean("backup_option_journal", true)) }
    var optFinances by remember { mutableStateOf(prefs.getBoolean("backup_option_finances", true)) }
    var optContacts by remember { mutableStateOf(prefs.getBoolean("backup_option_contacts", true)) }
    var optFiles by remember { mutableStateOf(prefs.getBoolean("backup_option_files", true)) }
    var optSettings by remember { mutableStateOf(prefs.getBoolean("backup_option_settings", true)) }
    var optHealth by remember { mutableStateOf(prefs.getBoolean("backup_option_health", true)) }
    var optNotes by remember { mutableStateOf(prefs.getBoolean("backup_option_notes", true)) }
    var optFocus by remember { mutableStateOf(prefs.getBoolean("backup_option_focus", true)) }

    fun updateBackupOption(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            statusText = "Exporting data..."
            viewModel.exportBackup(context, uri) { success ->
                statusText = if (success) "Export completed successfully!" else "Failed to export data."
            }
        }
    }

    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            statusText = "Exporting offline archive..."
            viewModel.exportHtmlZip(context, uri) { success ->
                statusText = if (success) "HTML Archive exported successfully! (Offline companion ready)" else "Failed to export HTML archive."
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            statusText = "Importing and reconciling..."
            viewModel.importBackup(context, uri) { success ->
                statusText = if (success) "Import completed successfully! Life OS data synced." else "Failed to import backup."
            }
        }
    }

    val authResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        hasDrivePermission = GoogleDriveSyncManager.hasDrivePermission(context)
        if (result.resultCode == Activity.RESULT_OK) {
            gdMessage = "Google Drive successfully authorized! Tap Backup or Restore to align your app data."
        } else {
            gdMessage = "Google Drive authorization declined."
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        // --- GOOGLE DRIVE CLOUD BACKUP & RESTORE CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            border = androidx.compose.foundation.BorderStroke(1.dp, WaterBlue.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Google Drive Sync",
                            tint = WaterBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Google Drive Backup",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    if (!hasDrivePermission) {
                        Button(
                            onClick = {
                                scope.launch {
                                    GoogleDriveSyncManager.getAccessToken(context) { intent ->
                                        authResolutionLauncher.launch(intent)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp).testTag("connect_drive_settings_btn")
                        ) {
                            Text("CONNECT", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1B5E20).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CONNECTED",
                                color = Color.Green,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Encrypts and uploads your complete Life OS workspace—including nested tasks, completed habits, transaction history logs, journal texts, and media files—directly to your secure, private Google Drive AppData folder.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                if (lastSyncTs > 0L) {
                    Text(
                        text = "Last synced: " + java.text.SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTs)),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                if (hasDrivePermission) {
                    if (isOperating) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = WaterBlue, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accessing Google Drive...", color = Color.LightGray, fontSize = 11.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isOperating = true
                                    gdMessage = "Initiating cloud backup..."
                                    viewModel.backupAllDataToGoogleDrive(context, { intent ->
                                        authResolutionLauncher.launch(intent)
                                    }) { success, msg ->
                                        isOperating = false
                                        gdMessage = msg
                                        if (success) {
                                            lastSyncTs = prefs.getLong("gd_all_last_sync_timestamp", 0L)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WaterBlue.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp).testTag("drive_backup_all_btn")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp), tint = WaterBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cloud Backup", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WaterBlue)
                            }

                            Button(
                                onClick = {
                                    isOperating = true
                                    gdMessage = "Initiating cloud restore..."
                                    viewModel.restoreAllDataFromGoogleDrive(context, { intent ->
                                        authResolutionLauncher.launch(intent)
                                    }) { success, msg ->
                                        isOperating = false
                                        gdMessage = msg
                                        if (success) {
                                            lastSyncTs = prefs.getLong("gd_all_last_sync_timestamp", 0L)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp).testTag("drive_restore_all_btn")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cloud Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                gdMessage?.let {
                    Text(
                        text = it,
                        color = if (it.contains("Successfully") || it.contains("authorized")) Color.Green else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFF1E1E22), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(4.dp))

        // --- BACKUP CATEGORIES SELECTION ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            border = androidx.compose.foundation.BorderStroke(1.dp, WaterBlue.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Backup Options",
                        tint = WaterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Configure Backup Options",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "Select which categories should be exported in manual snapshots or synchronized to Google Drive:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                val items = listOf(
                    Triple("Tasks, Custom Lists & Deadlines", optTasks) { v: Boolean -> optTasks = v; updateBackupOption("backup_option_tasks", v) },
                    Triple("Habits & Streak Completions", optHabits) { v: Boolean -> optHabits = v; updateBackupOption("backup_option_habits", v) },
                    Triple("Journal Texts & Entries", optJournal) { v: Boolean -> optJournal = v; updateBackupOption("backup_option_journal", v) },
                    Triple("Finance Accounts, Ledger & Transactions", optFinances) { v: Boolean -> optFinances = v; updateBackupOption("backup_option_finances", v) },
                    Triple("Contacts & Folders", optContacts) { v: Boolean -> optContacts = v; updateBackupOption("backup_option_contacts", v) },
                    Triple("Local Files & Documents (PDF/Word/Excel)", optFiles) { v: Boolean -> optFiles = v; updateBackupOption("backup_option_files", v) },
                    Triple("App Configuration & Shared Preferences", optSettings) { v: Boolean -> optSettings = v; updateBackupOption("backup_option_settings", v) },
                    Triple("Health & Fitness Records (Steps, Water, Food Diary)", optHealth) { v: Boolean -> optHealth = v; updateBackupOption("backup_option_health", v) },
                    Triple("Pinned & Personal Keep Notes", optNotes) { v: Boolean -> optNotes = v; updateBackupOption("backup_option_notes", v) },
                    Triple("Focus Session History Log", optFocus) { v: Boolean -> optFocus = v; updateBackupOption("backup_option_focus", v) }
                )

                items.forEach { (label, value, onValueChange) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueChange(!value) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = Color.LightGray, fontSize = 12.sp)
                        Switch(
                            checked = value,
                            onCheckedChange = onValueChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WaterBlue,
                                checkedTrackColor = WaterBlue.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFF1E1E22), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(4.dp))

        // --- MANUAL SNAPSHOTS ---
        Text("Manage Manual Snapshots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Export or restore your localized databases, settings, task lists, and history records securely.", color = Color.Gray, fontSize = 11.sp)
        
        Button(
            onClick = {
                exportLauncher.launch("life_os_backup_${System.currentTimeMillis()}.zip")
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Manual Backup (ZIP)")
        }
        
        Button(
            onClick = {
                importLauncher.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "application/x-zip-compressed"))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1E), contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import and Reconcile Backup (ZIP/JSON)")
        }

        Button(
            onClick = {
                htmlExportLauncher.launch("life_os_offline_dashboard_${System.currentTimeMillis()}.zip")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E), contentColor = Color.Green),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = Color.Green)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Offline HTML Companion (ZIP)", color = Color.Green)
        }
        
        statusText?.let {
            Text(it, color = if (it.contains("successfully")) Color.Green else Color.Red, fontSize = 12.sp)
        }
    }
}

@Composable
fun AppLockSettingsSection() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(AppLockHelper.isAppLockEnabled(context)) }
    var lockType by remember { mutableStateOf(AppLockHelper.getLockType(context)) }
    var code by remember { mutableStateOf(AppLockHelper.getLockCode(context) ?: "") }
    var biometricsEnabled by remember { mutableStateOf(AppLockHelper.isBiometricsEnabled(context)) }
    var showSetupDialog by remember { mutableStateOf(false) }
    
    // Recovery Questions State
    val questions = remember { AppLockHelper.getSecurityQuestions(context) }
    var q1 by remember { mutableStateOf(questions[0].first) }
    var a1 by remember { mutableStateOf(questions[0].second) }
    var q2 by remember { mutableStateOf(questions[1].first) }
    var a2 by remember { mutableStateOf(questions[1].second) }
    var q3 by remember { mutableStateOf(questions[2].first) }
    var a3 by remember { mutableStateOf(questions[2].second) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("App Lock Configuration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable App Lock", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Require PIN or password when opening Life OS", color = Color.Gray, fontSize = 11.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showSetupDialog = true
                    } else {
                        AppLockHelper.setAppLockEnabled(context, false)
                        AppLockHelper.setLockCode(context, null)
                        isEnabled = false
                        code = ""
                    }
                }
            )
        }
        
        if (isEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lock Type", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Select authentication mode", color = Color.Gray, fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = lockType == "pin",
                        onClick = {
                            lockType = "pin"
                            AppLockHelper.setLockType(context, "pin")
                        },
                        label = { Text("PIN") }
                    )
                    FilterChip(
                        selected = lockType == "password",
                        onClick = {
                            lockType = "password"
                            AppLockHelper.setLockType(context, "password")
                        },
                        label = { Text("Password") }
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric Unlock", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Allow face or fingerprint unlock if supported", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = {
                        biometricsEnabled = it
                        AppLockHelper.setBiometricsEnabled(context, it)
                    }
                )
            }
        }
        
        if (showSetupDialog) {
            AlertDialog(
                onDismissRequest = { showSetupDialog = false },
                title = { Text("Setup Secure Lock") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Configure your security code and security questions to enable recovery in case you forget it.", color = Color.Gray, fontSize = 11.sp)
                        
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text(if (lockType == "pin") "Enter PIN (Digits)" else "Enter Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = if (lockType == "pin") KeyboardType.Number else KeyboardType.Password)
                        )
                        
                        Text("Recovery Security Questions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(q1, color = Color.LightGray, fontSize = 11.sp)
                            OutlinedTextField(
                                value = a1,
                                onValueChange = { a1 = it },
                                label = { Text("Answer 1") },
                                singleLine = true
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(q2, color = Color.LightGray, fontSize = 11.sp)
                            OutlinedTextField(
                                value = a2,
                                onValueChange = { a2 = it },
                                label = { Text("Answer 2") },
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (code.isNotBlank() && a1.isNotBlank() && a2.isNotBlank()) {
                                AppLockHelper.setLockCode(context, code)
                                AppLockHelper.setAppLockEnabled(context, true)
                                AppLockHelper.saveSecurityQuestions(context, q1, a1, q2, a2, q3, a3)
                                AppLockHelper.setSecuritySetupComplete(context, true)
                                isEnabled = true
                                showSetupDialog = false
                            }
                        }
                    ) {
                        Text("Enable App Lock")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AppBlocksSettingsSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(AppBlockHelper.hasUsageStatsPermission(context)) }
    var blockedApps by remember { mutableStateOf(AppBlockHelper.getBlockedApps(context)) }
    var selectedAppForLimit by remember { mutableStateOf<String?>(null) }
    var limitMinutesText by remember { mutableStateOf("30") }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var newAppPackage by remember { mutableStateOf("") }
    
    // State for all installed apps on device
    var installedApps by remember { mutableStateOf<List<com.example.util.AppBlockHelper.AppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Refresh permission status when entering and load apps asynchronously
    LaunchedEffect(Unit) {
        hasPermission = AppBlockHelper.hasUsageStatsPermission(context)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            installedApps = com.example.util.AppBlockHelper.getInstalledApps(context)
            isLoadingApps = false
        }
    }

    // Format usage helper
    fun formatUsageTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m ${s}s"
            else -> "${s}s"
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("App Blocks & Usage Limits", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Set daily tracked screen-time limit quotas for distracting applications.", color = Color.Gray, fontSize = 11.sp)
        
        if (!hasPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Usage Stats Permission Required", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Life OS requires the Usage Access permission to track open times and enforce screen limits.", color = Color.LightGray, fontSize = 11.sp)
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Grant Permission", fontSize = 12.sp)
                    }
                }
            }
        }
        
        // Quick App Toggles for Instagram and YouTube
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("QUICK APP TOGGLES", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                
                // Instagram Toggle
                val isInstaBlocked = blockedApps.contains("com.instagram.android")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppListIcon(pkg = "com.instagram.android")
                        }
                    }
                    Switch(
                        checked = isInstaBlocked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                AppBlockHelper.addBlockedApp(context, "com.instagram.android")
                            } else {
                                AppBlockHelper.removeBlockedApp(context, "com.instagram.android")
                            }
                            blockedApps = AppBlockHelper.getBlockedApps(context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = WaterBlue,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                // YouTube Toggle
                val isYoutubeBlocked = blockedApps.contains("com.google.android.youtube")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppListIcon(pkg = "com.google.android.youtube")
                        }
                    }
                    Switch(
                        checked = isYoutubeBlocked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                AppBlockHelper.addBlockedApp(context, "com.google.android.youtube")
                            } else {
                                AppBlockHelper.removeBlockedApp(context, "com.google.android.youtube")
                            }
                            blockedApps = AppBlockHelper.getBlockedApps(context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = WaterBlue,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }

        // Instagram Advanced Blocker Settings Card
        var useSelectiveIg by remember { mutableStateOf(AppBlockHelper.isIgSelectiveBlockingEnabled(context)) }
        var isIgReelsBlocked by remember { mutableStateOf(AppBlockHelper.isIgReelsBlocked(context)) }
        var isIgStoriesBlocked by remember { mutableStateOf(AppBlockHelper.isIgStoriesBlocked(context)) }
        var isIgExploreBlocked by remember { mutableStateOf(AppBlockHelper.isIgExploreBlocked(context)) }
        var isIgAllowSharedReels by remember { mutableStateOf(AppBlockHelper.isIgAllowSharedReels(context)) }
        var isIgFeedScrollLimit by remember { mutableStateOf(AppBlockHelper.isIgFeedScrollLimit(context)) }
        var isIgReelsMuteAudio by remember { mutableStateOf(AppBlockHelper.isIgReelsMuteAudio(context)) }

        // YouTube Advanced Blocker Settings Card
        var useSelectiveYt by remember { mutableStateOf(AppBlockHelper.isYtSelectiveBlockingEnabled(context)) }
        var isYtShortsBlocked by remember { mutableStateOf(AppBlockHelper.isYtShortsBlocked(context)) }
        var isYtSearchBlocked by remember { mutableStateOf(AppBlockHelper.isYtSearchBlocked(context)) }
        var isYtCommentsBlocked by remember { mutableStateOf(AppBlockHelper.isYtCommentsBlocked(context)) }
        var isYtOnlyAllowApprovedChannels by remember { mutableStateOf(AppBlockHelper.isYtOnlyAllowApprovedChannels(context)) }
        var ytApprovedChannels by remember { mutableStateOf(AppBlockHelper.getYtApprovedChannels(context)) }

        // Snapchat Advanced Blocker Settings Card
        var useSelectiveSnap by remember { mutableStateOf(AppBlockHelper.isSnapSelectiveBlockingEnabled(context)) }
        var isSnapSpotlightBlocked by remember { mutableStateOf(AppBlockHelper.isSnapSpotlightBlocked(context)) }
        var isSnapMapBlocked by remember { mutableStateOf(AppBlockHelper.isSnapMapBlocked(context)) }
        var isSnapDiscoverBlocked by remember { mutableStateOf(AppBlockHelper.isSnapDiscoverBlocked(context)) }

        // Facebook Advanced Blocker Settings Card
        var useSelectiveFb by remember { mutableStateOf(AppBlockHelper.isFbSelectiveBlockingEnabled(context)) }
        var isFbReelsBlocked by remember { mutableStateOf(AppBlockHelper.isFbReelsBlocked(context)) }
        var isFbWatchBlocked by remember { mutableStateOf(AppBlockHelper.isFbWatchBlocked(context)) }
        var isFbStoriesBlocked by remember { mutableStateOf(AppBlockHelper.isFbStoriesBlocked(context)) }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE1306C).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().testTag("instagram_advanced_blocker_card")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF833AB4).copy(alpha = 0.08f), Color(0xFFE1306C).copy(alpha = 0.04f), Color.Transparent)
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title and Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF833AB4), Color(0xFFF77737))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Instagram Blocker",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INSTAGRAM SURGICAL BLOCKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Fine-grained controls for specific Instagram features.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                // 1. Master Toggle: Use Selective blocking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Selective Sub-Feature Blocking", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Bypasses full app blocking to allow Chats while enforcing specific limits below.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                    Switch(
                        checked = useSelectiveIg,
                        onCheckedChange = { checked ->
                            useSelectiveIg = checked
                            AppBlockHelper.setIgSelectiveBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFE1306C),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("ig_selective_blocking_switch")
                    )
                }

                if (useSelectiveIg) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))

                    // 2. Block Reels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Surgical Reels Blocker", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Prevents scrolling or viewing Reels completely. Instantly closes or reverts back.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgReelsBlocked,
                            onCheckedChange = { checked ->
                                isIgReelsBlocked = checked
                                AppBlockHelper.setIgReelsBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_block_reels_switch")
                        )
                    }

                    // 3. Block Stories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Stories", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Hides/interdicts user Stories completely. Story viewer is closed on click.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgStoriesBlocked,
                            onCheckedChange = { checked ->
                                isIgStoriesBlocked = checked
                                AppBlockHelper.setIgStoriesBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_block_stories_switch")
                        )
                    }

                    // 4. Block Explore Tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Explore & Search Tab", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Redirects away from the Explore search grid to prevent visual doomscrolling.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgExploreBlocked,
                            onCheckedChange = { checked ->
                                isIgExploreBlocked = checked
                                AppBlockHelper.setIgExploreBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_block_explore_switch")
                        )
                    }

                    // 5. Allow Shared Reels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Allow Shared Reel in Messages", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Enables viewing a single reel sent in DMs. Scrolling/swiping to next is instantly blocked & returns to chat.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgAllowSharedReels,
                            onCheckedChange = { checked ->
                                isIgAllowSharedReels = checked
                                AppBlockHelper.setIgAllowSharedReels(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_allow_shared_reels_switch")
                        )
                    }

                    // 6. Feed Scroll Limiter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Feed Scroll Limiter", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Quota: maximum of 5 scroll gestures in main feed per Focus phase before closure.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgFeedScrollLimit,
                            onCheckedChange = { checked ->
                                isIgFeedScrollLimit = checked
                                AppBlockHelper.setIgFeedScrollLimit(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_feed_scroll_limit_switch")
                        )
                    }

                    // 7. Auto-Mute Reels Audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Auto-Mute Reels Audio", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Eliminates auditory attention traps by forcing Reel mutes on launch.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isIgReelsMuteAudio,
                            onCheckedChange = { checked ->
                                isIgReelsMuteAudio = checked
                                AppBlockHelper.setIgReelsMuteAudio(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFE1306C),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("ig_mute_reels_audio_switch")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // YouTube Advanced Blocker Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().testTag("youtube_advanced_blocker_card")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF0000).copy(alpha = 0.08f), Color(0xFF990000).copy(alpha = 0.04f), Color.Transparent)
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF0000), Color(0xFF990000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "YouTube Blocker",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "YOUTUBE SURGICAL BLOCKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Fine-grained controls for specific YouTube features.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                // 1. Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Selective Sub-Feature Blocking", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Bypasses full app blocking to allow regular video play while restricting specific distractions.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                    Switch(
                        checked = useSelectiveYt,
                        onCheckedChange = { checked ->
                            useSelectiveYt = checked
                            AppBlockHelper.setYtSelectiveBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFFF0000),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("yt_selective_blocking_switch")
                    )
                }

                if (useSelectiveYt) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))

                    // 2. Block Shorts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block YouTube Shorts", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Instantly intercepts and blocks YouTube Shorts swipe-feeds.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isYtShortsBlocked,
                            onCheckedChange = { checked ->
                                isYtShortsBlocked = checked
                                AppBlockHelper.setYtShortsBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFF0000),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("yt_block_shorts_switch")
                        )
                    }

                    // 3. Block Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Search Feed", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Prevents manual searching of videos during active focus periods.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isYtSearchBlocked,
                            onCheckedChange = { checked ->
                                isYtSearchBlocked = checked
                                AppBlockHelper.setYtSearchBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFF0000),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("yt_block_search_switch")
                        )
                    }

                    // 4. Block Comments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Hide Comments Section", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Blocks view of the YouTube comments pane to stop reading/writing distractions.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isYtCommentsBlocked,
                            onCheckedChange = { checked ->
                                isYtCommentsBlocked = checked
                                AppBlockHelper.setYtCommentsBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFF0000),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("yt_block_comments_switch")
                        )
                    }

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))

                    // 5. Only Allow Whitelisted Channels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Only Allow Whitelisted Channels", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Blocks play/view of any video except those from your approved channels list below.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isYtOnlyAllowApprovedChannels,
                            onCheckedChange = { checked ->
                                isYtOnlyAllowApprovedChannels = checked
                                AppBlockHelper.setYtOnlyAllowApprovedChannels(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFF0000),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("yt_only_allow_approved_channels_switch")
                        )
                    }

                    if (isYtOnlyAllowApprovedChannels) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Whitelisted Channel Names (comma separated):",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = ytApprovedChannels,
                                onValueChange = { newValue ->
                                    ytApprovedChannels = newValue
                                    AppBlockHelper.setYtApprovedChannels(context, newValue)
                                },
                                placeholder = {
                                    Text("e.g. Marques Brownlee, Kurzgesagt, TEDx", color = Color.DarkGray, fontSize = 11.sp)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("yt_approved_channels_input"),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF0000),
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedContainerColor = Color(0xFF141419),
                                    unfocusedContainerColor = Color(0xFF0F0F12),
                                    cursorColor = Color(0xFFFF0000)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Text(
                                text = "Matches are case-insensitive and allow partial matches. Enter full channel names or keywords separated by commas.",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Snapchat Advanced Blocker Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFFC00).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().testTag("snapchat_advanced_blocker_card")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFC00).copy(alpha = 0.05f), Color(0xFFFFFC00).copy(alpha = 0.02f), Color.Transparent)
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFC00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Snapchat Blocker",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SNAPCHAT SURGICAL BLOCKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Fine-grained controls for specific Snapchat features.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                // 1. Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Selective Sub-Feature Blocking", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Bypasses full app blocking to allow chat, viewing & uploading snaps while blocking feeds.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                    Switch(
                        checked = useSelectiveSnap,
                        onCheckedChange = { checked ->
                            useSelectiveSnap = checked
                            AppBlockHelper.setSnapSelectiveBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFFFFC00),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("snap_selective_blocking_switch")
                    )
                }

                if (useSelectiveSnap) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))

                    // 2. Block Spotlight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Snapchat Spotlight", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Prevents viewing or scrolling the Spotlight vertical video feeds.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isSnapSpotlightBlocked,
                            onCheckedChange = { checked ->
                                isSnapSpotlightBlocked = checked
                                AppBlockHelper.setSnapSpotlightBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFFFC00),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("snap_block_spotlight_switch")
                        )
                    }

                    // 3. Block Map
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Snap Map", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Restricts access to the location map tab to keep focus private and active.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isSnapMapBlocked,
                            onCheckedChange = { checked ->
                                isSnapMapBlocked = checked
                                AppBlockHelper.setSnapMapBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFFFC00),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("snap_block_map_switch")
                        )
                    }

                    // 4. Block Discover & Stories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Stories & Discover Feeds", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Hides user Stories, sub feeds, and show discoveries completely.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isSnapDiscoverBlocked,
                            onCheckedChange = { checked ->
                                isSnapDiscoverBlocked = checked
                                AppBlockHelper.setSnapDiscoverBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFFFC00),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("snap_block_discover_switch")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Facebook Advanced Blocker Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().testTag("facebook_advanced_blocker_card")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1877F2).copy(alpha = 0.08f), Color(0xFF1877F2).copy(alpha = 0.04f), Color.Transparent)
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1877F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Facebook Blocker",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "FACEBOOK SURGICAL BLOCKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Fine-grained controls for specific Facebook features.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                // 1. Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Selective Sub-Feature Blocking", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Bypasses full app blocking to allow messaging/posts while blocking feeds & videos.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                    Switch(
                        checked = useSelectiveFb,
                        onCheckedChange = { checked ->
                            useSelectiveFb = checked
                            AppBlockHelper.setFbSelectiveBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF1877F2),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("fb_selective_blocking_switch")
                    )
                }

                if (useSelectiveFb) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))

                    // 2. Block Reels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Facebook Reels", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Prevents viewing or scrolling short vertical video Reels.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isFbReelsBlocked,
                            onCheckedChange = { checked ->
                                isFbReelsBlocked = checked
                                AppBlockHelper.setFbReelsBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFF1877F2),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("fb_block_reels_switch")
                        )
                    }

                    // 3. Block Watch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Watch & Video Feed", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Prevents entering the Watch tab or video panels entirely.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isFbWatchBlocked,
                            onCheckedChange = { checked ->
                                isFbWatchBlocked = checked
                                AppBlockHelper.setFbWatchBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFF1877F2),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("fb_block_watch_switch")
                        )
                    }

                    // 4. Block Stories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Block Stories", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Omit & hide Facebook Stories and story trays on the screen.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                        Switch(
                            checked = isFbStoriesBlocked,
                            onCheckedChange = { checked ->
                                isFbStoriesBlocked = checked
                                AppBlockHelper.setFbStoriesBlocked(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFF1877F2),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("fb_block_stories_switch")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Active Block Rules Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Block Rules", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(onClick = { showAddAppDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Manual Add App", tint = Color.LightGray)
                    }
                }
                
                if (blockedApps.isEmpty()) {
                    Text("No app limits configured. Enable blocks on any app in the directory below to restrict its usage.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    blockedApps.forEach { pkg ->
                        val limitMins = AppBlockHelper.getDailyLimitMinutes(context, pkg)
                        val dailyUsageSecs = AppBlockHelper.getDailyUsageSeconds(context, pkg)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                AppListIcon(pkg = pkg)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Limit: $limitMins min", color = Color(0xFFFFA726), fontSize = 10.sp)
                                    Text("•", color = Color.DarkGray, fontSize = 10.sp)
                                    Text("Today: ${formatUsageTime(dailyUsageSecs)}", color = if (dailyUsageSecs > 0) WaterBlue else Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    selectedAppForLimit = pkg
                                    limitMinutesText = limitMins.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Limit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = {
                                    AppBlockHelper.removeBlockedApp(context, pkg)
                                    blockedApps = AppBlockHelper.getBlockedApps(context)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Installed Apps & Screen Time Directory Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Installed Apps & Screen Time", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Lists all user-launchable apps installed. Updates automatically.", color = Color.Gray, fontSize = 10.sp)
                    }
                    IconButton(onClick = {
                        isLoadingApps = true
                        scope.launch {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                installedApps = com.example.util.AppBlockHelper.getInstalledApps(context)
                                isLoadingApps = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Apps List", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", fontSize = 12.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141419),
                        unfocusedContainerColor = Color(0xFF141419)
                    )
                )

                if (isLoadingApps) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WaterBlue, modifier = Modifier.size(24.dp))
                    }
                } else {
                    val filteredApps = remember(searchQuery, installedApps) {
                        if (searchQuery.isBlank()) {
                            installedApps
                        } else {
                            installedApps.filter {
                                it.label.contains(searchQuery, ignoreCase = true) ||
                                it.packageName.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredApps.isEmpty()) {
                        Text("No apps match your search.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredApps.forEach { app ->
                                val isBlocked = blockedApps.contains(app.packageName)
                                val dailyUsageSecs = AppBlockHelper.getDailyUsageSeconds(context, app.packageName)
                                val limitMins = AppBlockHelper.getDailyLimitMinutes(context, app.packageName)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        AppListIcon(pkg = app.packageName)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Today: ${formatUsageTime(dailyUsageSecs)}",
                                                color = if (dailyUsageSecs > 0) WaterBlue else Color.Gray,
                                                fontSize = 10.sp
                                            )
                                            if (isBlocked) {
                                                Text(
                                                    text = "Limit: $limitMins min",
                                                    color = Color(0xFFFFA726),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isBlocked) {
                                            IconButton(onClick = {
                                                selectedAppForLimit = app.packageName
                                                limitMinutesText = limitMins.toString()
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Limit",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (isBlocked) {
                                                    AppBlockHelper.removeBlockedApp(context, app.packageName)
                                                } else {
                                                    AppBlockHelper.addBlockedApp(context, app.packageName)
                                                }
                                                blockedApps = AppBlockHelper.getBlockedApps(context)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isBlocked) Color(0xFF2D1515) else Color(0xFF1E1E24),
                                                contentColor = if (isBlocked) Color.Red else Color.LightGray
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (isBlocked) "Blocked" else "Block",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
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
        
        selectedAppForLimit?.let { pkg ->
            AlertDialog(
                onDismissRequest = { selectedAppForLimit = null },
                title = { Text("Edit App Limit") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pkg, color = Color.Gray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = limitMinutesText,
                            onValueChange = { limitMinutesText = it },
                            label = { Text("Daily Limit (minutes)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mins = limitMinutesText.toIntOrNull() ?: 30
                            AppBlockHelper.setDailyLimitMinutes(context, pkg, mins)
                            selectedAppForLimit = null
                            blockedApps = AppBlockHelper.getBlockedApps(context)
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppForLimit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showAddAppDialog) {
            AlertDialog(
                onDismissRequest = { showAddAppDialog = false },
                title = { Text("Add App via Package Name") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("For unlisted or system package names", color = Color.Gray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = newAppPackage,
                            onValueChange = { newAppPackage = it },
                            label = { Text("Package Name (e.g. com.facebook.katana)") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAppPackage.isNotBlank()) {
                                AppBlockHelper.addBlockedApp(context, newAppPackage.trim())
                                blockedApps = AppBlockHelper.getBlockedApps(context)
                                showAddAppDialog = false
                                newAppPackage = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAppDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun GoogleCalendarAndTasksSyncSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val calendarSyncStatus by viewModel.calendarSyncStatus.collectAsState()
    val googleTasksSyncStatus by viewModel.googleTasksSyncStatus.collectAsState()
    
    val googleAccount = remember { GoogleSignIn.getLastSignedInAccount(context) }
    
    // Auth launcher for tasks sync
    val tasksAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.syncGoogleTasks(context)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, WaterBlue.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Cloud Backup Sync",
                    tint = WaterBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Google Calendar & Tasks Cloud Sync",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            
            Text(
                text = "Securely synchronize your scheduled events directly with Google Calendar, and your task lists with Google Tasks. This provides a robust, visual cloud backup of all your activities and task lists.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            
            HorizontalDivider(color = Color(0xFF1E1E22), thickness = 0.5.dp)

            // Google Calendar Sync Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF222225)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(18.dp))
                            Text("Google Calendar Backup", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0288D1).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", color = Color(0xFF03A9F4), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Text(
                        "Syncs your timed schedule blocks directly to your Google Calendar app for visual timeline backups.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STATUS", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(calendarSyncStatus, color = Color.LightGray, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.syncGoogleCalendar(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Sync Calendar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Google Tasks Sync Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF222225)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(18.dp))
                            Text("Google Tasks Backup", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF388E3C).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", color = Color(0xFF4CAF50), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Text(
                        "Synchronizes all lists and tasks bidirectionally with the official Google Tasks cloud API.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STATUS", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(googleTasksSyncStatus, color = Color.LightGray, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.syncGoogleTasks(context) { intent ->
                                    tasksAuthLauncher.launch(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Sync Tasks", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseConfigurationSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var dbUrl by remember { mutableStateOf(prefs.getString("custom_firebase_db_url", com.example.api.FirebaseConfig.DATABASE_URL) ?: com.example.api.FirebaseConfig.DATABASE_URL) }
    var projectId by remember { mutableStateOf(prefs.getString("custom_firebase_project_id", "cloud-storage-f8ab3") ?: "cloud-storage-f8ab3") }
    var appId by remember { mutableStateOf(prefs.getString("custom_firebase_app_id", "1:1071485303521:android:9e4d5881f185efbe5d5d88") ?: "1:1071485303521:android:9e4d5881f185efbe5d5d88") }
    var storageBucket by remember { mutableStateOf(prefs.getString("custom_firebase_storage_bucket", "cloud-storage-f8ab3.appspot.com") ?: "cloud-storage-f8ab3.appspot.com") }
    var realtimeSyncEnabled by remember { mutableStateOf(prefs.getBoolean("enable_firebase_realtime_sync", true)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF09090C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.WaterBlue.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = "Firebase Config",
                    tint = com.example.ui.theme.WaterBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Firebase Realtime Database & Storage",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Configure your own Firebase endpoints below. When changed, the app client dynamically reinstantiates Retrofit connections to sync tasks, ledger, profile details, and peer focus sessions in real time.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            HorizontalDivider(color = Color(0xFF1E1E22), thickness = 0.5.dp)

            // Database URL input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Firebase Database URL", color = Color.Gray, fontSize = 11.sp)
                OutlinedTextField(
                    value = dbUrl,
                    onValueChange = { dbUrl = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.example.ui.theme.WaterBlue,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = com.example.ui.theme.WaterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Project ID input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Firebase Project ID", color = Color.Gray, fontSize = 11.sp)
                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.example.ui.theme.WaterBlue,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = com.example.ui.theme.WaterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // App ID input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Firebase App ID", color = Color.Gray, fontSize = 11.sp)
                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.example.ui.theme.WaterBlue,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = com.example.ui.theme.WaterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Storage Bucket input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Firebase Storage Bucket", color = Color.Gray, fontSize = 11.sp)
                OutlinedTextField(
                    value = storageBucket,
                    onValueChange = { storageBucket = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.example.ui.theme.WaterBlue,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = com.example.ui.theme.WaterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Real-time Sync Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .clickable {
                        realtimeSyncEnabled = !realtimeSyncEnabled
                        prefs.edit().putBoolean("enable_firebase_realtime_sync", realtimeSyncEnabled).apply()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Real-time Synchronization",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Bi-directional database updates are push-triggered in real time",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = realtimeSyncEnabled,
                    onCheckedChange = { value ->
                        realtimeSyncEnabled = value
                        prefs.edit().putBoolean("enable_firebase_realtime_sync", value).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = com.example.ui.theme.WaterBlue,
                        checkedTrackColor = com.example.ui.theme.WaterBlue.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Reset to defaults
                        dbUrl = com.example.api.FirebaseConfig.DATABASE_URL
                        projectId = "cloud-storage-f8ab3"
                        appId = "1:1071485303521:android:9e4d5881f185efbe5d5d88"
                        storageBucket = "cloud-storage-f8ab3.appspot.com"
                        realtimeSyncEnabled = true

                        prefs.edit()
                            .remove("custom_firebase_db_url")
                            .remove("custom_firebase_project_id")
                            .remove("custom_firebase_app_id")
                            .remove("custom_firebase_storage_bucket")
                            .putBoolean("enable_firebase_realtime_sync", true)
                            .apply()

                        com.example.api.FirebaseClient.activeUrl = com.example.api.FirebaseConfig.DATABASE_URL
                        Toast.makeText(context, "Reset to official Life OS Firebase defaults!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24), contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Text("Reset Default", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (dbUrl.isBlank() || !dbUrl.startsWith("http")) {
                            Toast.makeText(context, "Please enter a valid HTTP/S Firebase Database URL", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        prefs.edit()
                            .putString("custom_firebase_db_url", dbUrl)
                            .putString("custom_firebase_project_id", projectId)
                            .putString("custom_firebase_app_id", appId)
                            .putString("custom_firebase_storage_bucket", storageBucket)
                            .putBoolean("enable_firebase_realtime_sync", realtimeSyncEnabled)
                            .apply()

                        // Dynamically update the Retrofit service active URL
                        com.example.api.FirebaseClient.activeUrl = dbUrl

                        Toast.makeText(context, "Firebase dynamic endpoint updated and saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.WaterBlue, contentColor = Color.Black)
                ) {
                    Text("Save Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
