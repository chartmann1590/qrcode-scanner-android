package com.charles.qrcode.data.feedback

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageHelper {

    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

        inputStream.use { stream ->
            val buffer = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            var bytesRead: Int
            while (stream.read(buf).also { bytesRead = it } != -1) {
                buffer.write(buf, 0, bytesRead)
            }
            return Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP)
        }
    }

    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val timestamp = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "issue-$timestamp-$random.png"
    }
}
