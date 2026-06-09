package com.charles.qrcode.data.feedback

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "feedback_bug_reports"
)

class BugReportRepo(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val key = stringPreferencesKey("bug_reports_list")

    val bugReports: Flow<List<BugReport>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[key] ?: "[]"
        try {
            json.decodeFromString(ListSerializer(BugReport.serializer()), jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveBugReport(report: BugReport) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                json.decodeFromString(
                    ListSerializer(BugReport.serializer()),
                    preferences[key] ?: "[]"
                )
            } catch (_: Exception) {
                emptyList()
            }
            val updatedList = currentList
                .filter { it.number != report.number }
                .plus(report)
                .sortedByDescending { it.number }
            preferences[key] = json.encodeToString(updatedList)
        }
    }

    suspend fun updateBugReports(reports: List<BugReport>) {
        context.dataStore.edit { preferences ->
            preferences[key] = json.encodeToString(
                reports.sortedByDescending { it.number }
            )
        }
    }

    suspend fun getBugReportsList(): List<BugReport> {
        return try {
            val preferences = context.dataStore.data.first()
            val jsonString = preferences[key] ?: "[]"
            json.decodeFromString(ListSerializer(BugReport.serializer()), jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
