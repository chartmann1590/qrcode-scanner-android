package com.charles.qrcode.data.feedback

import android.content.Context
import com.charles.qrcode.BuildConfig
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

class GithubApi private constructor(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        loggingInterceptor.redactHeader("Authorization")

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .addHeader("User-Agent", "QRCode-Android/1.0")
                val token = BuildConfig.GITHUB_API_TOKEN
                if (token.isNotEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val owner: String get() = BuildConfig.GITHUB_REPO_OWNER
    private val repo: String get() = BuildConfig.GITHUB_REPO_NAME

    val isConfigured: Boolean
        get() = BuildConfig.GITHUB_API_TOKEN.isNotEmpty() &&
                owner.isNotEmpty() && repo.isNotEmpty()

    val configError: String
        get() = when {
            BuildConfig.GITHUB_API_TOKEN.isEmpty() -> "GitHub API token not configured. Add github.api.token to local.properties."
            owner.isEmpty() -> "GitHub repo owner not configured. Add github.repo.owner to local.properties."
            repo.isEmpty() -> "GitHub repo name not configured. Add github.repo.name to local.properties."
            else -> ""
        }

    suspend fun createIssue(title: String, body: String): Result<GithubIssue> =
        withContext(Dispatchers.IO) {
            try {
                val request = CreateIssueRequest(title, body)
                val bodyJson = json.encodeToString(request)
                val httpRequest = Request.Builder()
                    .url("https://api.github.com/repos/$owner/$repo/issues")
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
                    .url("https://api.github.com/repos/$owner/$repo/issues/$issueNumber")
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
                    .url("https://api.github.com/repos/$owner/$repo/issues/$issueNumber/comments")
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
                    .url("https://api.github.com/repos/$owner/$repo/issues/$issueNumber/comments")
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
                val assetDir = BuildConfig.FEEDBACK_ASSETS_DIR
                val path = "$assetDir/$fileName"
                val requestJson = """{"message":"Upload feedback screenshot","content":"$base64Content"}"""
                val httpRequest = Request.Builder()
                    .url("https://api.github.com/repos/$owner/$repo/contents/$path")
                    .put(requestJson.toRequestBody("application/json".toMediaType()))
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
