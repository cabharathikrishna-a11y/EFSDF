package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsUserInfoPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    SettingsPageScope {
        SettingsSubpageWorkspace(
            title = "User Info",
            description = "Update your profile name, nickname, and emoji picture.",
            onBack = onBack
        ) {
            val currentUser = viewModel.currentUserRemote.collectAsState().value
            var name by remember { mutableStateOf(currentUser?.name ?: "") }
            var nickname by remember { mutableStateOf(currentUser?.nickname ?: "") }
            var emoji by remember { mutableStateOf(currentUser?.emoji ?: "") }
            var statusMsg by remember { mutableStateOf<String?>(null) }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WaterBlue,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                ProfilePicEditor(
                    initialValue = emoji.ifBlank { "👨‍💻" },
                    onValueChange = { emoji = it }
                )
                
                if (statusMsg != null) {
                    Text(statusMsg!!, color = if (statusMsg!!.startsWith("Updated")) Color.Green else Color.Red, fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        if (name.isBlank() || emoji.isBlank()) {
                            statusMsg = "Name and Emoji are mandatory"
                            return@Button
                        }
                        viewModel.completeProfileSetup(name, nickname, emoji)
                        statusMsg = "Updated Successfully!"
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Save Details")
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Firebase Diagnostics",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Test the Firebase Crashlytics reporting SDK integration dynamically by forcing a JVM runtime crash.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        throw RuntimeException("Test Crashlytics Setup: Life OS Forced Crash")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Trigger Test Crash (Crashlytics)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


