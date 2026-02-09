package com.bookiibookii.bookiibookii.home.notification.model

import com.bookiibookii.bookiibookii.data.model.NotificationItemDto

data class HomNotiItem(
    val notification: NotificationItemDto,
    val timeText: String,
    val bookTitle: String = "",
    val isUnread: Boolean = false
) {
    val id: Long get() = notification.id
    val title: String get() = notification.title
    val body: String get() = notification.message
}