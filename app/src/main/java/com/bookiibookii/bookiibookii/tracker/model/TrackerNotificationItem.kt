package com.bookiibookii.bookiibookii.tracker.model

import androidx.compose.ui.text.AnnotatedString

data class TrackerNotificationItem(
    val groupId: Long,
    val dDay: String,
    val body: AnnotatedString,
    val subText: String,
)
