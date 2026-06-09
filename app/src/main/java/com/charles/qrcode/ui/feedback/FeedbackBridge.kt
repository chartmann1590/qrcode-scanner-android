package com.charles.qrcode.ui.feedback

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView

fun setupFeedbackView(composeView: ComposeView, activity: ComponentActivity) {
    composeView.setContent {
        FeedbackApp(activity)
    }
}
