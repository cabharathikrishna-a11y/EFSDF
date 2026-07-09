package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.util.FocusTimerManager

data class PeerFocusInfo(
    val username: String,
    val displayName: String,
    val emoji: String,
    val isFocusing: Boolean,
    val liveFocusedSeconds: Int,
    val currentTask: String?,
    val currentTag: String? = null,
    val isMe: Boolean = false,
    val focusStatus: String = "idle"
)

@Composable
fun FriendsFocusPill(
    viewModel: AppViewModel,
    onClick: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()

    // Filter active users who are focusing
    val focusingUsers = allUsers.filter {
        it.value.isFocusing == true && 
        it.key != "admin" &&
        it.value.status != "logged_out" &&
        it.value.status != "uninstalled" &&
        it.value.isGoogleUser == true
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(width = 0.8.dp, color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("friends_focus_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (focusingUsers.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "No one focusing",
                    tint = Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "No one is focusing",
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    focusingUsers.values.forEach { user ->
                        UserAvatar(
                            emojiOrBase64 = user.emoji,
                            fontSize = 14.sp,
                            size = 20.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsFocusDetailsDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()
    val showElapsedTimeDialog by viewModel.showElapsedTimeDialog.collectAsState()

    val GoldRank = Color(0xFFFFD700)
    val SilverRank = Color(0xFFC0C0C0)
    val BronzeRank = Color(0xFFCD7F32)
    val WaterBlue = Color(0xFF38BDF8)

    var selectedFilter by remember { mutableStateOf("Today") }
    var filterExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("Today", "Past 7 Days", "Past 30 Days", "All Time")

    val todayStr = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    val days = when (selectedFilter) {
        "Today" -> 1
        "Past 7 Days" -> 7
        "Past 30 Days" -> 30
        "All Time" -> 365
        else -> 1
    }

    val targetDates = remember(selectedFilter, todayStr) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dates = mutableListOf<String>()
        val cal = java.util.Calendar.getInstance()
        for (i in 0 until days) {
            dates.add(sdf.format(cal.time))
            cal.add(java.util.Calendar.DATE, -1)
        }
        dates
    }

    fun getSecondsForDate(
        username: String,
        dateStr: String,
        isMe: Boolean,
        peerRemote: com.example.api.UserRemote?,
        currentUnixTime: Long
    ): Int {
        if (dateStr == todayStr) {
            if (isMe) {
                val isLocalFocusing = (FocusTimerManager.isTimerRunning.value || FocusTimerManager.isStopwatchActive.value) && FocusTimerManager.isFocusPhase.value && FocusTimerManager.pendingFocusReview.value == null
                val completedTodaySecs = FocusTimerManager.focusRecords.value.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, todayStr) }
                val pendingSecs = FocusTimerManager.pendingFocusReview.value?.let { FocusTimerManager.getOverlapSecondsForDate(it, todayStr) } ?: 0
                val activeSessionOverlap = if (isLocalFocusing) {
                    val startMs = viewModel.sessionStartTimestamp.value
                    if (startMs != null) {
                        FocusTimerManager.getActiveSessionOverlapSeconds(startMs, todayStr)
                    } else {
                        val currentChunkMs = FocusTimerManager.getCurrentChunkMs()
                        val totalMs = FocusTimerManager.accumulatedSessionTimeMs.value + currentChunkMs
                        (totalMs / 1000).toInt()
                    }
                } else {
                    if (FocusTimerManager.pendingFocusReview.value == null) {
                        (FocusTimerManager.accumulatedSessionTimeMs.value / 1000).toInt()
                    } else {
                        0
                    }
                }

                return completedTodaySecs + pendingSecs + activeSessionOverlap
            } else {
                if (peerRemote != null) {
                    val lastUpdated = peerRemote.lastUpdatedTimestamp ?: 0L
                    val lastUpdatedDateStr = if (lastUpdated > 0) {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(lastUpdated))
                    } else {
                        ""
                    }
                    if (lastUpdatedDateStr.isNotEmpty() && lastUpdatedDateStr != todayStr) {
                        return 0
                    }

                    val isFocusing = peerRemote.isFocusing == true
                    
                    // 1. Calculate the Temporary Bank (Live Session)
                    val liveFocusedSeconds = if (isFocusing && peerRemote.lastResumeTimeMs != null) {
                        val currentChunkMs = (currentUnixTime * 1000) - peerRemote.lastResumeTimeMs!!
                        val totalMs = (peerRemote.accumulatedTimeMs ?: 0L) + maxOf(0L, currentChunkMs)
                        (totalMs / 1000).toInt()
                    } else {
                        ((peerRemote.accumulatedTimeMs ?: 0L) / 1000).toInt()
                    }
                    
                    // 2. Add the Permanent Vault (Completed Records)
                    val completedTodaySecs = peerRemote.todaysFocusRecords?.sumOf { 
                        FocusTimerManager.getOverlapSecondsForDate(it, todayStr) 
                    } ?: 0
                    
                    return liveFocusedSeconds + completedTodaySecs
                } else {
                    return 0
                }
            }
        } else {
            if (isMe) {
                return FocusTimerManager.focusRecords.value.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, dateStr) }
            } else {
                val peerRecords = FocusTimerManager.loadPeerFocusRecords(context, username)
                return peerRecords.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, dateStr) }
            }
        }
    }

    fun formatFocusedSecondsForFilter(seconds: Int, filter: String): String {
        if (filter == "Today") {
            return formatLiveSeconds(seconds)
        } else {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
    }

    val currentMeUsername = viewModel.currentUsername.collectAsState().value ?: "me_user"
    val myName = viewModel.currentUserRemote.collectAsState().value?.nickname
        ?: viewModel.currentUserRemote.collectAsState().value?.name
        ?: "Bharathikrishna M"
    val myEmoji = viewModel.currentUserRemote.collectAsState().value?.emoji ?: "👨‍💻"

    val participantInfos = remember(allUsers, selectedFilter, targetDates, currentMeUsername, myName, myEmoji, showElapsedTimeDialog) {
        val keys = mutableSetOf<String>()
        keys.add(currentMeUsername)
        allUsers.forEach { (username, user) ->
            if (username != "admin" && 
                username != currentMeUsername &&
                user.status != "logged_out" &&
                user.status != "uninstalled" &&
                user.isGoogleUser == true
            ) {
                keys.add(username)
            }
        }

        keys.map { username ->
            val isMe = username == currentMeUsername
            val peerRemote = allUsers[username]

            var totalFilterSeconds = 0
            val listDates = targetDates
            listDates.forEach { dateStr ->
                totalFilterSeconds += getSecondsForDate(
                    username = username,
                    dateStr = dateStr,
                    isMe = isMe,
                    peerRemote = peerRemote,
                    currentUnixTime = System.currentTimeMillis() / 1000
                )
            }

            val displayName = if (isMe) {
                myName
            } else if (peerRemote != null) {
                peerRemote.nickname ?: peerRemote.name ?: username
            } else {
                when (username) {
                    "madhavan" -> "Madhavan Sethuraman"
                    "shalini" -> "Shalini Krishnan"
                    "subash" -> "Subash E"
                    else -> username
                }
            }

            val emoji = if (isMe) {
                myEmoji
            } else if (peerRemote != null) {
                peerRemote.emoji ?: "🎯"
            } else {
                when (username) {
                    "madhavan" -> "👨‍💻"
                    "shalini" -> "👩‍💻"
                    "subash" -> "👨‍💼"
                    else -> "🎯"
                }
            }

            val peerLastUpdated = peerRemote?.lastUpdatedTimestamp ?: 0L
            val peerLastUpdatedDateStr = if (peerLastUpdated > 0) {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(peerLastUpdated))
            } else {
                ""
            }
            val isPeerStale = !isMe && peerLastUpdatedDateStr.isNotEmpty() && peerLastUpdatedDateStr != todayStr

            val isFocusing = if (isPeerStale) false else (peerRemote?.focusStatus == "focusing" || peerRemote?.isFocusing == true)

            val focusStatus = if (isPeerStale) "idle" else (peerRemote?.focusStatus ?: (if (peerRemote?.isFocusing == true) "focusing" else "idle"))

            val currentTask = if (isMe) {
                FocusTimerManager.attachedTask.value?.title
            } else {
                if (isPeerStale) null else peerRemote?.currentTaskTitle
            }

            val currentTag = if (isMe) {
                FocusTimerManager.attachedTag.value.takeIf { it.isNotEmpty() }
            } else {
                if (isPeerStale) null else peerRemote?.currentTag
            }

            PeerFocusInfo(
                username = username,
                displayName = displayName,
                emoji = emoji,
                isFocusing = isFocusing,
                liveFocusedSeconds = totalFilterSeconds,
                currentTask = currentTask,
                currentTag = currentTag,
                isMe = isMe,
                focusStatus = focusStatus
            )
        }.sortedByDescending { it.liveFocusedSeconds }
    }

    var selectedFriendForHistory by remember { mutableStateOf<PeerFocusInfo?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f).fillMaxHeight(0.95f)
                .padding(16.dp)
                .testTag("friends_focus_details_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                if (selectedFriendForHistory == null) {
                    // Title and Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Friends Focus Details",
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Friends Focus Details",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close details",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Selection Dropdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Period Range:",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { filterExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedFilter,
                                    color = WaterBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select range",
                                    tint = WaterBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF141414))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = if (selectedFilter == option) WaterBlue else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = if (selectedFilter == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            selectedFilter = option
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (participantInfos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No other users registered",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(participantInfos) { index, peer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (peer.isFocusing) WaterBlue.copy(alpha = 0.08f)
                                            else Color.White.copy(alpha = 0.03f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (peer.isFocusing) WaterBlue.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank Number Badge
                                    Text(
                                        text = "#${index + 1}",
                                        color = when (index) {
                                            0 -> GoldRank
                                            1 -> SilverRank
                                            2 -> BronzeRank
                                            else -> Color.Gray
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )

                                    // Clickable Participant Row
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedFriendForHistory = peer },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Emoji / Photo
                                        UserAvatar(
                                            emojiOrBase64 = peer.emoji,
                                            size = 36.dp,
                                            fontSize = 18.sp
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Name and task details
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (peer.isMe) "${peer.displayName} (You)" else peer.displayName,
                                                    color = if (peer.isMe) WaterBlue else Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (!peer.isMe) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "@${peer.username}",
                                                        color = Color.Gray,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                }
                                                if (peer.focusStatus == "focusing") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF2E7D32))
                                                    )
                                                }
                                            }

                                            val subtitleText = when (peer.focusStatus) {
                                                "focusing" -> peer.currentTask?.let { "Focusing on: $it" } ?: "Focusing"
                                                "paused" -> peer.currentTask?.let { "Paused: $it" } ?: "Paused"
                                                "break" -> "On a Break"
                                                else -> "Idle"
                                            }

                                            val subtitleColor = when (peer.focusStatus) {
                                                "focusing" -> WaterBlue.copy(alpha = 0.8f)
                                                "paused" -> Color(0xFFFFA726).copy(alpha = 0.8f)
                                                "break" -> Color(0xFF66BB6A).copy(alpha = 0.8f)
                                                else -> Color.Gray
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = subtitleText,
                                                    color = subtitleColor,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.weight(1f, fill = false),
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                if (!peer.currentTag.isNullOrBlank()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(WaterBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .border(0.5.dp, WaterBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = peer.currentTag,
                                                            color = WaterBlue,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Focus Time Summed Over Range
                                    val focusTimeColor = when (peer.focusStatus) {
                                        "focusing" -> WaterBlue
                                        "paused" -> Color(0xFFFFA726)
                                        "break" -> Color(0xFF66BB6A)
                                        else -> Color.LightGray
                                    }
                                    LiveDurationText(
                                        viewModel = viewModel,
                                        baseSeconds = peer.liveFocusedSeconds,
                                        isFocusing = peer.isFocusing,
                                        isMe = peer.isMe,
                                        peerRemote = allUsers[peer.username],
                                        filter = selectedFilter
                                    )

                                    if (!peer.isMe && !peer.isFocusing) {
                                        Spacer(modifier = Modifier.width(8.dp))

                                        val remainingCooldown = viewModel.getBellCooldownRemaining(peer.username)
                                        val isOnCooldown = remainingCooldown > 0

                                        IconButton(
                                            onClick = {
                                                viewModel.ringFriendBell(
                                                    targetUsername = peer.username,
                                                    onSuccess = {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "Rang focus bell for ${peer.displayName}! 🔔",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    onError = { error ->
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            error,
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            },
                                            modifier = Modifier.size(24.dp),
                                            enabled = !isOnCooldown
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Remind friend to focus",
                                                tint = if (isOnCooldown) Color.Gray.copy(alpha = 0.5f) else WaterBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    FriendHistoryDetailsContent(
                        viewModel = viewModel,
                        peer = selectedFriendForHistory!!,
                        allUsers = allUsers,
                        selectedFilter = selectedFilter,
                        targetDates = targetDates,
                        todayStr = todayStr,
                        onBack = { selectedFriendForHistory = null },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun LiveDurationText(
    viewModel: AppViewModel,
    baseSeconds: Int,
    isFocusing: Boolean,
    isMe: Boolean,
    peerRemote: com.example.api.UserRemote?,
    filter: String
) {
    val systemTodayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }

    var liveSeconds by remember(baseSeconds, isFocusing, isMe, peerRemote) {
        val initialSecs = if (isFocusing && !isMe && peerRemote != null) {
            val currentUnixTime = System.currentTimeMillis() / 1000
            val completedTodaySecs = peerRemote.todaysFocusRecords?.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) } ?: 0
            
            if (peerRemote.lastResumeTimeMs != null) {
                val currentChunkMs = (currentUnixTime * 1000) - peerRemote.lastResumeTimeMs!!
                val totalMs = (peerRemote.accumulatedTimeMs ?: 0L) + maxOf(0L, currentChunkMs)
                completedTodaySecs + (totalMs / 1000).toInt()
            } else {
                completedTodaySecs + ((peerRemote.accumulatedTimeMs ?: 0L) / 1000).toInt()
            }
        } else {
            baseSeconds
        }
        mutableStateOf(initialSecs)
    }

    LaunchedEffect(isFocusing, isMe, peerRemote) {
        if (isFocusing) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                val currentUnixTime = System.currentTimeMillis() / 1000
                if (isMe) {
                    val isLocalFocusing = (FocusTimerManager.isTimerRunning.value || FocusTimerManager.isStopwatchActive.value) && FocusTimerManager.isFocusPhase.value && FocusTimerManager.pendingFocusReview.value == null
                    val completedTodaySecs = FocusTimerManager.focusRecords.value.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) }
                    val pendingSecs = FocusTimerManager.pendingFocusReview.value?.let { FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) } ?: 0
                    val activeSessionOverlap = if (isLocalFocusing) {
                        val startMs = viewModel.sessionStartTimestamp.value
                        if (startMs != null) {
                            FocusTimerManager.getActiveSessionOverlapSeconds(startMs, systemTodayStr)
                        } else {
                            val currentChunkMs = FocusTimerManager.getCurrentChunkMs()
                            val totalMs = FocusTimerManager.accumulatedSessionTimeMs.value + currentChunkMs
                            (totalMs / 1000).toInt()
                        }
                    } else 0
                    liveSeconds = completedTodaySecs + pendingSecs + activeSessionOverlap
                } else if (peerRemote != null) {
                    val completedTodaySecs = peerRemote.todaysFocusRecords?.sumOf { FocusTimerManager.getOverlapSecondsForDate(it, systemTodayStr) } ?: 0
                    if (peerRemote.lastResumeTimeMs != null) {
                        val currentChunkMs = (currentUnixTime * 1000) - peerRemote.lastResumeTimeMs!!
                        val totalMs = (peerRemote.accumulatedTimeMs ?: 0L) + maxOf(0L, currentChunkMs)
                        liveSeconds = completedTodaySecs + (totalMs / 1000).toInt()
                    } else {
                        liveSeconds = completedTodaySecs + ((peerRemote.accumulatedTimeMs ?: 0L) / 1000).toInt()
                    }
                }
            }
        }
    }

    Text(
        text = if (filter == "Today") formatLiveSeconds(liveSeconds) else {
            val hours = liveSeconds / 3600
            val minutes = (liveSeconds % 3600) / 60
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        },
        color = if (isFocusing) Color(0xFF38BDF8) else Color.LightGray,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
}

fun formatLiveSeconds(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s)
    }
}

fun formatRecordDuration(durationSeconds: Int, durationMinutes: Int): String {
    val secs = if (durationSeconds > 0) durationSeconds else durationMinutes * 60
    val h = secs / 3600
    val m = (secs % 3600) / 60
    val s = secs % 60
    return if (h > 0) {
        String.format(java.util.Locale.getDefault(), "%dh %dm %ds", h, m, s)
    } else if (m > 0) {
        String.format(java.util.Locale.getDefault(), "%dm %ds", m, s)
    } else {
        String.format(java.util.Locale.getDefault(), "%ds", s)
    }
}
