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

// 그룹 후기 전체 조회 — GET /api/groups/{groupId}/reviews
// 책 후기(bookReviews)와 파트너 후기(memberReviews)를 함께 반환.
// 기존 책 후기 프리필 시 writerId == 내 userId 인 항목을 골라 star/comment 사용.
data class GroupReviewsResDTO(
    val bookReviews: List<BookReviewItem>?,
    val memberReviews: List<MemberReviewItem>?,
)

data class BookReviewItem(
    val bookId: Long?,
    val bookTitle: String?,
    val bookAuthor: String?,
    val bookImage: String?,
    val writerId: Long?,
    val writerNickname: String?,
    val writerProfileImageUrl: String?,
    val star: Double?,
    val comment: String?,
    val createdAt: String?,
)

data class MemberReviewItem(
    val groupName: String?,
    val readingPeriod: Int?,
    val writerId: Long?,
    val writerNickname: String?,
    val writerProfileImageUrl: String?,
    val reaction: String?,
    val comment: String?,
)
