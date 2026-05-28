package com.bookiibookii.bookiibookii.data.model.mypage

data class ReportListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ReportSummary>,
)

data class ReportSummary(
    val reportId: Long,
    val reporterNickname: String,
    val groupName: String,
    val createdAt: String,
    val reportType: String,
    val content: String,
    val supportStatus: String,
    val adminReply: String?,
    val resolvedAt: String?,
)

data class ReportRequest(
    val groupId: Long,
    val targetId: Long,
    val reportType: String,
    val content: String,
)

data class ReportCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String,
)

data class MyGroupResponse(
    val isSuccess: Boolean,
    val result: List<GroupSummary>,
)

data class GroupSummary(
    val groupId: Long,
    val groupName: String,
    val groupHostNickname: String,
    val isHost: Boolean,
)

data class GroupMemberResponse(
    val isSuccess: Boolean,
    val result: List<MemberSummary>,
)

data class MemberSummary(
    val userId: Long,
    val nickname: String,
)
