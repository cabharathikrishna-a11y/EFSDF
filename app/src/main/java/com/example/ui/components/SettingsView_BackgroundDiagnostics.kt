package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SettingsBackgroundDiagnosticsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isIgnoringOptimizations by remember { mutableStateOf(checkBatteryOptimizations(context)) }
    var selectedBrandTab by remember { mutableStateOf("samsung") }
    var fcmToken by remember { mutableStateOf("Fetching FCM Token...") }
    var isFcmWorking by remember { mutableStateOf<Boolean?>(null) }

    // Re-check optimization status when entering or active
    LaunchedEffect(Unit) {
        isIgnoringOptimizations = checkBatteryOptimizations(context)
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fcmToken = task.result ?: "Token is null"
                    isFcmWorking = true
                } else {
                    fcmToken = "Error: ${task.exception?.message ?: "Unknown error"}"
                    isFcmWorking = false
                }
            }
        } catch (e: Exception) {
            fcmToken = "FCM Error or not initialized: ${e.message}"
            isFcmWorking = false
        }
    }

    SettingsPageScope {
        SettingsSubpageWorkspace(
            title = "Diagnostics & Background Sync",
            description = "Fix stopwatch freezing and background focus recording issues on Samsung, Oppo, Lenovo, and Motorola.",
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
                    border = BorderStroke(1.dp, if (isIgnoringOptimizations) Color(0xFF2E7D32) else Color(0xFFC62828)),
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
                                .background(
                                    if (isIgnoringOptimizations) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    else Color(0xFFC62828).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIgnoringOptimizations) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Status Icon",
                                tint = if (isIgnoringOptimizations) Color(0xFF4CAF50) else Color(0xFFE53935),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isIgnoringOptimizations) "BACKGROUND CONFIGURATION: OPTIMAL" else "BACKGROUND CONFIGURATION: RESTRICTED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIgnoringOptimizations) Color(0xFF4CAF50) else Color(0xFFE53935),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isIgnoringOptimizations) {
                                "Your device allows Life OS to operate with unrestricted background access. Stopwatches and focus timers will tick continuously, even when your screen is locked."
                            } else {
                                "Android's battery-saving system is active for Life OS. On many devices (especially Samsung, Oppo, Lenovo, Motorola), the system freezes background processes or terminates timers when the screen locks."
                            },
                            fontSize = 11.5.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                requestIgnoreBatteryOptimizations(context)
                                // Refresh status slightly after launching
                                isIgnoringOptimizations = checkBatteryOptimizations(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIgnoringOptimizations) Color(0xFF2E7D32) else WaterBlue,
                                contentColor = if (isIgnoringOptimizations) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Battery Settings",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isIgnoringOptimizations) "Open Battery Settings Again" else "Disable Battery Optimization",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 2. Explanations of why it freezes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070709)),
                    border = BorderStroke(1.dp, Color(0xFF1E1E22)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WHY TIMERS & STOPWATCHES FREEZE",
                            color = WaterBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. CPU Standby: When the screen turns off, Android suspends CPU cores to save power, freezing local clocks.\n" +
                                    "2. Aggressive Custom Skins: Samsung (OneUI), Oppo (ColorOS), Lenovo, and Motorola utilize aggressive OEM managers that kill background services or suppress periodic network/local state sync requests.\n" +
                                    "3. Network Restrictions: In sleep mode, background cellular and Wi-Fi sync are deferred, meaning focus data updates aren't logged in real-time until you unlock.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // 3. OEM Specific Workaround Tabs
                Text(
                    text = "OEM-SPECIFIC FIXES",
                    color = WaterBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                // Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C0C0E), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "samsung" to "Samsung",
                        "oppo" to "Oppo",
                        "lenovo" to "Lenovo",
                        "motorola" to "Moto",
                        "others" to "Others"
                    ).forEach { (key, label) ->
                        val isSelected = selectedBrandTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WaterBlue else Color.Transparent)
                                .clickable { selectedBrandTab = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Workaround Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
                    border = BorderStroke(1.dp, Color(0xFF1E1E22)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (selectedBrandTab) {
                            "samsung" -> {
                                Text("Samsung (One UI) Guidelines", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Set Battery to Unrestricted:\n" +
                                            "   • Long press the Life OS app icon -> Tap \"App info\" (or Go to Settings -> Apps -> Life OS).\n" +
                                            "   • Tap \"Battery\".\n" +
                                            "   • Select \"Unrestricted\" (the default is usually Optimized or Restricted).\n\n" +
                                            "2. Add to Never Sleeping Apps:\n" +
                                            "   • Go to Settings -> Battery and device care -> Battery -> Background usage limits.\n" +
                                            "   • Tap \"Never sleeping apps\".\n" +
                                            "   • Click \"+\" in the top right, select \"Life OS\" and click Add.",
                                    color = Color.LightGray,
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            "oppo" -> {
                                Text("Oppo / OnePlus / Realme (ColorOS) Guidelines", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Enable Background Execution:\n" +
                                            "   • Settings -> Apps -> App management -> Life OS.\n" +
                                            "   • Tap \"Battery usage\".\n" +
                                            "   • Toggle ON \"Allow background activity\" and \"Allow auto-launch\".\n\n" +
                                            "2. Lock in Recent Apps:\n" +
                                            "   • Swipe up to open your Recent Apps screen.\n" +
                                            "   • Tap the three-dots/options icon above the Life OS card.\n" +
                                            "   • Select \"Lock\" to prevent the ColorOS memory sweeper from cleaning the background thread.",
                                    color = Color.LightGray,
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            "lenovo" -> {
                                Text("Lenovo Guidelines", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Disable App Restriction:\n" +
                                            "   • Open Settings -> Apps -> Life OS.\n" +
                                            "   • Tap \"Battery\".\n" +
                                            "   • Select \"Unrestricted\".\n\n" +
                                            "2. Lenovo Power Management (ZUI):\n" +
                                            "   • Open the built-in ZUI Security / Power Manager app.\n" +
                                            "   • Look for \"Auto-start\" or \"Background app management\".\n" +
                                            "   • Allow Life OS to start and stay active in the background.",
                                    color = Color.LightGray,
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            "motorola" -> {
                                Text("Motorola Guidelines", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Disable Battery Saver Optimization:\n" +
                                            "   • Go to Settings -> Apps & notifications -> Special app access -> Battery optimization.\n" +
                                            "   • Filter by \"All apps\", select \"Life OS\" and choose \"Don't optimize\".\n\n" +
                                            "2. Enable Unrestricted Usage:\n" +
                                            "   • Go to Settings -> Apps -> Life OS -> Battery.\n" +
                                            "   • Select \"Unrestricted\" to grant standard foreground service background permissions without system suspension.",
                                    color = Color.LightGray,
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            "others" -> {
                                Text("General & Other Devices (Xiaomi, Vivo, etc.)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Xiaomi (MIUI / HyperOS):\n" +
                                            "   • Settings -> Apps -> Manage apps -> Life OS -> Battery saver -> Select \"No restrictions\". Also enable \"Autostart\".\n\n" +
                                            "2. Vivo (FuntouchOS):\n" +
                                            "   • Settings -> Battery -> High background power consumption -> Enable \"Life OS\".\n\n" +
                                            "3. General Troubleshooting:\n" +
                                            "   • Ensure your device is not in system-wide \"Power Saver Mode\" or \"Ultra Battery Saver\".\n" +
                                            "   • Keep the background notification for the \"KeepAliveService\" active. It prevents the system from categorizing Life OS as idle.",
                                    color = Color.LightGray,
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // 4. Test Keep-Alive Service Running Status
                val isServiceActive = isKeepAliveServiceRunning(context)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    border = BorderStroke(1.dp, Color(0xFF222226)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FOREGROUND KEEP-ALIVE SERVICE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isServiceActive) "Active and holding a background notification channel." else "Not active or has been suspended.",
                                color = if (isServiceActive) Color(0xFF4CAF50) else Color.Gray,
                                fontSize = 10.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isServiceActive) Color(0xFF4CAF50) else Color(0xFFD32F2F))
                        )
                    }
                }

                // 5. Firebase Cloud Messaging (FCM) Status
                val clipboardManager = LocalClipboardManager.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    border = BorderStroke(1.dp, if (isFcmWorking == true) Color(0xFF2E7D32) else Color(0xFF222226)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FIREBASE CLOUD MESSAGING (FCM)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (isFcmWorking) {
                                        true -> "FCM Service initialized and active."
                                        false -> "FCM failed or Google Play Services missing."
                                        else -> "Retrieving push notification status..."
                                    },
                                    color = if (isFcmWorking == true) Color(0xFF4CAF50) else Color.Gray,
                                    fontSize = 10.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (isFcmWorking) {
                                            true -> Color(0xFF4CAF50)
                                            false -> Color(0xFFD32F2F)
                                            else -> Color.Gray
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "FCM REGISTRATION TOKEN",
                            color = Color(0xFF03A9F4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF15151A), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF22222A), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = fcmToken,
                                color = Color.LightGray,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (isFcmWorking == true) {
                                    clipboardManager.setText(AnnotatedString(fcmToken))
                                    Toast.makeText(context, "FCM Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "FCM Token is not available to copy.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFcmWorking == true) Color(0xFF0288D1) else Color(0xFF222226),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = "Copy FCM Token",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun checkBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else {
        true
    }
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Battery settings not required on this Android version.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open settings automatically.", Toast.LENGTH_LONG).show()
        }
    }
}

private fun isKeepAliveServiceRunning(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    @Suppress("DEPRECATION")
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if ("com.example.service.KeepAliveService" == service.service.className) {
            return true
        }
    }
    return false
}
