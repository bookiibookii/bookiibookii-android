package com.bookiibookii.bookiibookii.data.model.mypage

// GET /api/mypage, GET /api/profiles/{nickname}
data class UserProfileResDTO(
    val userId: Long,
    val profileImageUrl: String?,
    val nickname: String,
    val introduction: String?,
    val userBooks: List<UserBookDto>?,
    val bookReviewCount: Int,
    val recentBookReviews: List<BookReviewSummaryDto>?,
    val boomUpCount: Int,
    val recentReceivedReviews: List<ReceivedMemberReviewDto>?,
)

data class UserBookDto(
    val title: String,
    val auth: String,
    val image: String?,
)

data class BookReviewSummaryDto(
    val bookTitle: String,
    val bookAuthor: String,
    val tradeType: String,
    val rating: Double,
    val comment: String?,
    val reviewDate: String?,
)

data class ReceivedMemberReviewDto(
    val reviewerNickname: String,
    val reviewerProfileUrl: String?,
    val reaction: String,
    val comment: String?,
    val createdAt: String?,
)

// PATCH /api/mypage
data class MypageReqDTO(
    val nickname: String,
    val gender: String? = null,
    val birth: String? = null,
    val s3Key: String? = null,
)

// PATCH /api/mypage/introduction
data class UpdateIntroductionReqDTO(
    val introduction: String?,
)

// POST /api/users/me/withdrawal
data class WithdrawalReqDTO(
    val reason: String,
    val customReason: String? = null,
)

// 하위 호환 유지용 (LoginActivity, user/Profile.kt에서 참조 중 — 해당 파일 담당팀 업데이트 전까지 유지)
data class MypageResult(
    val userId: Int,
    val profileImageUrl: String?,
    val nickname: String,
    val manner: Double,
    val topTags: List<String>?,
    val completeBook: Int,
    val relayGroup: Int,
    val togetherGroup: Int,
    val userBadges: List<UserBadge>?,
    val groups: List<MypageGroup>?,
    val books: List<MypageBook>?,
)

data class MypageGroup(
    val groupId: Int,
    val bookTitle: String,
    val auth: String,
    val GENRE: String,
    val group_status: String,
    val groupTags: List<String>,
)

data class MypageBook(
    val bookTitle: String,
    val rating: Double,
)

data class UserBadge(
    val userBadge: String,
    val count: Int,
)
