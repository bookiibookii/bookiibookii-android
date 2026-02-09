package com.bookiibookii.bookiibookii.home.notification.model

enum class NotificationType {
    // ✅ DB에서 확인된 SYSTEM 타입
    GROUP_JOIN_REQUEST,              // 참여 요청
    GROUP_MATCH_SUCCESS,             // 매칭 완료

    TRACKER_READING_STARTED,         // 독서 시작
    TRACKER_PERIOD_EXTENDED,         // 기간 연장
    TRACKER_READING_FINISHED,        // 독서 완료
    TRACKER_SHIPMENT_REGISTERED,     // 발송 등록
    TRACKER_DELIVERY_CONFIRMED,      // 수령 완료
    TRACKER_RETURN_SHIPMENT_REGISTERED, // 반납 등록
    TRACKER_EXCHANGE_COMPLETED,      // 교환 종료

    // TODO: 스웨거/백엔드 확정 후 추가할 가능성 (스샷 기반)
    INQUIRY_ANSWERED,        // 문의 답변
    REPORT_RESULT,           // 신고 결과
    NOTICE_CREATED,          // 공지
    COMMENT_CREATED,         // 댓글
    GROUP_APPLY_ACCEPTED,    // 그룹 신청 승인
    GROUP_APPLY_REJECTED,    // 그룹 신청 거절

    UNKNOWN;

    companion object {
        fun from(value: String?): NotificationType {
            return values().firstOrNull { it.name == value } ?: UNKNOWN
        }
    }
}