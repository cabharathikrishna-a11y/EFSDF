package com.example.api

import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass

import com.example.ui.FocusRecord

@JsonClass(generateAdapter = true)
data class UserRemote(
    val password: String = "",
    val name: String? = null,
    val nickname: String? = null,
    val emoji: String? = null,
    val isFocusing: Boolean? = null,
    val accumulatedTimeMs: Long = 0L,
    val lastResumeTimeMs: Long? = null,
    val currentTaskTitle: String? = null,
    val todaysFocusRecords: List<FocusRecord>? = null,
    val focusRecords: List<FocusRecord>? = null,
    val isStopwatchMode: Boolean? = null,
    val lastUpdatedTimestamp: Long? = null,
    val lastButtonClicked: String? = null,
    val lastButtonClickedTimestamp: Long? = null,
    val focusStatus: String? = null,
    val currentTag: String? = null,
    val isGoogleUser: Boolean? = null,
    val email: String? = null,
    val status: String? = null,
    val appVersion: String? = null,
    val forceApkUrl: String? = null,
    val lastUpdatedDeviceId: String? = null,
    val deviceLogs: String? = null
)

@JsonClass(generateAdapter = true)
data class BellSignal(
    val senderUsername: String = "",
    val senderDisplayName: String = "",
    val timestamp: Long = 0L,
    val isProcessed: Boolean = false
)

interface FirebaseApi {
    @GET("users.json")
    suspend fun getUsers(): retrofit2.Response<Map<String, UserRemote>?>

    @GET("users/{username}.json")
    suspend fun getUser(
        @Path("username") username: String
    ): retrofit2.Response<UserRemote?>

    @PUT("users/{username}.json")
    suspend fun putUser(
        @Path("username") username: String,
        @Body user: UserRemote
    ): UserRemote

    @DELETE("users/{username}.json")
    suspend fun deleteUser(
        @Path("username") username: String
    ): retrofit2.Response<Unit>

    @GET("bells/{username}.json")
    suspend fun getBellSignal(
        @Path("username") username: String
    ): retrofit2.Response<BellSignal?>

    @PUT("bells/{username}.json")
    suspend fun putBellSignal(
        @Path("username") username: String,
        @Body signal: BellSignal?
    ): BellSignal?

    @GET("requests/{username}.json")
    suspend fun getPeerRequests(
        @Path("username") username: String
    ): retrofit2.Response<Map<String, Boolean>?>

    @PUT("requests/{username}/{requester}.json")
    suspend fun putPeerRequest(
        @Path("username") username: String,
        @Path("requester") requester: String,
        @Body request: Boolean
    ): Boolean

    @DELETE("requests/{username}/{requester}.json")
    suspend fun deletePeerRequest(
        @Path("username") username: String,
        @Path("requester") requester: String
    ): retrofit2.Response<Unit>

    @GET("transfer/{requester}/{provider}.json")
    suspend fun getTransferredData(
        @Path("requester") requester: String,
        @Path("provider") provider: String
    ): retrofit2.Response<List<FocusRecord>?>

    @PUT("transfer/{requester}/{provider}.json")
    suspend fun putTransferredData(
        @Path("requester") requester: String,
        @Path("provider") provider: String,
        @Body records: List<FocusRecord>?
    ): List<FocusRecord>?

    @DELETE("transfer/{requester}/{provider}.json")
    suspend fun deleteTransferredData(
        @Path("requester") requester: String,
        @Path("provider") provider: String
    ): retrofit2.Response<Unit>
}

object FirebaseClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var cachedApi: FirebaseApi? = null
    private var cachedUrl: String? = null

    @Volatile
    private var appContextRef: java.lang.ref.WeakReference<android.content.Context>? = null

    var appContext: android.content.Context?
        get() = appContextRef?.get()
        set(value) {
            appContextRef = value?.let { java.lang.ref.WeakReference(it.applicationContext) }
        }

    val api: FirebaseApi
        get() {
            val url = activeUrl
            synchronized(this) {
                if (cachedApi == null || cachedUrl != url) {
                    cachedUrl = url
                    val retrofitApi = Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                        .create(FirebaseApi::class.java)
                    cachedApi = InterceptingFirebaseApi(retrofitApi) { appContext }
                }
                return cachedApi!!
            }
        }

    @Volatile
    var activeUrl: String = if (FirebaseConfig.DATABASE_URL.endsWith("/")) FirebaseConfig.DATABASE_URL else "${FirebaseConfig.DATABASE_URL}/"
        set(value) {
            val sanitized = if (value.endsWith("/")) value else "$value/"
            field = sanitized
        }
}

class InterceptingFirebaseApi(
    private val delegate: FirebaseApi,
    private val contextProvider: () -> android.content.Context?
) : FirebaseApi {

    private fun isTester(): Boolean {
        val ctx = contextProvider() ?: return false
        val prefs = ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("is_tester_mode", false)
    }

    override suspend fun getUsers(): retrofit2.Response<Map<String, UserRemote>?> {
        return delegate.getUsers()
    }

    override suspend fun getUser(username: String): retrofit2.Response<UserRemote?> {
        if (isTester() || username == "tester_mode_user") {
            return retrofit2.Response.success(UserRemote(password = "tester"))
        }
        return delegate.getUser(username)
    }

    private fun getAppVersionString(): String {
        val ctx = contextProvider() ?: return "Unknown"
        return try {
            val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    override suspend fun putUser(username: String, user: UserRemote): UserRemote {
        if (isTester() || username == "tester_mode_user") {
            // Intercept and bypass
            android.util.Log.d("InterceptingFirebase", "Bypassing putUser for username: $username in Tester Mode")
            return user
        }
        val userWithVersion = user.copy(appVersion = getAppVersionString())
        return delegate.putUser(username, userWithVersion)
    }

    override suspend fun deleteUser(username: String): retrofit2.Response<Unit> {
        if (isTester() || username == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing deleteUser for username: $username in Tester Mode")
            return retrofit2.Response.success(Unit)
        }
        return delegate.deleteUser(username)
    }

    override suspend fun getBellSignal(username: String): retrofit2.Response<BellSignal?> {
        if (isTester() || username == "tester_mode_user") {
            return retrofit2.Response.success(null)
        }
        return delegate.getBellSignal(username)
    }

    override suspend fun putBellSignal(username: String, signal: BellSignal?): BellSignal? {
        if (isTester() || username == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing putBellSignal for username: $username in Tester Mode")
            return signal
        }
        return delegate.putBellSignal(username, signal)
    }

    override suspend fun getPeerRequests(username: String): retrofit2.Response<Map<String, Boolean>?> {
        if (isTester() || username == "tester_mode_user") {
            return retrofit2.Response.success(null)
        }
        return delegate.getPeerRequests(username)
    }

    override suspend fun putPeerRequest(username: String, requester: String, request: Boolean): Boolean {
        if (isTester() || username == "tester_mode_user" || requester == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing putPeerRequest in Tester Mode")
            return true
        }
        return delegate.putPeerRequest(username, requester, request)
    }

    override suspend fun deletePeerRequest(username: String, requester: String): retrofit2.Response<Unit> {
        if (isTester() || username == "tester_mode_user" || requester == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing deletePeerRequest in Tester Mode")
            return retrofit2.Response.success(Unit)
        }
        return delegate.deletePeerRequest(username, requester)
    }

    override suspend fun getTransferredData(requester: String, provider: String): retrofit2.Response<List<FocusRecord>?> {
        return delegate.getTransferredData(requester, provider)
    }

    override suspend fun putTransferredData(requester: String, provider: String, records: List<FocusRecord>?): List<FocusRecord>? {
        if (isTester() || requester == "tester_mode_user" || provider == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing putTransferredData in Tester Mode")
            return records
        }
        return delegate.putTransferredData(requester, provider, records)
    }

    override suspend fun deleteTransferredData(requester: String, provider: String): retrofit2.Response<Unit> {
        if (isTester() || requester == "tester_mode_user" || provider == "tester_mode_user") {
            android.util.Log.d("InterceptingFirebase", "Bypassing deleteTransferredData in Tester Mode")
            return retrofit2.Response.success(Unit)
        }
        return delegate.deleteTransferredData(requester, provider)
    }
}
