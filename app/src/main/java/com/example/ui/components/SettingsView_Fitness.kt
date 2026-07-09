package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel
import com.example.ui.theme.WaterBlue
import kotlinx.coroutines.launch

@Composable
fun SettingsFitnessSyncTrendsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleFitSyncStatus by viewModel.googleFitSyncStatus.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Subpage Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "GOOGLE FIT SYNC",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Synchronize activity data directly with your Google Fit account",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
        
        HorizontalDivider(color = Color(0xFF1A1A1E), thickness = 1.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GoogleSyncTab(
                statusMessage = googleFitSyncStatus,
                onConnectFit = {
                    viewModel.connectAndSyncGoogleFit(context)
                },
                onClearCache = {
                    coroutineScope.launch {
                        viewModel.updateHealthMetric(
                            steps = 0,
                            sleepMinutes = 0,
                            waterMl = 0,
                            caloriesBurned = 0,
                            activeMinutes = 0
                        )
                        Toast.makeText(context, "Local metrics reset to baseline.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
