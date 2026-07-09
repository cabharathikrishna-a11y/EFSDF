package com.example.ui.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.util.AppUpdateManager
import com.example.util.UpdateStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUpdatesPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val updateStatus by AppUpdateManager.updateStatus.collectAsState()
    
    var autoDownload by remember { mutableStateOf(AppUpdateManager.isAutoUpdateEnabled(context)) }
    var pauseUpdates by remember { mutableStateOf(AppUpdateManager.isPauseUpdatesEnabled(context)) }
    var forceUpdate by remember { mutableStateOf(AppUpdateManager.isForceUpdateEnabled(context)) }
    
    var githubOwner by remember { mutableStateOf(AppUpdateManager.getGithubOwner(context)) }
    var githubRepo by remember { mutableStateOf(AppUpdateManager.getGithubRepo(context)) }
    var runningFirebaseCode by remember { mutableStateOf(AppUpdateManager.getRunningFirebaseVersion(context)) }

    // Check if there is an offline downloaded update ready to install
    val readyApkPath = remember(updateStatus) { AppUpdateManager.getReadyApkPath(context) }
    val offlineApkFile = remember(readyApkPath) { readyApkPath?.let { java.io.File(it) } }
    val isOfflineApkReady = remember(offlineApkFile) { 
        offlineApkFile != null && offlineApkFile.exists() && offlineApkFile.length() > 0 && AppUpdateManager.isValidAndNewerApk(context, offlineApkFile)
    }

    val currentVersionCode = remember { AppUpdateManager.getCurrentVersionCode(context) }
    val currentVersionName = remember { AppUpdateManager.getCurrentVersionName(context) }

    SettingsPageScope {
        SettingsSubpageWorkspace(
            title = "System Update Center",
            description = "Manage Firebase App Distribution updates, automated downloads, and restore backups.",
            onBack = onBack
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Status Dashboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    border = BorderStroke(1.dp, WaterBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(WaterBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (updateStatus) {
                                is UpdateStatus.Checking -> Icons.Default.Refresh
                                is UpdateStatus.Downloading -> Icons.Default.ArrowDropDown
                                is UpdateStatus.ReadyToInstall -> Icons.Default.CheckCircle
                                is UpdateStatus.NewVersionAvailable -> Icons.Default.Info
                                is UpdateStatus.Error -> Icons.Default.Warning
                                else -> Icons.Default.Settings
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Status Icon",
                                tint = WaterBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CURRENT VERSION: $currentVersionName (BUILD $currentVersionCode) | FIREBASE BUILD: $runningFirebaseCode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val statusLabel = when (val state = updateStatus) {
                            is UpdateStatus.Idle -> "System is up to date"
                            is UpdateStatus.Checking -> "Checking for updates..."
                            is UpdateStatus.SecuringData -> "Securing user data and performing auto-backup..."
                            is UpdateStatus.Downloading -> "Downloading system update: ${(state.progress * 100).toInt()}%"
                            is UpdateStatus.ReadyToInstall -> "System Update Downloaded & Ready"
                            is UpdateStatus.NewVersionAvailable -> "New Update Available: Build ${state.versionId}"
                            is UpdateStatus.NoUpdateAvailable -> "Your system is up to date (Build ${state.localVersion})"
                            is UpdateStatus.Error -> "Check failed: ${state.message}"
                        }

                        Text(
                            text = statusLabel.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        if (updateStatus is UpdateStatus.Downloading) {
                            val progress = (updateStatus as UpdateStatus.Downloading).progress
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { if (progress >= 0f) progress else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = WaterBlue,
                                trackColor = Color(0xFF1F1F24)
                            )
                        }
                    }
                }

                // 2. Manual Action Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MANUAL ACTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlue,
                            letterSpacing = 0.5.sp
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    AppUpdateManager.checkForUpdates(context, manualCheck = true)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("check_updates_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Updates", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        // Download & Install button if a new version is available but not yet downloaded
                        if (updateStatus is UpdateStatus.NewVersionAvailable) {
                            val state = updateStatus as UpdateStatus.NewVersionAvailable
                            Button(
                                onClick = {
                                    AppUpdateManager.startDownloadAndInstall(context, state.apkFileId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("download_updates_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Download", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download & Install Build ${state.versionId}", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Install button if update downloaded
                        val canInstall = updateStatus is UpdateStatus.ReadyToInstall || isOfflineApkReady
                        val apkFileToInstall = when {
                            updateStatus is UpdateStatus.ReadyToInstall -> (updateStatus as UpdateStatus.ReadyToInstall).apkFile
                            isOfflineApkReady -> offlineApkFile
                            else -> null
                        }

                        if (canInstall && apkFileToInstall != null) {
                            Button(
                                onClick = {
                                    AppUpdateManager.installApk(context, apkFileToInstall)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("install_updates_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Install", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Install Downloaded Update", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Force Redownload & Re-sync button
                        Button(
                            onClick = {
                                AppUpdateManager.forceRedownloadUpdate(context)
                                Toast.makeText(context, "Cleaning files & initiating fresh redownload...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("force_redownload_updates_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Redownload", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Force Redownload & Re-sync", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // App Distribution specific Tester authentication
                        Button(
                            onClick = {
                                try {
                                    val appDist = com.google.firebase.appdistribution.FirebaseAppDistribution.getInstance()
                                    if (!appDist.isTesterSignedIn) {
                                        Toast.makeText(context, "Opening Firebase App Distribution Login...", Toast.LENGTH_SHORT).show()
                                        appDist.signInTester()
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Firebase Tester Sign-In Succeeded!", Toast.LENGTH_LONG).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Sign-In Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Firebase Tester is already signed in!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "App Distribution Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("app_dist_sign_in_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1E)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Tester Sign In", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Firebase Tester Authentication", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Update Preferences Toggles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PREFERENCES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlue,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Auto download toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    autoDownload = !autoDownload
                                    AppUpdateManager.setAutoUpdateEnabled(context, autoDownload)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Download Updates", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Automatically download APKs silently in the background on startup.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = autoDownload,
                                onCheckedChange = {
                                    autoDownload = it
                                    AppUpdateManager.setAutoUpdateEnabled(context, it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.4f))
                            )
                        }

                        HorizontalDivider(color = Color(0xFF16161A), thickness = 0.5.dp)

                        // Pause updates toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pauseUpdates = !pauseUpdates
                                    AppUpdateManager.setPauseUpdatesEnabled(context, pauseUpdates)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pause Updates", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Temporarily pause silent background updates checks.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = pauseUpdates,
                                onCheckedChange = {
                                    pauseUpdates = it
                                    AppUpdateManager.setPauseUpdatesEnabled(context, it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.4f))
                            )
                        }

                        HorizontalDivider(color = Color(0xFF16161A), thickness = 0.5.dp)

                        // Force updates toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    forceUpdate = !forceUpdate
                                    AppUpdateManager.setForceUpdateEnabled(context, forceUpdate)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Force System Updates", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Disallow bypassing updates when a new critical build is available.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = forceUpdate,
                                onCheckedChange = {
                                    forceUpdate = it
                                    AppUpdateManager.setForceUpdateEnabled(context, it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.4f))
                            )
                        }
                    }
                }

                // 3.5. Firebase Running Version Override
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "FIREBASE VERSION CONFIGURATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlue,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "To prevent infinite installation loops on GitHub automated builds, Life OS tracks your current running version code independently from the hardcoded package codebase. Adjust this value to bypass or enable system updates manually.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Running Firebase Version", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (runningFirebaseCode > 1) {
                                            runningFirebaseCode -= 1
                                            AppUpdateManager.setRunningFirebaseVersion(context, runningFirebaseCode)
                                            Toast.makeText(context, "Running Firebase version updated to Build $runningFirebaseCode", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.background(Color(0xFF1E1E24), RoundedCornerShape(4.dp)).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Decrease Version",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = runningFirebaseCode.toString(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = {
                                        runningFirebaseCode += 1
                                        AppUpdateManager.setRunningFirebaseVersion(context, runningFirebaseCode)
                                        Toast.makeText(context, "Running Firebase version updated to Build $runningFirebaseCode", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFF1E1E24), RoundedCornerShape(4.dp)).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Increase Version",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                runningFirebaseCode = currentVersionCode
                                AppUpdateManager.setRunningFirebaseVersion(context, runningFirebaseCode)
                                Toast.makeText(context, "Running Firebase version reset to match package code ($currentVersionCode)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1E)),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("Reset to Package Build Code ($currentVersionCode)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Advanced Configurations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ADVANCED SOURCE SOURCES (GITHUB)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlue,
                            letterSpacing = 0.5.sp
                        )

                        OutlinedTextField(
                            value = githubOwner,
                            onValueChange = {
                                githubOwner = it
                                AppUpdateManager.setGithubOwner(context, it)
                            },
                            label = { Text("GitHub Owner", color = Color.Gray) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = githubRepo,
                            onValueChange = {
                                githubRepo = it
                                AppUpdateManager.setGithubRepo(context, it)
                            },
                            label = { Text("GitHub Repository Name", color = Color.Gray) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
