package com.bookiibookii.bookiibookii.data.model.mypage

data class RelayReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: RelayReviewResult?
)

data class RelayReviewResult(
    val reviews: List<RelayReview>
)

data class RelayReview(
    val groupId: Long,
    val bookTitle: String,
    val bookImage: String?,
    val startDate: String,
    val finishedDate: String,
    val partnerNickname: String,
    val partnerToMeRating: Double, // 나에 대한 별점
    val partnerToMeComment: String, // 나에 대한 코멘트
    val partnerBadges: List<Badge>, // 태그 리스트
    val partnerBookRating: Double, // 책에 대한 별점
    val partnerBookComment: String, // 책에 대한 코멘트
    val partnerBookReviewDate: String
)

data class Badge(
    val code: String,
    val description: String
)
