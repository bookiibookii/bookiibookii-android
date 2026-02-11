package com.bookiibookii.bookiibookii.data.model

import com.google.gson.annotations.SerializedName

data class RecommendedGroupDto(
    val groupId: Long,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("bookImage") val bookImageUrl: String?
)
