package com.bookiibookii.bookiibookii.tracker.model

data class TrackerMainUiState(
    val cards: List<TrackerCardModel> = emptyList(),
    val nickname: String = "",
    val notifications: List<TrackerNotificationItem> = emptyList(),
    val totalCount: Int = 0,
    val readingCount: Int = 0,
    val exchangingCount: Int = 0,
    val reviewCount: Int = 0,
    val hasNewNotification: Boolean = false,
    val loading: Boolean = false,
    // 첫 조회(load)가 한 번이라도 끝났는지. EmptyCard는 이게 true일 때만 노출
    val hasLoadedOnce: Boolean = false,
    val error: String? = null,
)
