package com.bookiibookii.bookiibookii.data.model

// [1] 책 목록 조회 (LibraryFragment)
data class LibraryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<BookResult>? // 리스트가 비어있거나 null일 수 있음
)

data class BookResult(
    val groupId : Int,
    val userBookId: Int,
    val bookId: Int,

    val title: String,
    val author: String,
    val image: String?,              // ★ 수정: URL은 없을 수 있으므로 Nullable 권장
    val hostId: Int,
    val hostProfileImageUrl: String?, // ★ 수정: Nullable 권장
    val hostNickname: String?,
    val startDate: String,
    val endDate : String?,
    val duration: Int,
    val rating: Double,
    val comment: String?,
    val groupType: String ,           // "RELAY" or "TOGETHER"
    val groupState : String,
)

// UI에서 사용하는 모델 (Adapter용)
data class LibBook(
    val groupId : Int,
    val id: Int,
    val hostName : String?,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val startDate: String,
    val endDate : String?,
    val isReviewed: Boolean,
    val hostProfileUrl: String?,
    val readStatus: ReadStatus,
    val progress: String?,
    val rating: Double,
    val groupType: String, // ★ 추가됨: 프래그먼트 분기 처리를 위해 필요
    val groupState : String,
    val isMine: Boolean
)

enum class ReadStatus { READING, DONE }

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
    val currentBookOwner: OwnerInfo?, // 현재 책 소유자 (이어읽기용)
    val myComment: String?,           // 내 한줄평/후기
    val partnerComment: String?,      // 상대 한줄평/후기
    val cards: List<CardItem>
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
    val creatorName: String // 작성자 이름
)

data class CardImage(
    val cardImageId: Int,
    val s3Key: String,
    val presignedGetUrl: String
)

// [3] 리뷰 작성 요청
data class ReviewRequest(
    val rating: Double,
    val comment: String
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
    val writerProfile: String? = null,
    val isBookmarked: Boolean?,
    val creatorName : String
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
    val profileImageUrl: String?
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

// 공통 응답 (결과 데이터가 String이거나 없을 때 사용)
data class BaseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String?
)

data class PostCommentRequest(
    val content: String
)

// 댓글 작성 응답 (Result가 content만 오는 경우)
data class PostCommentResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: PostCommentResult?
)

data class PostCommentResult(
    val content: String
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

data class TrackerResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<TrackerResult>?
)

data class TrackerResult(
    val groupId: Int,
    val groupType: String, // "TOGETHER" or "RELAY" (API 문서상 tradeType으로 명시됐으나 JSON엔 groupType도 보임, JSON 키값 확인 필요. 여기선 JSON 예시대로 작성)
    val tradeType: String?, // 예시 JSON에 tradeType: "DELIVERY" 등. TOGETHER일 수도 있음.

    // Together 상세 정보
    val togetherDetail: TogetherDetail?,

    // Relay 상세 정보 (필요시 사용)
    val relayDetail: RelayDetail?
)

data class TogetherDetail(
    val hostNickname: String?,
    val participantCount: Int,
    val myReadingRate: Int,    // ★ 내 독서율
    val groupReadingRate: Int  // ★ 그룹 평균 독서율
)

data class RelayDetail(
    val trackerStatus: String?
    // 기타 필드 생략
)

data class CompleteReadingResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CompleteReadingResult?
)

data class CompleteReadingResult(
    val matchedMemberId: Int,
    val currentReadingRate: Int,
    val completedAt: String
)

data class RelayReviewRequest(
    val bookRating: Double,      // 책 별점 (0.5 단위)
    val bookComment: String,     // 책 코멘트
    val partnerRating: Double,   // 파트너 별점 (0.5 단위)
    val partnerComment: String,  // 파트너 코멘트
    val badgeCodes: List<String> // 선택된 배지(태그) 영문 코드 리스트
)