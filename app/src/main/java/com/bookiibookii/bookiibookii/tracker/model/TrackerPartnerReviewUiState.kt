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
    val error: String? = null,
)
