package com.bookiibookii.bookiibookii.tracker.model

data class TrackerPartnerReviewUiState(
    val groupName: String = "",
    val myNickname: String = "",
    val myBookTitle: String = "",
    val myBookCoverUrl: String? = null,
    val myProfileImageUrl: String? = null,
    val partnerNickname: String = "",
    val partnerBookTitle: String = "",
    val partnerBookCoverUrl: String? = null,
    val partnerProfileImageUrl: String? = null,
    val loading: Boolean = false,
    // 후기 제출 진행 중 — 더블탭으로 중복 제출/중복 네비 방지용
    val submitting: Boolean = false,
    val error: String? = null,
)
