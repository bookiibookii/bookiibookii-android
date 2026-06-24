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
    // 독서 기간 수정 다이얼로그용 원본 dDay(일 수). 종료일 = 오늘 + dDayCount
    val dDayCount: Int? = null,
    val statusLabel: String = "",
    val currentStepLabel: String = "",
    val currentStepLabelStyle: TrackerStepLabelStyle = TrackerStepLabelStyle.Main,
    val currentStepPosition: Int = 1, // 1..4 — StatusProgressBar 칩 위치

    val myProfile: TrackerProfileItem = EmptyProfile,
    val partnerProfile: TrackerProfileItem = EmptyProfile,
    val exchangeLabel: String = "",
    val primaryAction: TrackerAction = TrackerAction.None,
    val secondaryAction: TrackerAction = TrackerAction.None,
    // primary/secondary 버튼 활성화 여부 — 상태로 결정
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    // 읽기 진행률 바·% 텍스트 표시 여부 — 교환 단계 이후엔 숨김
    val showReadingProgress: Boolean = true,
    // 더보기 드롭다운의 "독서 기간 수정" 호스트 전용 노출 분기에 사용
    val isHost: Boolean = false,
    val steps: List<TrackerStep> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    // 삭제된/존재하지 않는 그룹(404) → 삭제된 페이지 화면
    val notFound: Boolean = false,
)
