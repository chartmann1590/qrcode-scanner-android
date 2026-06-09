package com.charles.qrcode.data.feedback

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DiagnosticsHelper {

    fun collect(context: Context): String {
        val sb = StringBuilder()
        sb.appendLine("## Diagnostics")
        sb.appendLine()

        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appName = try {
                context.getString(context.applicationInfo.labelRes)
            } catch (_: Exception) {
                "QRCode"
            }
            sb.appendLine("- App: $appName")
            sb.appendLine("- Package: ${context.packageName}")
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            sb.appendLine("- Version: ${packageInfo.versionName} ($versionCode)")
        } catch (_: Exception) {
            sb.appendLine("- App: QRCode")
            sb.appendLine("- Package: ${context.packageName}")
        }

        sb.appendLine("- Device: ${Build.PRODUCT}")
        sb.appendLine("- Manufacturer: ${Build.MANUFACTURER}")
        sb.appendLine("- Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        sb.appendLine("- Locale: ${Locale.getDefault()}")
        sb.appendLine("- Time Zone: ${TimeZone.getDefault().id}")

        try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalStorage = statFs.totalBytes
            val freeStorage = statFs.availableBytes
            sb.appendLine("- Storage Free/Total: ${formatBytes(freeStorage)} / ${formatBytes(totalStorage)}")
        } catch (_: Exception) {
            sb.appendLine("- Storage: Unable to determine")
        }

        try {
            val runtime = Runtime.getRuntime()
            val freeMemory = runtime.freeMemory()
            val totalMemory = runtime.totalMemory()
            sb.appendLine("- Memory Free/Total: ${formatBytes(freeMemory)} / ${formatBytes(totalMemory)}")
        } catch (_: Exception) {
            sb.appendLine("- Memory: Unable to determine")
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
        sb.appendLine("- Timestamp: ${dateFormat.format(Date())}")

        return sb.toString()
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.1f GB", gb)
    }
}
