package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep

private val EmptyProfile = TrackerProfileItem(
    nickname = "",
    bookTitle = "",
    bookCoverUrl = null,
    profileImageUrl = null,
    progressPercent = 0,
    isOwnerBook = false,
)

data class TrackerDetailUiState(
    val groupName: String = "",
    val dDay: String = "",
    val statusLabel: String = "",
    val currentStepLabel: String = "",
    val myProfile: TrackerProfileItem = EmptyProfile,
    val partnerProfile: TrackerProfileItem = EmptyProfile,
    val exchangeLabel: String = "",
    val primaryActionLabel: String = "",
    val secondaryActionLabel: String = "",
    val steps: List<TrackerStep> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)
