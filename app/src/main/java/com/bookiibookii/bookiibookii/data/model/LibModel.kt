package com.bookiibookii.bookiibookii.data.model

// [1] 책 목록 조회 (LibraryFragment)
data class LibraryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<BookResult>? // 리스트가 비어있거나 null일 수 있음
)

data class BookResult(
    val userBookId: Int,
    val bookId: Int,
    val title: String,
    val author: String,
    val image: String?,              // ★ 수정: URL은 없을 수 있으므로 Nullable 권장
    val hostId: Int,
    val hostProfileImageUrl: String?, // ★ 수정: Nullable 권장
    val startDate: String,
    val duration: Int,
    val rating: Double,
    val comment: String?,
    val groupType: String            // "RELAY" or "TOGETHER"
)

// UI에서 사용하는 모델 (Adapter용)
data class LibBook(
    val id: Int,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val hostProfileUrl: String?,
    val readStatus: ReadStatus,
    val progress: String?,
    val rating: Double,
    val groupType: String // ★ 추가됨: 프래그먼트 분기 처리를 위해 필요
)

enum class ReadStatus { READING, DONE }

// ==========================================
// [2] 독서카드 목록 조회 (DetailFragment용 - 이어읽기/함께읽기 공통)
// ==========================================
data class CardListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CardListResult?
)

data class CardListResult(
    val groupId: Int,
    val cards: List<CardItem>
)

data class CardItem(
    val cardId: Int,
    val page: Int,
    val memo: String,
    val cardImage: CardImage?,
    val createdAt: String,
    val bookTitle: String,
    val isBookmarked: Boolean,
    // ★ 이어읽기(Relay) 전용 필드
    val mycomment: String?,
    val partnercomment: String?,
    // ★ 소유권 확인 (빈 화면 분기 처리용)
    val bookOwn: BookOwn?
)

data class BookOwn(
    val my: Boolean // true: 내 책(호스트), false: 남의 책(게스트)
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
    // ★ 상세 화면에서 수정/삭제 버튼 노출 여부를 위해 필요
    val isMine: Boolean = false,
    // ★ 상세 화면 프로필 표시용
    val writerName: String? = null,
    val writerProfile: String? = null
)

// ==========================================
// [4] 댓글 목록 조회
// ==========================================
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

// ==========================================
// [5] 카드 생성/수정/이미지 업로드
// ==========================================

//// Presigned URL 발급 응답
//data class PresignedUrlResponse(
//    val isSuccess: Boolean,
//    val code: String,
//    val message: String,
//    val result: PresignedUrlResult?
//)
//
//data class PresignedUrlResult(
//    val s3Key: String,
//    val presignedPutUrl: String
//)

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

// ==========================================
// [6] 리뷰 작성 (함께읽기)
// ==========================================

// 리뷰 작성 요청 Body
data class ReviewRequest(
    val rating: Double,
    val comment: String
)

// 공통 응답 (결과 데이터가 String이거나 없을 때 사용)
data class BaseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String?
)