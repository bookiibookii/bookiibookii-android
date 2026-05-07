package com.bookiibookii.bookiibookii.data.model.mypage

data class ProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ProfileResult?
)

data class ProfileResult(
    val userId: Int,
    val profileImageUrl: String?,
    val nickname: String,
    val manner: Double,
    val topTags: List<String>,
    val completeBook: Int,
    val readingGroup: Int,
    val togetherGroup: Int,
    val userBadges: List<UserBadge>,
    val groups: List<MypageGroup>,
    val books: List<MypageBook>
)
