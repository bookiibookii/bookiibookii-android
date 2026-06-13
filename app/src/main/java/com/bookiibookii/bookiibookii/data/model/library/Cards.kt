package com.bookiibookii.bookiibookii.data.model.library

// 카드 목록 조회 응답 (GET /api/member-books/group/{groupId}/cards)
data class MemberCardListResponseDTO(
    val groupId: Int,
    val cards: List<MemberCardResponseDTO>
)

// 카드 공통 응답 DTO — 목록/상세/북마크 모두 사용
data class MemberCardResponseDTO(
    val cardId: Int,
    val memberBookId: Int,
    val cardType: String,
    val page: Int,
    val totalPages: Int?,
    val memo: String?,
    val quotation: String?,
    val cardImage: MemberCardImageResponseDTO?,
    val createdAt: String,
    val completedAt: String?,
    val genre: String?,
    val bookTitle: String?,
    val isMine: Boolean,
    val isBookmarked: Boolean,
    val creatorName: String?,
    val creatorProfileImageUrl: String?,
    val reactionCounts: List<MemberCardReactionCountDTO>,
    val myReactions: List<String>,
)

data class MemberCardImageResponseDTO(
    val cardImageId: Int,
    val s3Key: String,
    val presignedGetUrl: String
)

data class MemberCardReactionCountDTO(
    val reaction: String,
    val count: Int
)

// 카드 생성 요청 (POST /api/member-books/{memberBookId}/cards)
data class MemberCardCreateRequestDTO(
    val cardType: String,
    val quotation: String,
    val s3Key: String,
    val page: Int,
    val memo: String,
    val quotationValidForText: Boolean = false,
    val s3KeyValidForImage: Boolean = false,
)

// 카드 수정 요청 (PATCH /api/member-books/cards/{cardId})
data class MemberCardUpdateRequestDTO(
    val page: Int,
    val memo: String,
    val quotation: String,
    val s3Key: String? = null
)

// 카드 생성 응답
data class MemberCardCreateResponseDTO(
    val cardId: Int,
    val cardType: String,
    val page: Int,
    val memo: String,
    val quotation: String,
    val cardImage: MemberCardImageResponseDTO?,
    val createdAt: String,
    val creatorName: String,
    val creatorProfileImageUrl: String?
)

// 북마크 토글 응답 (PATCH /api/member-books/cards/{cardId}/bookmark)
data class MemberCardBookmarkResponseDTO(
    val bookmarked: Boolean
)

// 리액션 토글 요청/응답 (PATCH /api/member-books/cards/{cardId}/reactions)
data class MemberCardReactionToggleRequestDTO(
    val reaction: String
)

data class MemberCardReactionToggleResponseDTO(
    val reaction: String,
    val active: Boolean
)

// Presigned URL 응답 (POST /api/member-books/{memberBookId}/cards/presigned-url)
data class PresignedUrlResponseDTO(
    val s3Key: String,
    val presignedPutUrl: String
)

// 독서카드 공유 토큰 발급 응답 (POST /api/member-books/cards/{cardId}/share-token)
data class ShareTokenResponseDTO(
    val shareToken: String,
    val shareUrl: String
)

// 공유 토큰 기반 독서카드 공개 조회 응답 (GET /api/public/reading-cards/{shareToken})
// 공개 엔드포인트라 ApiResponse 래퍼 없이 DTO를 직접 반환한다. required 없음 → 전부 nullable.
data class PublicReadingCardResponseDTO(
    val cardType: String?,
    val bookTitle: String?,
    val bookAuthor: String?,
    val bookImage: String?,
    val creatorNickname: String?,
    val page: Int?,
    val memo: String?,
    val quotation: String?,
    val imageUrl: String?,
)
