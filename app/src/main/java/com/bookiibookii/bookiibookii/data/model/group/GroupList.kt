package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// GET /api/groups 응답 result = GroupSliceResponseDTO
data class GroupListResponse(
    @SerializedName("groupList") val groupList: List<GroupItem>?,
    @SerializedName("totalCount") val totalCount: Int = 0,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
)
