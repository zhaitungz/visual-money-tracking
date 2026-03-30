package com.example.visualmoneytracker.data.remote.cloud

import android.content.Context
import android.content.SharedPreferences
import com.example.visualmoneytracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxSyncManager @Inject constructor(
    private val context: Context
) {
    private val client = OkHttpClient()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("box_prefs", Context.MODE_PRIVATE)

    val clientId: String get() = BuildConfig.BOX_CLIENT_ID
    val clientSecret: String get() = BuildConfig.BOX_CLIENT_SECRET
    val redirectUri: String get() = BuildConfig.BOX_REDIRECT_URI

    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(v) = prefs.edit().putString("access_token", v).apply()

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(v) = prefs.edit().putString("refresh_token", v).apply()

    val isAuthenticated: Boolean get() = accessToken != null

    /** Exchange auth code for tokens */
    suspend fun exchangeCode(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = "grant_type=authorization_code&code=$code" +
                "&client_id=$clientId&client_secret=$clientSecret&redirect_uri=$redirectUri"
            val req = Request.Builder()
                .url("https://api.box.com/oauth2/token")
                .post(body.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            accessToken = json.getString("access_token")
            refreshToken = json.getString("refresh_token")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Refresh access token */
    suspend fun refreshAccessToken(): Result<Unit> = withContext(Dispatchers.IO) {
        val rt = refreshToken ?: return@withContext Result.failure(Exception("No refresh token"))
        try {
            val body = "grant_type=refresh_token&refresh_token=$rt" +
                "&client_id=$clientId&client_secret=$clientSecret"
            val req = Request.Builder()
                .url("https://api.box.com/oauth2/token")
                .post(body.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            accessToken = json.getString("access_token")
            refreshToken = json.optString("refresh_token", rt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Upload a file to Box root folder, returns file ID */
    suspend fun uploadFile(file: File, remoteFileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            val token = accessToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            try {
                val attributes = JSONObject().apply {
                    put("name", remoteFileName)
                    put("parent", JSONObject().put("id", "0"))
                }.toString()

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("attributes", attributes)
                    .addFormDataPart(
                        "file", file.name,
                        file.asRequestBody("application/octet-stream".toMediaType())
                    )
                    .build()

                val req = Request.Builder()
                    .url("https://upload.box.com/api/2.0/files/content")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val resp = client.newCall(req).execute()
                if (!resp.isSuccessful) {
                    // Try refresh once
                    refreshAccessToken()
                    return@withContext uploadFile(file, remoteFileName)
                }
                val json = JSONObject(resp.body!!.string())
                val fileId = json.getJSONArray("entries").getJSONObject(0).getString("id")
                Result.success(fileId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** List files in Box root folder */
    suspend fun listFiles(): Result<List<BoxFile>> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            val req = Request.Builder()
                .url("https://api.box.com/2.0/folders/0/items?fields=id,name,created_at&limit=100")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            val entries = json.getJSONArray("entries")
            val files = (0 until entries.length()).map { i ->
                val obj = entries.getJSONObject(i)
                BoxFile(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    createdAt = obj.optString("created_at", "")
                )
            }.filter { it.name.startsWith("vmt_backup_") }
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Download file content by file ID */
    suspend fun downloadFile(fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            val req = Request.Builder()
                .url("https://api.box.com/2.0/files/$fileId/content")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            Result.success(resp.body!!.bytes())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete a file by ID */
    suspend fun deleteFile(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            val req = Request.Builder()
                .url("https://api.box.com/2.0/files/$fileId")
                .header("Authorization", "Bearer $token")
                .delete()
                .build()
            client.newCall(req).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        prefs.edit().remove("access_token").remove("refresh_token").apply()
    }
}

data class BoxFile(val id: String, val name: String, val createdAt: String)
