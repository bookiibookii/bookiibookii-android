package com.bookiibookii.bookiibookii.data.model

// 공지사항 리스트 조회 응답
data class NoticeListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<NoticeSummary>
)

data class NoticeSummary(
    val id: Int,
    val createdAt: String, // "2026-02-03T..."
    val title: String,
    val summary: String
)

// 공지사항 상세 조회 응답
data class NoticeDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NoticeDetail
)

data class NoticeDetail(
    val id: Int,
    val title: String,
    val content: String,
    val image: String?, // 이미지 URL (없을 수도 있음)
    val createdAt: String
)

// 문의하기 리스트 조회 응답
data class InquiryListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<InquirySummary>
)

data class InquirySummary(
    val inquiryId: Int,
    val nickname: String,
    val createdAt: String,     // 질문 작성일
    val title: String,
    val content: String,
    val supportStatus: String, // "PENDING" or "RESOLVED" (예상)
    val adminReply: String?,   // 답변 내용 (없으면 null일 수 있음)
    val resolvedAt: String?    // 답변 작성일
)

// 문의하기 작성 요청
data class InquiryRequest(
    val title: String,
    val content: String
)

// 문의하기 작성 응답 (성공 시 result가 문자열)
data class InquiryCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)

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
    val groupName: String,
    val targetMember: String, // API 스펙상 필요할 것으로 추정 (XML 입력 필드 존재)
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
    val name: String
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