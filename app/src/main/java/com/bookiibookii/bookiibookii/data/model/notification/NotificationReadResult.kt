package com.bookiibookii.bookiibookii.data.model.notification

// PATCH /api/notifications/{notificationId}/read 응답
data class NotificationReadResult(
    val id: Long,
    val type: String,
    val isRead: Boolean,
    val readAt: String?,
    val payload: Map<String, Any?>?
)
