package com.bookiibookii.bookiibookii.data.model.recommendation

import com.google.gson.annotations.SerializedName

data class RecommendedGroupItem(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("bookImageUrl") val bookImageUrl: String?
)
