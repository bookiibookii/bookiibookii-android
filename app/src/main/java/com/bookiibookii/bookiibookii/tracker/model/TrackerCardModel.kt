package com.bookiibookii.bookiibookii.tracker.model

data class TrackerCardModel(
    val groupId: Long,
    val groupName: String,
    // 헤더 "제목 · 상태" 표시용
    val displayBookTitle: String,
    val bookTitle: String,
    val progressLabel: String,
    val dDay: String,
    val left: TrackerProfileItem,
    val right: TrackerProfileItem,
    val primaryAction: TrackerAction = TrackerAction.None,
    val secondaryAction: TrackerAction = TrackerAction.None,
    // primary/secondary 버튼 활성화 여부 — 상태로 결정
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    // 읽기 진행률 바·% 텍스트 표시 여부 — 교환 단계 이후엔 숨김
    val showReadingProgress: Boolean = true,
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
    // 진행률 텍스트를 "%" 대신 다른 라벨로 표시할 때 사용(예: "교환 준비 완료")
    val progressLabelOverride: String? = null,
)
