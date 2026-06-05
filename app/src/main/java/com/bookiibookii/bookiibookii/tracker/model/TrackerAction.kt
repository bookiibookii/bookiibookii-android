package com.bookiibookii.bookiibookii.tracker.model

enum class TrackerAction(val label: String) {
    None(""),
    RecordProgress("진행률 기록"),
    WriteReadingCard("독서카드 작성"),
    WriteBookReview("책 후기 작성"),
    CheckDeliveryInfo("배송 정보 확인"),
    CheckShippingInfo("운송장 정보 확인"),
    ConfirmReceive("수령 확인"),
    RegisterTrackingNumber("운송장 등록"),
    RegisterMeeting("약속 등록"),
    GoToComments("댓글 바로가기"),
    CheckMeeting("약속 확인"),
    ConfirmExchange("교환 확인"),
    WritePartnerReview("교환독서 후기 작성"),
}

// displayStatus → (primary, secondary)
fun actionsForStatus(displayStatus: String?): Pair<TrackerAction, TrackerAction> = when (displayStatus) {
    "READING" -> TrackerAction.RecordProgress to TrackerAction.WriteReadingCard
    "REVIEW_WRITING" -> TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard
    // 교환독서 후기 작성: 단일 버튼 → 파트너 리뷰 화면
    "EXCHANGE_REVIEW_WRITING" -> TrackerAction.WritePartnerReview to TrackerAction.None
    "TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    // 반납 단계 운송장 등록: TRACKING_REQUIRED와 버튼·동작 동일
    "RETURN_TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    // 배송 중: 운송장 정보 확인(좌) / 수령 확인(우)
    "SHIPPING" -> TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo
    "MEETING_REQUIRED" -> TrackerAction.GoToComments to TrackerAction.RegisterMeeting
    "EXCHANGING" -> TrackerAction.ConfirmExchange to TrackerAction.CheckMeeting
    else -> TrackerAction.None to TrackerAction.None
}
