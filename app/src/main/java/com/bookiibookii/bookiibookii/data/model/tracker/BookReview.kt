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

// 내 책 리뷰 목록 조회 — GET /api/groups/{groupId}/reviews/book/me
// 현재 로그인 사용자가 해당 그룹에서 작성한 책 리뷰만 반환(파트너 매너 리뷰 제외).
// 내 원래 책 리뷰(MY_BOOK)와 파트너 책 리뷰(PARTNER_BOOK)를 reviewType으로 구분.
// 작성한 리뷰가 없으면 빈 배열.
data class MyBookReviewsResDTO(
    val reviews: List<BookReviewItem>?,
)

data class BookReviewItem(
    val reviewId: Long?,
    val reviewType: String?,   // MY_BOOK | PARTNER_BOOK
    val groupId: Long?,
    val bookId: Long?,
    val bookTitle: String?,
    val bookAuthor: String?,
    val bookImageUrl: String?,
    val rating: Double?,
    val content: String?,
    val isEditable: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
)
