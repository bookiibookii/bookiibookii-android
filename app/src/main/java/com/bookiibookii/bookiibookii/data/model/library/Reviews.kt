package com.bookiibookii.bookiibookii.data.model.library

// 책 리뷰 등록/수정 요청 (POST /api/groups/{groupId}/reviews, PATCH /api/groups/{groupId}/reviews/me)
data class BookReviewUpsertDTO(
    val star: Double,
    val comment: String
)

// 파트너 후기 등록 요청 (POST /api/groups/{groupId}/member-reviews)
data class MemberReviewCreateDTO(
    val reaction: String,
    val comment: String
)

// 책 리뷰 응답
data class BookReviewResponseDTO(
    val reviewId: Int,
    val groupId: Int,
    val memberBookId: Int,
    val star: Double,
    val comment: String,
    val readingStatus: String,
    val exchangeStatus: String
)

// 파트너 후기 등록 응답
data class MemberReviewResponseDTO(
    val reviewId: Int,
    val groupCompleted: Boolean
)
