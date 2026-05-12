package com.bookiibookii.bookiibookii.home.notification.vm

import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem

data class NotificationUiState(
    val items: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val errorType: Int? = null
)
