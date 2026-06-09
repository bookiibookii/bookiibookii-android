package com.bookiibookii.bookiibookii.tracker.model

enum class TrackerAction(val label: String) {
    None(""),
    RecordProgress("진행률 기록"),
    WriteReadingCard("독서카드 작성"),
    WriteBookReview("책 후기 작성"),
    EditBookReview("책 후기 수정"),
    CheckDeliveryInfo("배송 정보 확인"),
    CheckShippingInfo("운송장 정보 확인"),
    ConfirmReceive("수령 확인"),
    RegisterTrackingNumber("운송장 등록"),
    RegisterMeeting("약속 등록"),
    GoToComments("메시지로 이동"),
    CheckMeeting("약속 확인"),
    ConfirmExchange("교환 확인"),
    CompleteExchange("교환 완료"),
    WritePartnerReview("교환독서 후기 작성"),
}

// displayStatus → (primary, secondary)
fun actionsForStatus(displayStatus: String?): Pair<TrackerAction, TrackerAction> = when (displayStatus) {
    "READING" -> TrackerAction.RecordProgress to TrackerAction.WriteReadingCard
    "REVIEW_WRITING" -> TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard
    // 파트너 후기 대기
    "REVIEW_WAITING_PARTNER" -> TrackerAction.EditBookReview to TrackerAction.WriteReadingCard
    // 교환독서 후기 작성: 단일 버튼 → 파트너 리뷰 화면
    "EXCHANGE_REVIEW_WRITING" -> TrackerAction.WritePartnerReview to TrackerAction.None
    "TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    // 반납 단계 운송장 등록: TRACKING_REQUIRED와 버튼·동작 동일
    "RETURN_TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    // 배송 중: 운송장 정보 확인(좌) / 수령 확인(우)
    "SHIPPING" -> TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo
    // 반납 배송 중 — SHIPPING과 버튼 동일
    "RETURNING" -> TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo
    // 파트너 운송장 등록 대기 — SHIPPING과 버튼 동일, 둘 다 비활성화
    "WAITING_PARTNER_TRACKING_REGISTER" -> TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo
    // 파트너 수령 확인 대기 — SHIPPING과 버튼 동일, 둘 다 비활성화
    "WAITING_PARTNER_RECEIPT_CONFIRM" -> TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo
    "MEETING_REGISTER_REQUIRED" -> TrackerAction.GoToComments to TrackerAction.RegisterMeeting
    // 호스트의 약속 등록 대기(게스트 화면) — 버튼은 동일, 약속 등록만 비활성화
    "WAITING_HOST_MEETING_REGISTER" -> TrackerAction.GoToComments to TrackerAction.RegisterMeeting
    "EXCHANGING" -> TrackerAction.ConfirmExchange to TrackerAction.CheckMeeting
    // 파트너의 약속 완료 대기 — 단일 "교환 완료" 버튼, 비활성화
    "WAITING_PARTNER_MEETING_COMPLETE" -> TrackerAction.CompleteExchange to TrackerAction.None
    else -> TrackerAction.None to TrackerAction.None
}

// secondary 버튼을 비활성화해야 하는 상태
// - WAITING_HOST_MEETING_REGISTER: 호스트의 약속 등록 대기
// - WAITING_PARTNER_TRACKING_REGISTER: 파트너 운송장 등록 대기(두 버튼 모두 비활성)
// - WAITING_PARTNER_RECEIPT_CONFIRM: 파트너 수령 확인 대기(두 버튼 모두 비활성)
fun isSecondaryActionDisabled(displayStatus: String?): Boolean = displayStatus in setOf(
    "WAITING_HOST_MEETING_REGISTER",
    "WAITING_PARTNER_TRACKING_REGISTER",
    "WAITING_PARTNER_RECEIPT_CONFIRM",
)

// primary 버튼을 비활성화해야 하는 상태
// - WAITING_PARTNER_MEETING_COMPLETE: 파트너의 약속 완료 대기
// - WAITING_PARTNER_TRACKING_REGISTER: 파트너 운송장 등록 대기(두 버튼 모두 비활성)
// - WAITING_PARTNER_RECEIPT_CONFIRM: 파트너 수령 확인 대기(두 버튼 모두 비활성)
fun isPrimaryActionDisabled(displayStatus: String?): Boolean = displayStatus in setOf(
    "WAITING_PARTNER_MEETING_COMPLETE",
    "WAITING_PARTNER_TRACKING_REGISTER",
    "WAITING_PARTNER_RECEIPT_CONFIRM",
)

// 읽기 진행률 바·% 텍스트를 숨겨야 하는 상태 (교환 약속~교환 이후 단계)
private val PROGRESS_HIDDEN_STATUSES = setOf(
    "MEETING_REGISTER_REQUIRED",
    "WAITING_HOST_MEETING_REGISTER",
    "EXCHANGING",
    "WAITING_PARTNER_MEETING_COMPLETE",
    "EXCHANGE_REVIEW_WRITING",
)

fun isReadingProgressHidden(displayStatus: String?): Boolean =
    displayStatus in PROGRESS_HIDDEN_STATUSES
