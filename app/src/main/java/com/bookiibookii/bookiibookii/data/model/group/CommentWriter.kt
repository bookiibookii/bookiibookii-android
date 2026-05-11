package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class CommentWriter(
    val userId: Long,
    val name: String,
    @SerializedName("profileImageUrl")
    val profileImage: String?,
    val role: String
)
