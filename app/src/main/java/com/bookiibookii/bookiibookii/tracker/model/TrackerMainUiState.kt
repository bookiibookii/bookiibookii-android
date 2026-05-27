package com.bookiibookii.bookiibookii.tracker.model

data class TrackerMainUiState(
    val cards: List<TrackerCardModel> = emptyList(),
    val totalCount: Int = 0,
    val readingCount: Int = 0,
    val exchangingCount: Int = 0,
    val reviewCount: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)
