package com.bookiibookii.bookiibookii.bookData.Data

data class City(
    val name: String,
    val districts: List<String>
)

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
// 공지사항 데이터
data class MypNotice(
    val title: String,
    val content: String,
    val date: String,
    val isNew: Boolean = false
)

// 문의 데이터
data class MypQuestion(
    val id: Long,
    val title: String,
    val content: String,
    val date: String,
    val answer: String? = null, // null이면 답변 대기중
    val answerDate: String? = null
)

// 신고 데이터
data class MypReport(
    val id: Long,
    val targetName: String,
    val title: String,
    val content: String,
    val date: String,
    val state: String, // "답변 대기 중" or "답변 완료"
    val answer: String? = null
)