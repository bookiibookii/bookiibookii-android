package com.bookiibookii.bookiibookii.data.model.library

// [3] 리뷰 작성 요청
data class ReviewRequest(
    val rating: Double,
    val comment: String
)

data class RelayReviewRequest(
    val bookRating: Double,
    val bookComment: String,
    val partnerRating: Double,
    val partnerComment: String,
    val badgeCodes: List<String>
)

// POST /api/reviews/relay/{userBookId}/book — 책 리뷰만 단독 작성
data class RelayBookReviewRequest(
    val bookRating: Double,
    val bookComment: String
)
