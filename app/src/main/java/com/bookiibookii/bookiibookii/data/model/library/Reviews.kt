package com.bookiibookii.bookiibookii.data.model.library

// ── 그룹 리뷰 조회 응답 (GET /api/groups/{groupId}/reviews) ──────────────────

data class GroupReviewsResponseDTO(
    val bookReviews: List<BookReviewItemDTO>,
    val memberReviews: List<MemberReviewItemDTO>,
)

data class BookReviewItemDTO(
    val bookId: Int,
    val bookTitle: String,
    val bookAuthor: String?,
    val bookImage: String?,
    val writerId: Int,
    val writerNickname: String,
    val writerProfileImageUrl: String?,
    val star: Double,
    val comment: String?,
    val createdAt: String,
)

data class MemberReviewItemDTO(
    val groupName: String?,
    val readingPeriod: Int?,
    val writerId: Int,
    val writerNickname: String,
    val writerProfileImageUrl: String?,
    val reaction: String,
    val comment: String?,
)

// ── 책 리뷰 등록/수정 ────────────────────────────────────────────────────────

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
