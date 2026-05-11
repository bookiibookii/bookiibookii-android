package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupSearchResponse(
    @SerializedName("groupList")
    val groupList: List<GroupItem>,
    val totalCount: Int,
    val currentPage: Int,
    val hasNext: Boolean
)
