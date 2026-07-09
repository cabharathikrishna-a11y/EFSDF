package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import com.example.util.MapDownloadManager
import kotlinx.coroutines.launch

@Composable
fun SettingsJournalPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val downloadedStatus = remember {
        mutableStateMapOf<String, Boolean>().apply {
            MapDownloadManager.zones.forEach { zone ->
                this[zone.id] = MapDownloadManager.isMapFileDownloaded(context, zone.id)
            }
        }
    }

    val activeDownloadingZoneId by MapDownloadManager.activeDownloadingZoneId.collectAsState()
    val downloadProgress by MapDownloadManager.downloadProgress.collectAsState()
    val downloadError by MapDownloadManager.downloadError.collectAsState()

    LaunchedEffect(activeDownloadingZoneId) {
        if (activeDownloadingZoneId == null) {
            MapDownloadManager.zones.forEach { zone ->
                downloadedStatus[zone.id] = MapDownloadManager.isMapFileDownloaded(context, zone.id)
            }
        }
    }

    SettingsPageScope {
        val onThisDayNotificationEnabled by viewModel.onThisDayNotificationEnabled.collectAsState()
        val onThisDayNotificationTime by viewModel.onThisDayNotificationTime.collectAsState()
        val onThisDayOnScreenEnabled by viewModel.onThisDayOnScreenEnabled.collectAsState()

        SettingsSubpageWorkspace(
            title = "Life Journal Settings",
            description = "Configurations, storage maps.",
            onBack = onBack
        ) {
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Journal records and metadata are completely local and private to your device.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

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
                            Text("On This Day Notifications", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sends a notification reminding you of historical journal entries written on this day in history.", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = onThisDayNotificationEnabled,
                            onCheckedChange = { viewModel.updateOnThisDayNotificationEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = WaterBlue),
                            modifier = Modifier.testTag("on_this_day_notification_switch")
                        )
                    }
                    
                    if (onThisDayNotificationEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Notification Trigger Time", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = onThisDayNotificationTime,
                            onValueChange = { viewModel.updateOnThisDayNotificationTime(it) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            placeholder = { Text("e.g. 09:00 AM or 18:30", color = Color.DarkGray, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("on_this_day_notification_time_input")
                        )
                    }

                    HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("On Screen Reminders", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Displays an on-screen dialog when today's historic journal entries exist.", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = onThisDayOnScreenEnabled,
                            onCheckedChange = { viewModel.updateOnThisDayOnScreenEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = WaterBlue),
                            modifier = Modifier.testTag("on_this_day_onscreen_switch")
                        )
                    }
                }
            }

            // India Offline Map Manager Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Offline India Map Manager",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage high-fidelity vector maps of India for completely offline travel/journal location display. Files are preserved during updates but deleted upon uninstallation.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (downloadError != null) {
                        Text(
                            text = "Download Error: $downloadError",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    MapDownloadManager.zones.forEachIndexed { index, zone ->
                        if (index > 0) {
                            HorizontalDivider(color = Color(0xFF1E1E22), modifier = Modifier.padding(vertical = 12.dp))
                        }

                        val isDownloaded = downloadedStatus[zone.id] ?: false
                        val isCurrentDownloading = activeDownloadingZoneId == zone.id

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = zone.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (zone.id == "southern") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(WaterBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PRIMARY",
                                                color = WaterBlue,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "File size: ${zone.size}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )

                                if (isCurrentDownloading) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val pct = ((downloadProgress ?: 0f) * 100).toInt()
                                    LinearProgressIndicator(
                                        progress = { downloadProgress ?: 0f },
                                        color = WaterBlue,
                                        trackColor = Color.DarkGray,
                                        modifier = Modifier.fillMaxWidth().height(4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Downloading: $pct%",
                                        color = WaterBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (isDownloaded) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Downloaded & Ready",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            if (isDownloaded) {
                                IconButton(
                                    onClick = {
                                        MapDownloadManager.deleteMap(context, zone.id)
                                        downloadedStatus[zone.id] = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Map",
                                        tint = Color(0xFFE53935)
                                    )
                                }
                            } else if (isCurrentDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = WaterBlue,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                val anyDownloading = activeDownloadingZoneId != null
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            MapDownloadManager.downloadMap(context, zone.id)
                                        }
                                    },
                                    enabled = !anyDownloading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WaterBlue,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color(0xFF1E1E22),
                                        disabledContentColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "Download",
                                        fontSize = 11.sp,
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
