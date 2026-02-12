package com.bookiibookii.bookiibookii.group.main

data class GroupData(
    val groupId : Int,
    val coverImgUrl: String,
    val bookTitle: String,
    val bookAuthor: String,
    val genre: String,
    val status: String,
    val readingPeriod: String,
    val memberCount: String,
    val isHot: Boolean,
    val profileImgUrl: String?,
    val nickname: String,
    val date: String,
    val tags: List<String>,
    val customTag : String?,
    val groupType: String,
    val tradeType: String?,
    val maxMemberCount: Int?,
    val badgeContent: String // ★ [추가] 칩에 들어갈 텍스트 ("택배", "송파구" 등)
)