package com.bookiibookii.bookiibookii.notification.model

import androidx.compose.ui.text.AnnotatedString
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem

// 서버 알림 DTO → 알림 센터 UI 모델
fun NotificationItem.toUiModel(): NotificationUiModel = NotificationUiModel(
    id = id,
    title = title,
    body = AnnotatedString(message),
    timeText = DateUtils.calculateTimeAgo(createdAt),
    bookTitle = "",
    isUnread = !isRead,
    iconStyle = if (isRead) NotificationIconStyle.NORMAL else NotificationIconStyle.HIGHLIGHT,
)
