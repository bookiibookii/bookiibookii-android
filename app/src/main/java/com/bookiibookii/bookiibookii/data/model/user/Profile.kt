package com.bookiibookii.bookiibookii.data.model.user

import com.bookiibookii.bookiibookii.data.model.mypage.MypageBook
import com.bookiibookii.bookiibookii.data.model.mypage.MypageGroup
import com.bookiibookii.bookiibookii.data.model.mypage.UserBadge
import com.google.gson.annotations.SerializedName

data class OtherProfileResult(
    val userId: Int,
    val profileImageUrl: String?,
    val nickname: String,
    val manner: Double,
    val topTags: List<String>?,
    val completeBook: Int,

    @SerializedName("relayGroup")
    val readingGroup: Int,

    val togetherGroup: Int,
    val userBadges: List<UserBadge>?,
    val groups: List<MypageGroup>?,
    val books: List<MypageBook>?
)
