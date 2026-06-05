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

enum class TrackerStepLabelStyle { Main, Sub }

data class TrackerDetailUiState(
    val groupName: String = "",
    val dDay: String = "",
    val statusLabel: String = "",
    val currentStepLabel: String = "",
    val currentStepLabelStyle: TrackerStepLabelStyle = TrackerStepLabelStyle.Main,
    val currentStepPosition: Int = 1, // 1..4 — StatusProgressBar 칩 위치

    val myProfile: TrackerProfileItem = EmptyProfile,
    val partnerProfile: TrackerProfileItem = EmptyProfile,
    val exchangeLabel: String = "",
    val primaryAction: TrackerAction = TrackerAction.None,
    val secondaryAction: TrackerAction = TrackerAction.None,
    // 호스트만 약속 등록 가능 — 게스트는 약속 등록 버튼 비활성화
    val isHost: Boolean = false,
    val steps: List<TrackerStep> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)
