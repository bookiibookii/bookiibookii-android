package com.bookiibookii.bookiibookii.data.model.library

// ==========================================
// [2] 독서카드 목록 조회 (DetailFragment용 - 이어읽기/함께읽기 공통)
// ==========================================
data class GroupCardListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: GroupCardResult?
)

data class GroupCardResult(
    val groupId: Int,
    val currentBookOwner: OwnerInfo?,
    val myComment: String?,
    val partnerComment: String?,
    val togetherComments: List<TogetherComment>?, // ★ [추가됨] 함께읽기 코멘트 배열
    val cards: List<CardItem>
)

// ★ [추가됨] 함께읽기 코멘트 모델
data class TogetherComment(
    val userId: Int,
    val nickname: String,
    val comment: String
)

data class OwnerInfo(
    val matchedMemberId: Int,
    val nickname: String
)

data class CardItem(
    val cardId: Int,
    val page: Int,
    val memo: String,
    val cardImage: CardImage?,
    val createdAt: String,
    val bookTitle: String,
    val isBookmarked: Boolean,
    val creatorName: String,
    val profileImageUrl: String? = null,
    val commentCount: Int = 0
)

data class CardImage(
    val cardImageId: Int,
    val s3Key: String,
    val presignedGetUrl: String
)

// ==========================================
// [3] 카드 상세 조회 (LibraryCardDetailFragment)
// ==========================================
data class CardDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CardDetailResult?
)

data class CardDetailResult(
    val cardId: Int,
    val page: Int,
    val memo: String,
    val cardImage: CardImage?,
    val createdAt: String,
    val bookTitle: String,
    val isBookmarked: Boolean?,
    val creatorName: String
)

// ==========================================
// [5] 카드 생성/수정/이미지 업로드
// ==========================================

// 카드 생성 요청
data class CreateCardRequest(
    val s3Key: String,
    val page: Int,
    val memo: String
)

// 카드 수정 요청
data class UpdateCardRequest(
    val page: Int,
    val memo: String,
    val s3Key: String? = null
)

// 카드 생성/수정 성공 응답
data class CardOperationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CardDetailResult?
)

// 카드 생성 성공 응답
data class CreateCardResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CardDetailResult? // 생성된 카드 정보
)

// 북마크 토글 응답
data class BookmarkToggleResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BookmarkResult?
)

data class BookmarkResult(
    val bookmarked: Boolean
)

// 북마크 목록 조회 응답 (GET /api/cards/bookmarks)
data class BookmarkListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<CardItem>? // CardItem은 기존에 정의한 것 재사용
)
