package com.bookiibookii.bookiibookii.tracker.model

enum class TrackerAction(val label: String) {
    None(""),
    RecordProgress("진행률 기록"),
    WriteReadingCard("독서카드 작성"),
    WriteBookReview("책 후기 작성"),
    CheckDeliveryInfo("배송 정보 확인"),
    RegisterTrackingNumber("운송장 등록"),
}

// displayStatus → (primary, secondary)
fun actionsForStatus(displayStatus: String?): Pair<TrackerAction, TrackerAction> = when (displayStatus) {
    "READING" -> TrackerAction.RecordProgress to TrackerAction.WriteReadingCard
    "REVIEW_WRITING",
    "EXCHANGE_REVIEW_WRITING" -> TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard
    "TRACKING_REQUIRED" -> TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo
    else -> TrackerAction.None to TrackerAction.None
}
