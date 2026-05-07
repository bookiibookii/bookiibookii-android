package com.bookiibookii.bookiibookii.data.model.recommendation

import com.google.gson.annotations.SerializedName

data class RecommendedBookmateItem(
    @SerializedName("userId") val userId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("matchedTags") val matchedTags: List<String>?,
    @SerializedName("recentBookTitle") val recentBookTitle: String?
)
