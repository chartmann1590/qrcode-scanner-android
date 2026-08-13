package com.charles.qrcode.data.feedback

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Talks to the cloudflare-worker/ feedback relay, not api.github.com directly — the
 * Worker holds the GitHub token as a server-side secret and hardcodes this app's own
 * repo, so no owner/repo/credential ever needs to travel through this app. Previously
 * embedded BuildConfig.GITHUB_API_TOKEN client-side as a Bearer header, which shipped a
 * real repo-write PAT in every release build (extractable from the APK). See
 * cloudflare-worker/src/index.ts.
 */
class GithubApi private constructor(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl = "https://qrcode-scanner-github-feedback.charles-h-hartmann1.workers.dev"

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .addHeader("User-Agent", "QRCode-Android/1.0")
                chain.proceed(builder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // Always true now — the relay is a fixed public Worker URL, not per-install config.
    val isConfigured: Boolean = true
    val configError: String = ""

    suspend fun createIssue(title: String, body: String): Result<GithubIssue> =
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = org.json.JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("labels", org.json.JSONArray().put("bug"))
                }
                val bodyJson = jsonBody.toString()
                val httpRequest = Request.Builder()
                    .url("$baseUrl/issue")
                    .post(bodyJson.toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(json.decodeFromString(GithubIssue.serializer(), responseBody))
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Result.failure(Exception("GitHub API error ${response.code}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getIssue(issueNumber: Int): Result<GithubIssue> =
        withContext(Dispatchers.IO) {
            try {
                val httpRequest = Request.Builder()
                    .url("$baseUrl/issue/$issueNumber")
                    .get()
                    .build()
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(json.decodeFromString(GithubIssue.serializer(), responseBody))
                } else {
                    Result.failure(Exception("GitHub API error: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getComments(issueNumber: Int): Result<List<GithubComment>> =
        withContext(Dispatchers.IO) {
            try {
                val httpRequest = Request.Builder()
                    .url("$baseUrl/issue/$issueNumber/comments")
                    .get()
                    .build()
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(
                        json.decodeFromString(
                            ListSerializer(GithubComment.serializer()),
                            responseBody
                        )
                    )
                } else {
                    Result.failure(Exception("GitHub API error: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun postComment(issueNumber: Int, body: String): Result<GithubComment> =
        withContext(Dispatchers.IO) {
            try {
                val request = PostCommentRequest(body)
                val bodyJson = json.encodeToString(request)
                val httpRequest = Request.Builder()
                    .url("$baseUrl/issue/$issueNumber/comments")
                    .post(bodyJson.toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(json.decodeFromString(GithubComment.serializer(), responseBody))
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Result.failure(Exception("GitHub API error ${response.code}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadAsset(fileName: String, base64Content: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val requestJson = """{"filename":"$fileName","contentBase64":"$base64Content"}"""
                val httpRequest = Request.Builder()
                    .url("$baseUrl/upload-image")
                    .post(requestJson.toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))
                    val uploadResponse = json.decodeFromString(
                        UploadAssetResponse.serializer(),
                        responseBody
                    )
                    val downloadUrl = uploadResponse.content?.downloadUrl
                    if (downloadUrl != null) {
                        Result.success(downloadUrl)
                    } else {
                        Result.failure(Exception("No download URL in upload response"))
                    }
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Result.failure(Exception("Upload failed ${response.code}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    companion object {
        @Volatile
        private var instance: GithubApi? = null

        fun getInstance(context: Context): GithubApi {
            return instance ?: synchronized(this) {
                instance ?: GithubApi(context.applicationContext).also { instance = it }
            }
        }
    }
}
