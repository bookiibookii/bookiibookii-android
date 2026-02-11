package com.bookiibookii.bookiibookii.data.model

import com.google.gson.annotations.SerializedName

data class RecommendedGroupDto(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("bookImageUrl") val bookImageUrl: String?
)

data class RecommendedBookmateDto(
    @SerializedName("userId") val userId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?, // presigned GET url, 없으면 null
    @SerializedName("matchedTags") val matchedTags: List<String>?,
    @SerializedName("recentBookTitle") val recentBookTitle: String?
)