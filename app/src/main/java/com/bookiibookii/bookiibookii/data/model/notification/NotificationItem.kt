package com.bookiibookii.bookiibookii.data.model.notification

enum class NotificationCategory {
    SYSTEM,
    KEYWORD
}

data class NotificationItem(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val payload: Map<String, Any?>?
)
