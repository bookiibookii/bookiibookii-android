package com.bookiibookii.bookiibookii.tracker.model

data class TrackerPartnerReviewUiState(
    val groupName: String = "",
    val myNickname: String = "",
    val myBookTitle: String = "",
    val partnerNickname: String = "",
    val partnerBookTitle: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)
