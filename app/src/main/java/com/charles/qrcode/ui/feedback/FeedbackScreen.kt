package com.charles.qrcode.ui.feedback

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import android.os.Build
import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.charles.qrcode.data.feedback.BugReport
import com.charles.qrcode.data.feedback.BugReportRepo
import com.charles.qrcode.data.feedback.DiagnosticsHelper
import com.charles.qrcode.data.feedback.GithubApi
import com.charles.qrcode.data.feedback.GithubComment
import com.charles.qrcode.data.feedback.GithubIssue
import com.charles.qrcode.data.feedback.ImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

private val Slate900 = Color(0xFF0F172A)
private val Slate800 = Color(0xFF1E293B)
private val Slate700 = Color(0xFF334155)
private val Indigo = Color(0xFF6366F1)
private val Cyan = Color(0xFF06B6D4)
private val Emerald = Color(0xFF10B981)
private val Rose = Color(0xFFF43F5E)
private val Amber = Color(0xFFF59E0B)
private val Slate50 = Color(0xFFF8FAFC)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate500 = Color(0xFF64748B)

private val FeedbackColorScheme = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Cyan,
    onSecondary = Slate900,
    surface = Slate800,
    onSurface = Slate50,
    background = Slate900,
    onBackground = Slate50,
    error = Rose,
    onError = Color.White,
    outline = Slate700,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
)

@Composable
fun FeedbackApp(activity: ComponentActivity) {
    MaterialTheme(colorScheme = FeedbackColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedbackScreen(activity)
        }
    }
}

@Composable
private fun FeedbackScreen(activity: ComponentActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { BugReportRepo(context) }
    val githubClient = remember { GithubApi.getInstance(context) }

    var reports by remember { mutableStateOf(emptyList<BugReport>()) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<BugReport?>(null) }

    LaunchedEffect(Unit) {
        repo.bugReports.collect { reports = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Support & Feedback",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Slate50
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!githubClient.isConfigured) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Rose.copy(alpha = 0.15f))
            ) {
                Text(
                    text = githubClient.configError,
                    modifier = Modifier.padding(12.dp),
                    color = Rose,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { showReportDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = githubClient.isConfigured,
            colors = ButtonDefaults.buttonColors(containerColor = Indigo)
        ) {
            Icon(Icons.Default.Report, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report a Problem")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reports.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Feedback,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Slate500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No reports submitted yet.",
                        color = Slate500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(
                text = "Submitted Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate50
            )
            Spacer(modifier = Modifier.height(8.dp))

            reports.forEach { report ->
                ReportCard(
                    report = report,
                    onClick = { selectedReport = report }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmitted = { report ->
                scope.launch { repo.saveBugReport(report) }
                showReportDialog = false
            },
            githubClient = githubClient
        )
    }

    selectedReport?.let { report ->
        IssueDetailDialog(
            report = report,
            onDismiss = { selectedReport = null },
            onStatusChanged = { updatedReport ->
                scope.launch { repo.saveBugReport(updatedReport) }
                selectedReport = updatedReport
            }
        )
    }
}

@Composable
private fun ReportCard(report: BugReport, onClick: () -> Unit) {
    val isOpen = report.status.equals("open", true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#${report.number} \u00b7 ${formatDate(report.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOpen) Emerald.copy(alpha = 0.2f) else Rose.copy(alpha = 0.2f)
            ) {
                Text(
                    text = report.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (isOpen) Emerald else Rose,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmitted: (BugReport) -> Unit,
    githubClient: GithubApi
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            color = Slate900,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Report a Problem",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate50
                    )
                    if (!isSubmitting) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Slate300
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your report will be submitted to this app\u2019s GitHub issue " +
                                "tracker. Do not include passwords, private keys, medical " +
                                "information, financial information, or anything you do not " +
                                "want visible to the repository maintainers. If this repository " +
                                "is public, your report may be publicly visible.",
                            color = Amber,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Subject *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = title.isBlank() && errorMessage != null
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    isError = description.isBlank() && errorMessage != null
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = includeDiagnostics,
                        onCheckedChange = { includeDiagnostics = it }
                    )
                    Text("Include phone/app diagnostics", color = Slate300)
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { pickMediaLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Attach Screenshot")
                    }
                    if (selectedImageUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Remove",
                                tint = Rose
                            )
                        }
                    }
                }

                selectedImageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    ImagePreview(uri = uri)
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Rose,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isSubmitting) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Title is required."
                                return@Button
                            }
                            if (description.isBlank()) {
                                errorMessage = "Description is required."
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            scope.launch {
                                try {
                                    var imageUrl: String? = null

                                    if (selectedImageUri != null) {
                                        val base64 = withContext(Dispatchers.IO) {
                                            ImageHelper.uriToBase64(
                                                context,
                                                selectedImageUri!!
                                            )
                                        }
                                        val fileName = ImageHelper.generateFileName()
                                        val uploadResult =
                                            githubClient.uploadAsset(fileName, base64)
                                        if (uploadResult.isFailure) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage =
                                                    "Image upload failed: " +
                                                        uploadResult.exceptionOrNull()?.message
                                                isSubmitting = false
                                            }
                                            return@launch
                                        }
                                        imageUrl = uploadResult.getOrNull()
                                    }

                                    val diagnostics = if (includeDiagnostics) {
                                        DiagnosticsHelper.collect(context)
                                    } else ""

                                    val body = buildString {
                                        appendLine("## Description")
                                        appendLine()
                                        appendLine(description)
                                        appendLine()
                                        appendLine("## Contact Info")
                                        appendLine()
                                        appendLine("- Name: ${name.ifBlank { "Not provided" }}")
                                        appendLine("- Email: ${email.ifBlank { "Not provided" }}")
                                        if (imageUrl != null) {
                                            appendLine()
                                            appendLine("## Attachment")
                                            appendLine()
                                            appendLine("![Screenshot]($imageUrl)")
                                        }
                                        if (diagnostics.isNotBlank()) {
                                            appendLine()
                                            append(diagnostics)
                                        }
                                    }

                                    val issueResult =
                                        githubClient.createIssue("[Feedback] $title", body)
                                    if (issueResult.isSuccess) {
                                        val issue = issueResult.getOrNull()!!
                                        val report = BugReport(
                                            number = issue.number,
                                            title = issue.title,
                                            status = issue.state,
                                            createdAt = issue.createdAt,
                                            htmlUrl = issue.htmlUrl
                                        )
                                        onSubmitted(report)
                                    } else {
                                        errorMessage =
                                            "Failed to create issue: " +
                                                issueResult.exceptionOrNull()?.message
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting && githubClient.isConfigured
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting...")
                        } else {
                            Text("Submit Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueDetailDialog(
    report: BugReport,
    onDismiss: () -> Unit,
    onStatusChanged: (BugReport) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val githubClient = remember { GithubApi.getInstance(context) }

    var issue by remember { mutableStateOf<GithubIssue?>(null) }
    var comments by remember { mutableStateOf(emptyList<GithubComment>()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var replyImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPostingComment by remember { mutableStateOf(false) }
    var commentError by remember { mutableStateOf<String?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> replyImageUri = uri }

    LaunchedEffect(report.number) {
        isLoading = true
        loadError = null

        val issueResult = githubClient.getIssue(report.number)
        if (issueResult.isSuccess) {
            issue = issueResult.getOrNull()
            val fetched = issueResult.getOrNull()!!
            if (fetched.state != report.status) {
                onStatusChanged(report.copy(status = fetched.state))
            }
        } else {
            loadError =
                "Failed to load issue: ${issueResult.exceptionOrNull()?.message}"
        }

        val commentsResult = githubClient.getComments(report.number)
        if (commentsResult.isSuccess) {
            comments = commentsResult.getOrNull() ?: emptyList()
        }

        isLoading = false
    }

    val view = LocalView.current
    val density = LocalDensity.current
    val navBarBottomDp = remember {
        val rootInsets = view.rootWindowInsets
        val bottomPx = if (rootInsets != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rootInsets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom
            } else {
                @Suppress("DEPRECATION")
                rootInsets.systemWindowInsetBottom
            }
        } else 0
        with(density) { bottomPx.toDp() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            color = Slate900,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Issue #${report.number}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate50
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val isOpen = report.status.equals("open", true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOpen) {
                                Emerald.copy(alpha = 0.2f)
                            } else {
                                Rose.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = report.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isOpen) Emerald else Rose,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate300
                        )
                    }
                }

                Divider(color = Slate700)

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Indigo)
                    }
                } else if (loadError != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                loadError!!,
                                color = Rose,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                report.title,
                                color = Slate300,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                formatDate(report.createdAt),
                                color = Slate500,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        issue?.let { githubIssue ->
                            Text(
                                githubIssue.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate50
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Created: ${formatDate(githubIssue.createdAt)}",
                                color = Slate500,
                                style = MaterialTheme.typography.bodySmall
                            )
                            githubIssue.body?.let { body ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Slate800)
                                ) {
                                    Text(
                                        body,
                                        modifier = Modifier.padding(12.dp),
                                        color = Slate300,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        if (comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Comments (${comments.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate50
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            comments.forEach { comment ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Slate800
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Cyan
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                comment.user.login,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Cyan
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                formatDate(comment.createdAt),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate500
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            comment.body,
                                            color = Slate300,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }

                Divider(color = Slate700)

                Column(modifier = Modifier.padding(
                    start = 12.dp, end = 12.dp, top = 12.dp,
                    bottom = 12.dp + navBarBottomDp
                )) {
                    commentError?.let { error ->
                        Text(
                            error,
                            color = Rose,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { pickMediaLauncher.launch("image/*") },
                            enabled = !isPostingComment
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = if (replyImageUri != null) Cyan else Slate500
                            )
                        }
                        if (replyImageUri != null) {
                            IconButton(onClick = { replyImageUri = null }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = Rose
                                )
                            }
                        }
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Write a reply...") },
                            singleLine = true,
                            enabled = !isPostingComment
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isBlank()) {
                                    commentError = "Reply cannot be empty."
                                    return@IconButton
                                }

                                commentError = null
                                isPostingComment = true

                                scope.launch {
                                    try {
                                        var imageUrl: String? = null

                                        if (replyImageUri != null) {
                                            val base64 = withContext(Dispatchers.IO) {
                                                ImageHelper.uriToBase64(
                                                    context,
                                                    replyImageUri!!
                                                )
                                            }
                                            val fileName = ImageHelper.generateFileName()
                                            val uploadResult =
                                                githubClient.uploadAsset(fileName, base64)
                                            if (uploadResult.isFailure) {
                                                commentError =
                                                    "Image upload failed: " +
                                                        uploadResult.exceptionOrNull()?.message
                                                isPostingComment = false
                                                return@launch
                                            }
                                            imageUrl = uploadResult.getOrNull()
                                        }

                                        val body = buildString {
                                            appendLine("## Reply")
                                            appendLine()
                                            appendLine(replyText)
                                            if (imageUrl != null) {
                                                appendLine()
                                                appendLine("## Attachment")
                                                appendLine()
                                                appendLine("![Screenshot]($imageUrl)")
                                            }
                                        }

                                        val result =
                                            githubClient.postComment(report.number, body)
                                        if (result.isSuccess) {
                                            replyText = ""
                                            replyImageUri = null
                                            val commentsResult =
                                                githubClient.getComments(report.number)
                                            if (commentsResult.isSuccess) {
                                                comments =
                                                    commentsResult.getOrNull() ?: emptyList()
                                            }
                                        } else {
                                            commentError =
                                                "Failed to post: " +
                                                    result.exceptionOrNull()?.message
                                        }
                                    } catch (e: Exception) {
                                        commentError = "Error: ${e.message}"
                                    } finally {
                                        isPostingComment = false
                                    }
                                }
                            },
                            enabled = !isPostingComment && replyText.isNotBlank()
                        ) {
                            if (isPostingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Indigo,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Indigo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(uri: Uri) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use {
                    bitmap = BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
            }
        }
    }

    bitmap?.let { bmp ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        for (format in inputFormats) {
            try {
                val date = format.parse(dateString)
                if (date != null) return outputFormat.format(date)
            } catch (_: Exception) {
                continue
            }
        }
        dateString
    } catch (_: Exception) {
        dateString
    }
}
