package com.bookiibookii.bookiibookii.data.model.notification

data class NotificationListResult(
    val items: List<NotificationItem>,
    val nextCursor: String?,
    val hasNext: Boolean
)
