package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// GET /api/groups/{groupId}/applylist 응답 result = ApplicationListDTO
data class GroupAppListResponse(
    @SerializedName("applicationList") val applicationList: List<GroupAppItem> = emptyList(),
    @SerializedName("totalCount") val totalCount: Int = 0,
)

// ApplicationDetailDTO
data class GroupAppItem(
    @SerializedName("applicationId") val applicationId: Long?,
    @SerializedName("user") val user: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?, // null=프로필 미등록
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("applyMsg") val applyMsg: String?,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("bookAuthor") val bookAuthor: String?,
    @SerializedName("bookImage") val bookImage: String?,
)
