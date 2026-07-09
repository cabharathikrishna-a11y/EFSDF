package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.CircleShape
import java.io.File
import java.io.FileOutputStream
import android.content.Context

@Composable
fun SettingsGeneralSystemPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    SettingsPageScope {
        val tabOrder by viewModel.tabOrder.collectAsState()
        val tabBarOrientation by viewModel.tabBarOrientation.collectAsState()
        val additionalReminderTimes by viewModel.additionalReminderTimes.collectAsState()
        val antiBurnScreenEnabled by viewModel.antiBurnScreenEnabled.collectAsState()
        val hiddenTabs by viewModel.hiddenTabs.collectAsState()
        val allDayNotificationEnabled by viewModel.allDayNotificationEnabled.collectAsState()
        val allDayNotificationTime by viewModel.allDayNotificationTime.collectAsState()
        val onThisDayNotificationEnabled by viewModel.onThisDayNotificationEnabled.collectAsState()
        val onThisDayNotificationTime by viewModel.onThisDayNotificationTime.collectAsState()
        val onThisDayOnScreenEnabled by viewModel.onThisDayOnScreenEnabled.collectAsState()
        var tempOrder by remember(tabOrder) { mutableStateOf(tabOrder) }

        // General System Page
        SettingsSubpageWorkspace(
            title = "General System Settings",
            description = "Configure core systems, tab layout orientation and app reordering.",
            onBack = onBack
        ) {
            // Alignment Options Column Block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Navigation Bar Position",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Set the tab position: left (sidebar), right, top, bottom, or legacy profiles.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("left", "right").forEach { mode ->
                                val isSelected = tabBarOrientation.lowercase() == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) WaterBlue else Color(0xFF141414))
                                        .clickable { viewModel.updateTabBarOrientation(mode) }
                                        .padding(vertical = 12.dp)
                                        .testTag("tab_mode_${mode}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.uppercase(), color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("top", "bottom").forEach { mode ->
                                val isSelected = tabBarOrientation.lowercase() == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) WaterBlue else Color(0xFF141414))
                                        .clickable { viewModel.updateTabBarOrientation(mode) }
                                        .padding(vertical = 12.dp)
                                        .testTag("tab_mode_${mode}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.uppercase(), color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab order customization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reorder navigation tabs",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Move tabs up and down to customize their layout position.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tempOrder.forEachIndexed { index, screen ->
                            val label = when (screen) {
                                Screen.TASKS -> "Tasks"
                                Screen.CALENDAR -> "Calendar"
                                Screen.TIMER -> "Timer"
                                Screen.HABITS -> "Habits"
                                Screen.COUNTDOWN -> "Countdown"
                                Screen.JOURNAL -> "Journal"
                                Screen.KEEP_NOTES -> "Keep Notes"
                                Screen.CONTACTS -> "Contacts"
                                Screen.FILE_EXPLORER -> "File Explorer"
                                Screen.FINANCES -> "Finances"
                                Screen.DEEPA_AI -> "Deepa AI"
                                Screen.SEARCH -> "Search"
                                Screen.ANALYTICS -> "Analytics"
                                Screen.SETTINGS -> "Settings"
                                Screen.LOGIN -> "Login"
                                Screen.PROFILE_SETUP -> "Profile Setup"
                                Screen.PERMISSION_ONBOARDING -> "Permissions Onboarding"
                                Screen.CALENDAR_OPTIMIZATION_ONBOARDING -> "Calendar Optimization"
                                Screen.HEALTH -> "Health"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF141414), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val isTabHidden = hiddenTabs.contains(screen)
                                    IconButton(
                                        onClick = { viewModel.toggleTabVisibility(screen) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTabHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Visibility",
                                            tint = if (isTabHidden) Color.Gray else WaterBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = tempOrder.toMutableList()
                                                val tmp = list.removeAt(index)
                                                list.add(index - 1, tmp)
                                                tempOrder = list
                                            }
                                        },
                                        modifier = Modifier.size(28.dp),
                                        enabled = index > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) Color.White else Color.DarkGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < tempOrder.size - 1) {
                                                val list = tempOrder.toMutableList()
                                                val tmp = list.removeAt(index)
                                                list.add(index + 1, tmp)
                                                tempOrder = list
                                            }
                                        },
                                        modifier = Modifier.size(28.dp),
                                        enabled = index < tempOrder.size - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (index < tempOrder.size - 1) Color.White else Color.DarkGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.saveTabOrder(tempOrder) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("save_tab_order_subpage_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black)
                        ) {
                            Text("SAVE TAB ORDER", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Silent Mode Card
            val masterSilentMode by viewModel.masterSilentModeEnabled.collectAsState()
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
                        Text("MASTER SILENT MODE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("When enabled, all app reminders, sounds, and vibrations are completely silenced.", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = masterSilentMode,
                        onCheckedChange = { viewModel.updateMasterSilentModeEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("master_silent_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Staging Mode Card
            val isStagingMode by viewModel.isStagingMode.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("staging_mode_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Staging & Mock Users", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Show simulated friends and mock records (madhavan, shalini, subash).", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isStagingMode,
                        onCheckedChange = {
                            viewModel.setStagingMode(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("staging_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Display Background Image Card
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            var useCustomReminderBg by remember { mutableStateOf(sharedPrefs.getBoolean("use_custom_reminder_bg", false)) }
            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
            var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            
            // Cropper adjustments
            var zoomScale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var isCroppingMode by remember { mutableStateOf(false) }
            
            // Live preview image state
            var previewBitmap by remember(useCustomReminderBg) {
                mutableStateOf<Bitmap?>(
                    if (useCustomReminderBg) {
                        try {
                            val file = File(context.filesDir, "reminder_bg.jpg")
                            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                )
            }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    selectedImageUri = uri
                    val bitmap = uriToBitmap(context, uri)
                    if (bitmap != null) {
                        loadedBitmap = bitmap
                        zoomScale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        isCroppingMode = true
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("reminder_background_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REMINDER BACKGROUND IMAGE",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose and crop a custom portrait image to display on the full-screen reminder overlay.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use Custom Image", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = useCustomReminderBg,
                            onCheckedChange = { isChecked ->
                                useCustomReminderBg = isChecked
                                sharedPrefs.edit().putBoolean("use_custom_reminder_bg", isChecked).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("use_custom_reminder_bg_switch")
                        )
                    }

                    if (useCustomReminderBg) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(72.dp, 128.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141414))
                                    .border(BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (previewBitmap != null) {
                                    Image(
                                        bitmap = previewBitmap!!.asImageBitmap(),
                                        contentDescription = "Background Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CHOOSE IMAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (previewBitmap != null) {
                                    Button(
                                        onClick = {
                                            try {
                                                val file = File(context.filesDir, "reminder_bg.jpg")
                                                if (file.exists()) file.delete()
                                                previewBitmap = null
                                                useCustomReminderBg = false
                                                sharedPrefs.edit().putBoolean("use_custom_reminder_bg", false).apply()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RESET TO DEFAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // High-fidelity full screen cropping dialog
            if (isCroppingMode && loadedBitmap != null) {
                Dialog(
                    onDismissRequest = { isCroppingMode = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = false
                    )
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        val screenWidth = maxWidth
                        val screenHeight = maxHeight
                        val density = LocalDensity.current
                        
                        // Calculate portrait 9:16 cropping frame dynamically to fit safe screen boundaries
                        val cropWidthDp = minOf(screenWidth - 48.dp, (screenHeight - 180.dp) * (9f / 16f))
                        val cropHeightDp = cropWidthDp * (16f / 9f)

                        val cropWidthPx = with(density) { cropWidthDp.toPx() }
                        val cropHeightPx = with(density) { cropHeightDp.toPx() }

                        val bmWidth = loadedBitmap!!.width.toFloat()
                        val bmHeight = loadedBitmap!!.height.toFloat()

                        // Fit image base scale to completely cover the crop frame initially
                        val baseScale = maxOf(cropWidthPx / bmWidth, cropHeightPx / bmHeight)
                        val layoutWidthDp = with(density) { (bmWidth * baseScale).toDp() }
                        val layoutHeightDp = with(density) { (bmHeight * baseScale).toDp() }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(loadedBitmap) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        zoomScale = (zoomScale * zoom).coerceIn(1f, 5f)
                                        
                                        val totalScale = baseScale * zoomScale
                                        val wz = bmWidth * totalScale
                                        val hz = bmHeight * totalScale
                                        
                                        val maxOffsetX = (wz - cropWidthPx) / 2f
                                        val maxOffsetY = (hz - cropHeightPx) / 2f
                                        
                                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = loadedBitmap!!.asImageBitmap(),
                                contentDescription = "Cropping Source",
                                modifier = Modifier
                                    .size(width = layoutWidthDp, height = layoutHeightDp)
                                    .graphicsLayer(
                                        scaleX = zoomScale,
                                        scaleY = zoomScale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    ),
                                contentScale = ContentScale.Crop
                            )

                            // Semi-translucent mask overlay with high-contrast portrait cropping window
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
                                }
                                val cropPath = androidx.compose.ui.graphics.Path().apply {
                                    addRect(androidx.compose.ui.geometry.Rect(
                                        canvasWidth / 2f - cropWidthPx / 2f,
                                        canvasHeight / 2f - cropHeightPx / 2f,
                                        canvasWidth / 2f + cropWidthPx / 2f,
                                        canvasHeight / 2f + cropHeightPx / 2f
                                    ))
                                }
                                
                                val maskPath = androidx.compose.ui.graphics.Path.combine(
                                    androidx.compose.ui.graphics.PathOperation.Difference,
                                    path,
                                    cropPath
                                )
                                
                                drawPath(
                                    path = maskPath,
                                    color = Color.Black.copy(alpha = 0.75f)
                                )
                                
                                // White fine boundary outline for the 9:16 target window
                                drawRect(
                                    color = Color.White.copy(alpha = 0.8f),
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        canvasWidth / 2f - cropWidthPx / 2f,
                                        canvasHeight / 2f - cropHeightPx / 2f
                                    ),
                                    size = androidx.compose.ui.geometry.Size(cropWidthPx, cropHeightPx),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                                )
                            }

                            // Dynamic instruction text at top of screen
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Position Background",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Drag to reposition • Pinch with two fingers to resize",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Interactive controls at bottom
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 48.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isCroppingMode = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }

                                Button(
                                    onClick = {
                                        val cropped = cropBitmapToPortraitAndScale(
                                            loadedBitmap!!,
                                            zoomScale,
                                            offsetX,
                                            offsetY,
                                            cropWidthPx,
                                            cropHeightPx,
                                            baseScale
                                        )
                                        saveBitmapToFile(context, cropped)
                                        
                                        // Update preview & enable custom BG option
                                        useCustomReminderBg = true
                                        sharedPrefs.edit().putBoolean("use_custom_reminder_bg", true).apply()
                                        
                                        try {
                                            val file = File(context.filesDir, "reminder_bg.jpg")
                                            previewBitmap = if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                                        } catch (e: Exception) {
                                            previewBitmap = null
                                        }
                                        
                                        isCroppingMode = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply & Crop", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper methods for Custom Reminder Background Bitmap crop & conversion
private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

private fun cropBitmapToPortraitAndScale(
    bitmap: Bitmap,
    zoomScale: Float,
    offsetX: Float,
    offsetY: Float,
    rectWidthPx: Float,
    rectHeightPx: Float,
    baseScale: Float
): Bitmap {
    val bitmapWidth = bitmap.width.toFloat()
    val bitmapHeight = bitmap.height.toFloat()
    
    val totalScale = baseScale * zoomScale
    val cropWidthOnBitmap = rectWidthPx / totalScale
    val cropHeightOnBitmap = rectHeightPx / totalScale
    
    // Calculate top-left coordinates on the original bitmap
    val startX = bitmapWidth / 2f - (offsetX + rectWidthPx / 2f) / totalScale
    val startY = bitmapHeight / 2f - (offsetY + rectHeightPx / 2f) / totalScale
    
    val startXCoerced = startX.toInt().coerceIn(0, (bitmapWidth - cropWidthOnBitmap).toInt().coerceAtLeast(0))
    val startYCoerced = startY.toInt().coerceIn(0, (bitmapHeight - cropHeightOnBitmap).toInt().coerceAtLeast(0))
    
    val widthCoerced = cropWidthOnBitmap.toInt().coerceIn(50, bitmap.width - startXCoerced)
    val heightCoerced = cropHeightOnBitmap.toInt().coerceIn(50, bitmap.height - startYCoerced)
    
    val cropped = Bitmap.createBitmap(bitmap, startXCoerced, startYCoerced, widthCoerced, heightCoerced)
    
    // Scale to a high quality but memory friendly resolution (540 x 960)
    val scaled = Bitmap.createScaledBitmap(cropped, 540, 960, true)
    
    if (cropped != bitmap) {
        cropped.recycle()
    }
    return scaled
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.filesDir, "reminder_bg.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

