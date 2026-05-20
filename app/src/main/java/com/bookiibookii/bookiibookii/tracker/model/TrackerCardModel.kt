package com.bookiibookii.bookiibookii.tracker.model

data class TrackerCardModel(
    val groupName: String,
    val bookTitle: String,
    val progressLabel: String,
    val dDay: String,
    val left: TrackerProfileItem,
    val right: TrackerProfileItem,
    val primaryActionLabel: String,
    val secondaryActionLabel: String,
)

data class TrackerProfileItem(
    val nickname: String,
    val bookTitle: String,
    val bookCoverUrl: String?,
    val profileImageUrl: String?,
    val progressPercent: Int,
    val isMine: Boolean,
)
