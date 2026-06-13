package com.bookiibookii.bookiibookii.data.model.library

// 라이브러리 멤버북 목록 조회 응답 (GET /api/library/memberbooks)
data class LibraryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<BookResult>?
)

data class BookResult(
    val groupId: Int,
    val groupName: String,
    val memberBookId: Int,
    val bookId: Int,
    val title: String,
    val author: String,
    val image: String?,
    val hostId: Int,
    val hostProfileImageUrl: String?,
    val hostNickName: String?,
    val startDate: String,
    val endDate: String?,
    val genre: String? = null,
    val completedAt: String? = null,
    val totalPages: Int? = null,
    val duration: Int,
    val progressRate: Int,
    val rating: Double,
    val comment: String?,
    val groupType: String,
    val groupStatus: String,
    val mine: Boolean,
)

// UI 모델 (Fragment/ViewModel 용)
data class LibBook(
    val groupId: Int,
    val memberBookId: Int,
    val hostName: String?,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val startDate: String,
    val endDate: String?,
    val isReviewed: Boolean,
    val hostProfileUrl: String?,
    val readStatus: ReadStatus,
    val progressRate: Int,
    val rating: Double,
    val groupType: String,
    val groupState: String,
    var isMine: Boolean
)

enum class ReadStatus { READING, DONE }
