package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupListResponse(
    @SerializedName("groupList")
    val groupList: List<GroupItem>?,
    val currentPage: Int,
    val hasNext: Boolean
)
