package com.bookiibookii.bookiibookii.home.notification.vm

import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.error.model.ErrorType

data class NotificationUiState(
    val items: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val errorType: ErrorType? = null
)
