package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.WaterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDeepLinksPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "INTEGRATIONS & DEEP LINKS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09090C))
            )
        },
        containerColor = Color(0xFF09090C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D11)),
                border = BorderStroke(1.dp, Color(0xFF222225)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Integration & Shortcuts Center",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "Life OS supports powerful custom URI deep linking. You can create widget buttons, custom shortcuts, or automation scripts to trigger app navigation or run silent background synchronizations directly from your external home screens.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // App Logo link Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D11)),
                border = BorderStroke(1.dp, Color(0xFF222225)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Application Logo Assets",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Use these high-resolution launcher logo links to build shortcuts or launcher badges.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    val logoUrl = "https://raw.githubusercontent.com/cabharathikrishna/Life.os/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF16161B), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF282830), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Official Logo URL",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(logoUrl))
                                    Toast.makeText(context, "Copied Logo URL!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Logo", tint = WaterBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = logoUrl,
                            color = WaterBlue,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Screen Navigation Links
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D11)),
                border = BorderStroke(1.dp, Color(0xFF222225)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Screen Deep Links",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Tap any link to copy it. When triggered externally, the app opens directly to that page.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    val screens = listOf(
                        "Home Dashboard" to "lifeos://home",
                        "Tasks Engine" to "lifeos://tasks",
                        "Calendar Planner" to "lifeos://calendar",
                        "Timer focus" to "lifeos://timer",
                        "Habits Tracker" to "lifeos://habits",
                        "Life Journal" to "lifeos://journal",
                        "Contacts Directory" to "lifeos://contacts",
                        "File Explorer" to "lifeos://file_explorer",
                        "Financial Ledger" to "lifeos://finances",
                        "Countdown & Alerts" to "lifeos://countdown",
                        "Deepa AI Assistant" to "lifeos://ai_chat",
                        "Onboarding Screen" to "lifeos://onboarding",
                        "System Settings" to "lifeos://settings",
                        "Analytics Center" to "lifeos://analytics",
                        "Search Engine" to "lifeos://search"
                    )

                    screens.forEach { (name, link) ->
                        DeepLinkRow(name = name, uri = link, clipboardManager = clipboardManager, context = context)
                    }
                }
            }

            // Action Trigger Links
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D11)),
                border = BorderStroke(1.dp, Color(0xFF222225)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Automation Action Triggers",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Launch these actions silently or navigate instantly to synchronize cloud backends.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    val actions = listOf(
                        "Sync Google Calendar" to "lifeos://action/sync_calendar",
                        "Auto-backup Google Drive" to "lifeos://action/backup_drive",
                        "Force Contacts Device Sync" to "lifeos://action/force_contacts_sync",
                        "Check App System Updates" to "lifeos://action/check_updates"
                    )

                    actions.forEach { (name, link) ->
                        DeepLinkRow(name = name, uri = link, clipboardManager = clipboardManager, context = context)
                    }
                }
            }

            // Integration Help Info
            Text(
                "Tip: You can use these deep links inside macro apps (like Tasker, Macrodroid, or Bixby Routines) or launcher shortcuts to trigger custom smart buttons in widgets on your Android desktop.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DeepLinkRow(
    name: String,
    uri: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF16161B))
            .clickable {
                clipboardManager.setText(AnnotatedString(uri))
                Toast.makeText(context, "Copied link for $name!", Toast.LENGTH_SHORT).show()
            }
            .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(uri, color = WaterBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy link",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
