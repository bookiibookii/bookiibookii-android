package com.bookiibookii.bookiibookii.data.model.group

data class GroupApplyRequest(
    val applyMsg: String
)

data class GroupApplyResponse(
    val applicationId: Long,
    val status: String,
    val createdAt: String
)
