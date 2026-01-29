package com.bookiibookii.bookiibookii.bookData.Data

data class LibBook(
    val id: Long = 0,
    val title: String,
    val author: String,
    val coverUrl: String? = null,

    val readStatus: ReadStatus,
    val progress: String? = null,
    val rating: Int? = null
)

enum class ReadStatus {
    READING, // 진행 중
    DONE     // 종료
}

data class LibReview(
    val id: Long,
    val userName: String,
    val content: String,
    val page: Int,
    val date: String,
    val profileImage: Int? = null,
    val reviewImageUri: String? = null,
    val isMine: Boolean = false
)

// MypDataModels.kt

// 1. 후기 데이터
data class MypReview(
    val content: String, // 예: "친절하고 매너가 좋아요"
    val count: Int       // 예: 8
)

// 2. 그룹 데이터
data class MypGroup(
    val title: String,
    val author: String,
    val isRecruiting: Boolean, // true: 모집 중, false: 모집 완료
    val tags: List<String>
)

// 3. 최근 읽은 책 데이터
data class MypLateBook(
    val title: String,
    val rating: Int // 1~5
)

data class ReviewData(
    val userName: String,
    val bookTitle: String,
    val author: String,
    val dateRange: String,
    val tags: List<String>,
    val rating: Float,
    val comment: String?,
    var isExpanded: Boolean = false
)

data class MyReviewData(
    val id: Int,
    val userName: String,
    val bookTitle: String,
    val author: String,
    val date: String,

    // 나의 리뷰 정보
    val myTags: List<String>,
    val myRating: Int,
    val myComment: String,

    // 파트너 리뷰 정보
    val partnerTags: List<String>,
    val partnerRating: Int,
    val partnerComment: String,

    var isExpanded: Boolean = false
)

data class City(
    val name: String,
    val districts: List<String>
)

// 기존 클래스들과 함께 관리
data class MypProfileData(
    val nickname: String,
    val name: String,
    val phone: String,
    val address: String,
    val addressDetail: String,
    val regionInfo: String // 직접 교환 정보 (시/도 시/군/구)
)

data class MyReceivedReview(
    val id: Long,
    val writerName: String,
    val bookTitle: String,
    val bookAuthor: String,
    val datePeriod: String, // "2025. 12. 18 ~ 2026. 01. 12"
    val tags: List<String>,
    val content: String,
    val partnerContent: String, // 파트너의 한줄평
    val timestamp: Long // 정렬용 시간
)