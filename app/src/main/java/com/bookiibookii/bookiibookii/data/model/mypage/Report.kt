package com.bookiibookii.bookiibookii.data.model.mypage

data class ReportListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ReportSummary>
)

data class ReportSummary(
    val reportId: Int,
    val reporterNickname: String,
    val groupName: String,
    val createdAt: String,     // 신고일
    val reportType: String,    // "ABUSE", "SPAM" 등
    val content: String,
    val supportStatus: String, // "PENDING", "RESOLVED"
    val adminReply: String?,
    val resolvedAt: String?
)

// 신고 작성 요청
data class ReportRequest(
    val groupId: Int,
    val targetId: Int, // API 스펙상 필요할 것으로 추정 (XML 입력 필드 존재)
    val reportType: String,   // "ABUSE", "SPAM" ...
    val content: String
)

// 신고 작성 응답
data class ReportCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)

// 내 그룹 조회 응답
data class MyGroupResponse(
    val isSuccess: Boolean,
    val result: List<GroupSummary>
)

data class GroupSummary(
    val groupId: Int,
    val groupName: String,       // 👈 name 대신 groupName으로 변경!
    val groupHostNickname: String,
    val isHost: Boolean
)

// 그룹 멤버 조회 응답
data class GroupMemberResponse(
    val isSuccess: Boolean,
    val result: List<MemberSummary>
)

data class MemberSummary(
    val userId: Int,
    val nickname: String // 멤버 닉네임
)

// 닉네임 중복 확인 요청/응답
data class NicknameCheckRequest(
    val nickname: String
)

data class NicknameCheckResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Map<String, Boolean>?
)
