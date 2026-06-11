package com.bookiibookii.bookiibookii.data.model.mypage

// GET /api/mypage/reviews/written — 작성한 후기 목록(페이징)
data class WrittenReviews(
    val totalCount: Long,
    val content: List<WrittenReviewItem>,
    val pageInfo: PageInfo,
)

data class WrittenReviewItem(
    val reviewId: Long,
    val bookId: Long,
    val bookTitle: String?,
    val author: String?,
    val rating: Double,
    val content: String?,
    val exchangeType: String?,
    val exchangeTypeLabel: String?,
    val reviewedAt: String?,
)

// GET /api/mypage/reviews/received — 받은 후기 목록(페이징)
data class ReceivedReviews(
    val positiveCount: Long,
    val content: List<ReceivedReviewItem>,
    val pageInfo: PageInfo,
)

data class ReceivedReviewItem(
    val reviewId: Long,
    val reviewerId: Long,
    val reviewerNickname: String?,
    val reviewerProfileImageUrl: String?,
    val partnerReviewType: String?,
    val partnerReviewLabel: String?,
    val comment: String?,
    val reviewedAt: String?,
)

data class PageInfo(
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val hasNext: Boolean,
)
