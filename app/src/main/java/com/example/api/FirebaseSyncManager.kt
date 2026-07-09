package com.example.api

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FirebaseSyncManager {
    
    // We keep a thread-safe map of our friends' live statuses
    private val _friendsLiveStatus = MutableStateFlow<Map<String, UserRemote>>(emptyMap())
    val friendsLiveStatus = _friendsLiveStatus.asStateFlow()

    private var database: FirebaseDatabase? = null
    private val activeListeners = mutableMapOf<String, ValueEventListener>()

    private fun getDatabase(context: Context): FirebaseDatabase {
        return database ?: synchronized(this) {
            val db = database ?: run {
                val url = FirebaseConfig.getDatabaseUrl(context)
                val instance = FirebaseDatabase.getInstance(url)
                try {
                    instance.setPersistenceEnabled(true)
                } catch (e: Exception) {
                    // Can throw if initialized more than once, safe to ignore
                }
                instance
            }
            database = db
            db
        }
    }

    fun parseUserSnapshot(snapshot: DataSnapshot): UserRemote? {
        try {
            val password = snapshot.child("password").getValue(String::class.java) ?: ""
            val name = snapshot.child("name").getValue(String::class.java)
            val nickname = snapshot.child("nickname").getValue(String::class.java)
            val emoji = snapshot.child("emoji").getValue(String::class.java)
            val isFocusing = snapshot.child("isFocusing").getValue(Boolean::class.java)
            val accumulatedTimeMs = snapshot.child("accumulatedTimeMs").getValue(Long::class.java) ?: 0L
            val lastResumeTimeMs = snapshot.child("lastResumeTimeMs").getValue(Long::class.java)
            val currentTaskTitle = snapshot.child("currentTaskTitle").getValue(String::class.java)
            val isStopwatchMode = snapshot.child("isStopwatchMode").getValue(Boolean::class.java)
            val lastUpdatedTimestamp = snapshot.child("lastUpdatedTimestamp").getValue(Long::class.java)
            val lastButtonClicked = snapshot.child("lastButtonClicked").getValue(String::class.java)
            val lastButtonClickedTimestamp = snapshot.child("lastButtonClickedTimestamp").getValue(Long::class.java)
            val focusStatus = snapshot.child("focusStatus").getValue(String::class.java)
            val currentTag = snapshot.child("currentTag").getValue(String::class.java)
            val isGoogleUser = snapshot.child("isGoogleUser").getValue(Boolean::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            val status = snapshot.child("status").getValue(String::class.java)
            val lastUpdatedDeviceId = snapshot.child("lastUpdatedDeviceId").getValue(String::class.java)

            val todaysFocusRecords = mutableListOf<com.example.ui.FocusRecord>()
            snapshot.child("todaysFocusRecords").children.forEach { recordSnapshot ->
                val recordId = recordSnapshot.child("id").getValue(String::class.java) ?: java.util.UUID.randomUUID().toString()
                val recTaskTitle = recordSnapshot.child("taskTitle").getValue(String::class.java) ?: ""
                val recDuration = recordSnapshot.child("durationSeconds").getValue(Int::class.java) ?: 0
                val recDurationMinutes = recordSnapshot.child("durationMinutes").getValue(Int::class.java) ?: (recDuration / 60)
                val recStartTime = recordSnapshot.child("startTime").getValue(String::class.java) ?: ""
                val recEndTime = recordSnapshot.child("endTime").getValue(String::class.java) ?: ""
                val recDate = recordSnapshot.child("dateString").getValue(String::class.java) ?: ""
                val recTag = recordSnapshot.child("tag").getValue(String::class.java) ?: ""
                val recNotes = recordSnapshot.child("notes").getValue(String::class.java) ?: ""

                todaysFocusRecords.add(
                    com.example.ui.FocusRecord(
                        id = recordId,
                        taskTitle = recTaskTitle,
                        durationMinutes = recDurationMinutes,
                        durationSeconds = recDuration,
                        startTime = recStartTime,
                        endTime = recEndTime,
                        dateString = recDate,
                        tag = recTag,
                        notes = recNotes
                    )
                )
            }

            return UserRemote(
                password = password,
                name = name,
                nickname = nickname,
                emoji = emoji,
                isFocusing = isFocusing,
                accumulatedTimeMs = accumulatedTimeMs,
                lastResumeTimeMs = lastResumeTimeMs,
                currentTaskTitle = currentTaskTitle,
                todaysFocusRecords = todaysFocusRecords,
                isStopwatchMode = isStopwatchMode,
                lastUpdatedTimestamp = lastUpdatedTimestamp,
                lastButtonClicked = lastButtonClicked,
                lastButtonClickedTimestamp = lastButtonClickedTimestamp,
                focusStatus = focusStatus,
                currentTag = currentTag,
                isGoogleUser = isGoogleUser,
                email = email,
                status = status,
                lastUpdatedDeviceId = lastUpdatedDeviceId
            )
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Failed to parse UserRemote from snapshot", e)
            return null
        }
    }

    /**
     * Instantly listen to specific friends and stream their updates.
     * Modified to listen to the entire parent 'users' node to automatically and instantly track all logged-in Google friends in real-time.
     */
    fun listenToFriends(context: Context, friendUsernames: List<String>) {
        val usersRef = getDatabase(context).getReference("users")

        if (activeListeners.containsKey("all_users_parent")) return

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    _friendsLiveStatus.value = emptyMap()
                    return
                }
                val currentMap = _friendsLiveStatus.value.toMutableMap()
                snapshot.children.forEach { userSnapshot ->
                    val username = userSnapshot.key ?: return@forEach
                    val userRemote = parseUserSnapshot(userSnapshot)
                    if (userRemote != null) {
                        currentMap[username] = userRemote
                    } else {
                        currentMap.remove(username)
                    }
                }
                _friendsLiveStatus.value = currentMap
                Log.d("FirebaseSyncManager", "Real-time update received for all users. Total users: ${currentMap.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSyncManager", "Failed to listen to all users: ${error.message}")
            }
        }

        usersRef.addValueEventListener(listener)
        activeListeners["all_users_parent"] = listener
    }

    /**
     * Call this when your own focus state changes to push it to the cloud instantly.
     */
    fun pushMyStatus(context: Context, myUsername: String, isFocusing: Boolean, currentTask: String) {
        val myRef = getDatabase(context).getReference("users").child(myUsername)
        
        val updates = mapOf<String, Any>(
            "isFocusing" to isFocusing,
            "currentTaskTitle" to currentTask,
            "lastUpdatedTimestamp" to System.currentTimeMillis()
        )
        
        myRef.updateChildren(updates)
    }

    fun stopListening(context: Context) {
        val usersRef = getDatabase(context).getReference("users")
        activeListeners.forEach { (username, listener) ->
            if (username == "all_users_parent") {
                usersRef.removeEventListener(listener)
            } else {
                usersRef.child(username).removeEventListener(listener)
            }
        }
        activeListeners.clear()
    }
}
