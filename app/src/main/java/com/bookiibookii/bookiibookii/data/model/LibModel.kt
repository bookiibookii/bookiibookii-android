package com.bookiibookii.bookiibookii.data.model

data class LibraryResponse(
    val isSuccess : Boolean,
    val code : String,
    val message : String,
    val result : List<BookResult>
)

data class BookResult(
    val userBookId: Int,
    val bookId: Int,
    val title: String,
    val author: String,
    val image: String,              // 책 표지 URL
    val hostId: Int,
    val hostProfileImageUrl: String, // 호스트 프로필 URL
    val startDate: String,
    val duration: Int,              // 독서 기간 (일)
    val rating: Double,             // 0.0이면 읽는 중, > 0.0이면 완독으로 간주 (로직에 따라 변경 가능)
    val comment: String?
)

data class LibBook(
    val id : Int,
    val title: String,
    val author: String,
    val coverUrl: String?,      // 책 표지
    val hostProfileUrl: String?, // 호스트 프로필
    val readStatus: ReadStatus, // 상태 (READING, DONE)
    val progress: String?,      // "50% 읽음" or "p.120" 등
    val rating: Double
)

enum class ReadStatus { READING, DONE }

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
    val bookTitle: String
)

data class CardImage(
    val cardImageId: Int,
    val s3Key: String,
    val presignedGetUrl: String // 실제 이미지 로드용 URL
)

// [2] 댓글 목록 조회 응답
data class CommentListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CommentListResult?
)

data class CommentListResult(
    val totalCount: Int,
    val comments: List<CommentItem>
)

data class CommentItem(
    val id: Int,
    val content: String,
    val writer: CommentWriter,
    val createdAt: String
)

data class CommentWriter(
    val userId: Int,
    val name: String,
    val profileImage: String?
)

// [2] 카드 생성 요청
data class CreateCardRequest(
    val s3Key: String,
    val page: Int,
    val memo: String
)

// [3] 카드 수정 요청 (s3Key는 이미지가 변경된 경우에만 보냄)
data class UpdateCardRequest(
    val page: Int,
    val memo: String,
    val s3Key: String? = null
)

// [4] 카드 생성/수정 성공 응답 (공통)
data class CardOperationResponse(
    val isSuccess: Boolean,
    val result: CardDetailResult? // 기존에 정의한 CardDetailResult 재사용
)