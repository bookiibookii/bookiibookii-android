package com.bookiibookii.bookiibookii.data.model.group

data class GroupAppListResponse(
    val applicationList: List<GroupAppItem>,
    val totalCount: Int
)

data class GroupAppItem(
    val applicationId: Long,
    val user: Int,
    val name: String,
    val tags: List<String>?,
    val createdAt: String,
    val applyMsg: String,
    val profileImageUrl: String?
)
