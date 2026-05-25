package com.bookiibookii.bookiibookii.data.model.group

// GET /api/groups/{groupId} 응답
data class GroupDetailResponse(
    // 1. 그룹 및 상태 정보
    val groupId: Long,
    val groupStatus: String,        // RECRUITING, MATCHED
    val isHost: Boolean,            // 조회자가 방장인지 여부
    val tradeType: String,          // DIRECT, DELIVERY
    val placeName: String,
    val address: String,

    // 2. 도서 상세 정보
    val title: String,
    val bookImage: String?,
    val author: String,
    val genre: String,              // CustomCategory 명칭

    // 3. 그룹 설정 및 배지 정보
    val readingPeriod: Int,         // 독서 기간 (day)
    val matchedCount: Int,          // 현재 확정 인원
    val maxCapacity: Int,           // 정원
    val waitingCount: Int,          // 대기자 수
    val isHot: Boolean,             // 대기자가 정원의 3배 이상인지
    val createdAt: String,
    val startDate: String?,         // 미시작 시 null

    // 4. 호스트 정보 및 규칙
    val hostNickname: String,
    val hostProfileImageUrl: String?,   // Presigned GET URL

    // 5. 그룹 소개 및 참여 멤버 슬롯
    val groupComment: String?,      // 그룹 소개글 (생성 시 선택 입력)
    val groupName: String,
    val rules: List<GroupRule>,
    // 정원 4명 중 2명 참여 시 -> [방장, 게스트1, EMPTY, EMPTY] 순서
    val participantSlots: List<ParticipantSlot>,

    // APPLY(신청하기), CANCEL(취소하기), MANAGE(요청관리), TRACKER(트래커보기), FULL(인원마감)
    val buttonStatus: String,
)

// 그룹 규칙 (응답 전용)
data class GroupRule(
    // MEMO / POSTIT / PHOTO / ALL_ROUNDER / NO_IDEA / CUSTOM
    val tag: String,
    val content: String,
)

// 참여 멤버 슬롯
data class ParticipantSlot(
    val nickname: String?,
    val profileImageUrl: String?,
    val role: String,               // HOST, GUEST, EMPTY
    val isMe: Boolean,
)
