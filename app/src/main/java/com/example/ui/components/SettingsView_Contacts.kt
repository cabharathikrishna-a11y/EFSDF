package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.core.content.ContextCompat
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SettingsContactsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.syncGoogleContacts(context) { intent ->
                // Avoid looping
            }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: false
        val writeGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] ?: false
        if (!readGranted || !writeGranted) {
            Toast.makeText(context, "Full system contact synchronization requires Contacts permission.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Contacts permissions verified!", Toast.LENGTH_SHORT).show()
        }
    }

    fun hasContactsPermissions(): Boolean {
        val readPerm = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val writePerm = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return readPerm && writePerm
    }

    SettingsPageScope {
        val isContactsSyncPaused by viewModel.isContactsSyncPaused.collectAsState()

        SettingsSubpageWorkspace(
            title = "Contacts Settings",
            description = "Anniversaries and device contact synchronization.",
            onBack = onBack
        ) {
            Text("Search algorithms index display names, emails, and phone indices. Anniversaries are linked to countdown reminders automatically.", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Contacts System Sync",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Synchronize contacts created within Life OS with your Android device's native contacts application in real time.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .clickable {
                                if (isContactsSyncPaused) {
                                    if (!hasContactsPermissions()) {
                                        contactsPermissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.READ_CONTACTS,
                                                android.Manifest.permission.WRITE_CONTACTS
                                            )
                                        )
                                    }
                                }
                                viewModel.setContactsSyncPaused(!isContactsSyncPaused)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pause Real-time Sync",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isContactsSyncPaused) "Real-time updates are PAUSED" else "Real-time updates are ACTIVE",
                                color = if (isContactsSyncPaused) Color.LightGray else WaterBlue,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isContactsSyncPaused,
                            onCheckedChange = { paused ->
                                if (!paused) {
                                    if (!hasContactsPermissions()) {
                                        contactsPermissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.READ_CONTACTS,
                                                android.Manifest.permission.WRITE_CONTACTS
                                            )
                                        )
                                    }
                                }
                                viewModel.setContactsSyncPaused(paused)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WaterBlue,
                                checkedTrackColor = WaterBlue.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (!hasContactsPermissions()) {
                                contactsPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.READ_CONTACTS,
                                        android.Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            } else {
                                viewModel.forceSyncAllContactsToDevice()
                                Toast.makeText(context, "Force-synced app contacts to phone!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("force_sync_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Save App Contacts to Phone", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    Text(
                        text = "Notes: Syncing transfers First name, Last name, Phone, Email, and Profile picture to raw system contacts. Offline changes won't fetch system contact updates, but editing locally overrides whatever is on the device.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Account and Cloud Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Cloud Google Contacts Sync",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Connect a Google account to save, sync and backup contacts with Google Contacts Cloud services.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    val googleAccount = remember { com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context) }
                    val defaultEmail = googleAccount?.email ?: "cabharathikrishna@gmail.com"
                    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
                    var contactsAccount by remember { mutableStateOf(prefs.getString("selected_contacts_account", defaultEmail)) }
                    var contactsGroup by remember { mutableStateOf(prefs.getString("selected_contacts_group", "LifeOS Contacts Group")) }
                    var autoCloudBackup by remember { mutableStateOf(prefs.getBoolean("auto_cloud_contacts_backup", true)) }

                    LaunchedEffect(Unit) {
                        if (prefs.getString("selected_contacts_account", null) == null) {
                            prefs.edit().putString("selected_contacts_account", defaultEmail).apply()
                        }
                    }

                    val googleSignInLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == android.app.Activity.RESULT_OK) {
                            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            try {
                                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                val email = account?.email ?: ""
                                if (email.isNotEmpty()) {
                                    contactsAccount = email
                                    prefs.edit().putString("selected_contacts_account", email).apply()
                                    Toast.makeText(context, "Connected to: $email", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Google Account selection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    var groupDropdownExpanded by remember { mutableStateOf(false) }

                    // Google Account Selection
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Google Account for Saving Contacts", color = Color.Gray, fontSize = 11.sp)
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
                                    text = contactsAccount ?: "No Google Account connected",
                                    color = if (contactsAccount != null) Color.White else Color.Gray,
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

                    // Contacts Group Selection
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Target Contacts Directory / Label", color = Color.Gray, fontSize = 11.sp)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .clickable { groupDropdownExpanded = true }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contactsGroup ?: "Default My Contacts",
                                    color = if (contactsGroup != null) Color.White else Color.Gray,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.Gray
                                )
                            }

                            DropdownMenu(
                                expanded = groupDropdownExpanded,
                                onDismissRequest = { groupDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1B1B22))
                            ) {
                                val groups = listOf("LifeOS Contacts Group", "My Contacts", "Work Contacts", "Family & Friends Label")
                                groups.forEach { grp ->
                                    DropdownMenuItem(
                                        text = { Text(grp, color = Color.White) },
                                        onClick = {
                                            contactsGroup = grp
                                            prefs.edit().putString("selected_contacts_group", grp).apply()
                                            groupDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Auto cloud backup toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .clickable {
                                autoCloudBackup = !autoCloudBackup
                                prefs.edit().putBoolean("auto_cloud_contacts_backup", autoCloudBackup).apply()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Sync to Google Drive",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Automatically upload contact backups to connected Google account",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoCloudBackup,
                            onCheckedChange = { value ->
                                autoCloudBackup = value
                                prefs.edit().putBoolean("auto_cloud_contacts_backup", value).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WaterBlue,
                                checkedTrackColor = WaterBlue.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val googleContactsSyncStatus by viewModel.googleContactsSyncStatus.collectAsState()

                    Button(
                        onClick = {
                            viewModel.syncGoogleContacts(context) { intent ->
                                googleAuthLauncher.launch(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sync_google_contacts_now_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (googleContactsSyncStatus == "Syncing...") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing with Google...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Google Contacts Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (googleContactsSyncStatus.isNotEmpty() && googleContactsSyncStatus != "Ready") {
                        val isSuccess = googleContactsSyncStatus.contains("successful", ignoreCase = true)
                        Text(
                            text = "Sync Status: $googleContactsSyncStatus",
                            color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFE57373),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
