package com.bookiibookii.bookiibookii.tracker.model

data class TrackerBookReviewUiState(
    val bookTitle: String = "",
    val bookImageUrl: String? = null,
    // 수정 모드 프리필용 — 기존 후기 별점(0~5)·내용. 작성 모드면 기본값 유지.
    val initialStar: Double = 0.0,
    val initialComment: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)
