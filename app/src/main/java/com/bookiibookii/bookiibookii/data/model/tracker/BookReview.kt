package com.bookiibookii.bookiibookii.data.model.tracker

data class BookReviewReqDTO(
    val star: Double,
    val comment: String?,
)

data class BookReviewResDTO(
    val reviewId: Long?,
    val groupId: Long?,
    val memberBookId: Long?,
    val star: Double?,
    val comment: String?,
    val readingStatus: String?,
    val exchangeStatus: String?,
)
