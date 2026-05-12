package com.bookiibookii.bookiibookii.home.notification.model

import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem

data class NotificationUiItem(
    val notification: NotificationItem,
    val timeText: String,
    val bookTitle: String = "",
    val isUnread: Boolean = false
) {
    val id: Long get() = notification.id
    val title: String get() = notification.title
    val body: String get() = notification.message
}
