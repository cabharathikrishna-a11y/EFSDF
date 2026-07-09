package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SettingsFileExplorerPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isSdCardConnected by remember {
        mutableStateOf(com.example.util.StorageHelper.isExternalStorageConnected(context))
    }
    var preferredStorage by remember {
        mutableStateOf(com.example.util.StorageHelper.getPreferredStorage(context))
    }

    LaunchedEffect(Unit) {
        isSdCardConnected = com.example.util.StorageHelper.isExternalStorageConnected(context)
    }

    SettingsSubpageWorkspace(
        title = "File Explorer Settings",
        description = "Volume paths.",
        onBack = onBack
    ) {
        Text(
            text = "Secure physical file indices reside directly under context sandbox paths.",
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isSdCardConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Primary Data & Media Storage",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select preferred storage target for application records and media files.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    listOf(
                        Pair("internal", "Internal Device Storage"),
                        Pair("sd_card", "Removable SD Card Storage")
                    ).forEach { (key, label) ->
                        val isSelected = preferredStorage == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WaterBlue.copy(alpha = 0.12f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) WaterBlue else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    preferredStorage = key
                                    com.example.util.StorageHelper.setPreferredStorage(context, key)
                                    Toast.makeText(
                                        context,
                                        "Storage target updated to ${if (key == "sd_card") "SD Card" else "Internal Storage"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (key == "sd_card") Icons.Default.Build else Icons.Default.Home,
                                    contentDescription = null,
                                    tint = if (isSelected) WaterBlue else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (isSelected) WaterBlue else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    preferredStorage = key
                                    com.example.util.StorageHelper.setPreferredStorage(context, key)
                                    Toast.makeText(
                                        context,
                                        "Storage target updated to ${if (key == "sd_card") "SD Card" else "Internal Storage"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = WaterBlue,
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Drive File Synchronization Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = WaterBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Google Drive Cloud Save Settings",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure preferred Google account and target directory for file backups & attachments.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                val googleAccount = remember { com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context) }
                val defaultEmail = googleAccount?.email ?: "cabharathikrishna@gmail.com"
                val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
                var fileBackupAccount by remember { mutableStateOf(prefs.getString("selected_file_backup_account", defaultEmail)) }
                var fileBackupDir by remember { mutableStateOf(prefs.getString("selected_file_backup_dir", "Root Directory (/)")) }
                var customDirInput by remember { mutableStateOf(prefs.getString("custom_file_backup_dir_path", "")) }

                val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                            val email = account?.email ?: ""
                            if (email.isNotEmpty()) {
                                fileBackupAccount = email
                                prefs.edit().putString("selected_file_backup_account", email).apply()
                                Toast.makeText(context, "Connected to: $email", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Google Account selection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                var dirDropdownExpanded by remember { mutableStateOf(false) }

                // Google Account selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Connected Google Account", color = Color.Gray, fontSize = 11.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .build()
                                    val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                                    client.signOut().addOnCompleteListener {
                                        googleSignInLauncher.launch(client.signInIntent)
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fileBackupAccount ?: "No Account Chosen",
                                color = if (fileBackupAccount != null) Color.White else Color.Gray,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Google Drive folder selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Google Drive Folder", color = Color.Gray, fontSize = 11.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { dirDropdownExpanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fileBackupDir ?: "Root Directory (/)",
                                color = if (fileBackupDir != null) Color.White else Color.Gray,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = dirDropdownExpanded,
                            onDismissRequest = { dirDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF1B1B22))
                        ) {
                            val directories = listOf("Root Directory (/)", "LifeOS_Files", "Personal_Daily_Archive", "Work_Cloud_Backup", "Custom Path...")
                            directories.forEach { dir ->
                                DropdownMenuItem(
                                    text = { Text(dir, color = Color.White) },
                                    onClick = {
                                        fileBackupDir = dir
                                        prefs.edit().putString("selected_file_backup_dir", dir).apply()
                                        dirDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (fileBackupDir == "Custom Path...") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Custom Folder Path", color = Color.Gray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = customDirInput ?: "",
                            onValueChange = { newValue ->
                                customDirInput = newValue
                                prefs.edit().putString("custom_file_backup_dir_path", newValue).apply()
                            },
                            placeholder = { Text("e.g. Backups/Attachments/2026", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = WaterBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
