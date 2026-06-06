package com.bookiibookii.bookiibookii.tracker.model

data class TrackerCardModel(
    val groupId: Long,
    val groupName: String,
    val bookTitle: String,
    val progressLabel: String,
    val dDay: String,
    val left: TrackerProfileItem,
    val right: TrackerProfileItem,
    val primaryAction: TrackerAction = TrackerAction.None,
    val secondaryAction: TrackerAction = TrackerAction.None,
    val isHost: Boolean = false,
)

data class TrackerProfileItem(
    val nickname: String,
    val bookTitle: String,
    val bookCoverUrl: String?,
    val profileImageUrl: String?,
    val progressPercent: Int,
    val isOwnerBook: Boolean,
    val totalPages: Int = 0,
)
