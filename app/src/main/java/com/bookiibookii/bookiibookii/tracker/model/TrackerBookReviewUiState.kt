package com.bookiibookii.bookiibookii.tracker.model

data class TrackerBookReviewUiState(
    val bookTitle: String = "",
    val bookImageUrl: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
)
