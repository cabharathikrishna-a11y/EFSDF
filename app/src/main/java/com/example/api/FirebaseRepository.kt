package com.example.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FirebaseRepository {
    private val _usersState = MutableStateFlow<Map<String, UserRemote>>(emptyMap())
    val usersState: StateFlow<Map<String, UserRemote>> = _usersState.asStateFlow()

    private val lock = Any()

    fun updateUsers(newUsers: Map<String, UserRemote>) {
        synchronized(lock) {
            // Merge the incoming users into the existing state map to prevent overwriting
            // of local user's data during real-time friend updates.
            val currentMap = _usersState.value.toMutableMap()
            newUsers.forEach { (username, user) ->
                currentMap[username] = user.copy(
                    todaysFocusRecords = user.todaysFocusRecords?.toList() ?: emptyList()
                )
            }
            _usersState.value = currentMap
        }
    }

    fun getUsers(): Map<String, UserRemote> {
        synchronized(lock) {
            return _usersState.value
        }
    }
}
