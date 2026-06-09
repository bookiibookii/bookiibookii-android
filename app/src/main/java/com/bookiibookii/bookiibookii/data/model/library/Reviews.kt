package com.bookiibookii.bookiibookii.data.model.library

// ── 그룹 리뷰 조회 응답 (GET /api/groups/{groupId}/reviews) ──────────────────

data class GroupReviewsResponseDTO(
    val bookReviews: List<BookReviewItemDTO>,
    val memberReviews: List<MemberReviewItemDTO>,
)

// 책 리뷰 항목 (스펙 BookReviewItem) — 그룹 리뷰 조회/내 책 리뷰/일괄 수정 응답 공용.
// reviewType(MY_BOOK|PARTNER_BOOK)으로 내 책/파트너 책 리뷰를 구분.
data class BookReviewItemDTO(
    val reviewId: Int,
    val reviewType: String?,
    val groupId: Int?,
    val bookId: Int?,
    val bookTitle: String?,
    val bookAuthor: String?,
    val bookImageUrl: String?,
    val rating: Double?,
    val content: String?,
    val isEditable: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
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

// ── 내 책 리뷰 목록 조회 (GET /api/groups/{groupId}/reviews/book/me) ──────────
// 수정 시 reviewId 확보용. 항목은 BookReviewItemDTO 공용.
data class MyBookReviewsResponseDTO(
    val reviews: List<BookReviewItemDTO>?,
)

// ── 책 리뷰 등록/수정 ────────────────────────────────────────────────────────

// 책 리뷰 등록/수정 요청 (POST /api/groups/{groupId}/reviews, PATCH /api/groups/{groupId}/reviews/book/{reviewId})
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

// ── 내 그룹 리뷰 일괄 수정 (PATCH /api/groups/{groupId}/reviews/my-group) ──────
// 책 리뷰들 + 파트너(멤버) 리뷰를 한 번에 수정. 요청/응답 항목 모두 optional.

data class MyGroupReviewsUpdateDTO(
    val bookReviews: List<BookReviewUpdateItemDTO>? = null,
    val memberReview: MemberReviewUpdateItemDTO? = null,
)

data class BookReviewUpdateItemDTO(
    val memberBookId: Int,       // required
    val star: Double? = null,
    val comment: String? = null,
)

data class MemberReviewUpdateItemDTO(
    val reaction: String? = null,  // BOOM_UP | BOOM_DOWN
    val comment: String? = null,
)

// 수정 응답 — bookReviews는 BookReviewItem(=BookReviewItemDTO), memberReview는 MemberReviewItem(=MemberReviewItemDTO)
data class MyGroupReviewsResponseDTO(
    val bookReviews: List<BookReviewItemDTO>? = null,
    val memberReview: MemberReviewItemDTO? = null,
)
