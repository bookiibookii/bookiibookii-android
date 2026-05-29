package com.bookiibookii.bookiibookii.tracker.model

enum class TrackerAction(val label: String) {
    None(""),
    RecordProgress("진행률 기록"),
    WriteReadingCard("독서카드 작성"),
    WriteBookReview("책 후기 작성"),
    CheckDeliveryInfo("배송 정보 확인"),
    RegisterTrackingNumber("운송장 등록"),
    RegisterMeeting("약속 등록"),
    GoToComments("댓글 바로가기"),
    CheckMeeting("약속 확인"),
    ConfirmExchange("교환 확인"),
}

// displayStatus → (primary, secondary)
fun actionsForStatus(displayStatus: String?): Pair<TrackerAction, TrackerAction> = when (displayStatus) {
    "READING" -> TrackerAction.RecordProgress to TrackerAction.WriteReadingCard
    "REVIEW_WRITING",
    "EXCHANGE_REVIEW_WRITING" -> TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard
    "TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    "MEETING_REQUIRED" -> TrackerAction.RegisterMeeting to TrackerAction.GoToComments
    "EXCHANGING" -> TrackerAction.ConfirmExchange to TrackerAction.CheckMeeting
    else -> TrackerAction.None to TrackerAction.None
}
