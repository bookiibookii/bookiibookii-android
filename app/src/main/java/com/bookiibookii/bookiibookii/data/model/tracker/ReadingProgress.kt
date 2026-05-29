package com.bookiibookii.bookiibookii.data.model.tracker

data class ReadingProgressReqDTO(
    val currentPage: Int,
)

data class ReadingProgressResDTO(
    val memberBookId: Long?,
    val currentPage: Int?,
    val totalPages: Int?,
    val progressRate: Int?,
    val readingStatus: String?,
    val readingStatusText: String?,
    val dDay: Int?,
)
