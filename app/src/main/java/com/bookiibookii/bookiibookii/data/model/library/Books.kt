package com.bookiibookii.bookiibookii.data.model.library

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
    val hostNickName: String?,
    val startDate: String,
    val endDate : String?,
    val duration: Int,
    val rating: Double,
    val comment: String?,
    val groupType: String ,           // "RELAY" or "TOGETHER"
    val groupStatus : String,
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
    var isMine: Boolean
)

enum class ReadStatus { READING, DONE }
