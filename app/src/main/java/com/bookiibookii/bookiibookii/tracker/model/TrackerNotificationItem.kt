package com.bookiibookii.bookiibookii.tracker.model

data class TrackerNotificationItem(
    val groupId: Long,
    val dDay: String,
    val template: String,
    val nickname: String,
    val bookTitle: String,
    val remainingSeconds: Long,
    val subText: String,
)
