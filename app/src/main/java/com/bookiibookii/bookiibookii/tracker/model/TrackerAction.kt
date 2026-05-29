package com.bookiibookii.bookiibookii.tracker.model

enum class TrackerAction(val label: String) {
    None(""),
    RecordProgress("진행률 기록"),
    WriteReadingCard("독서카드 작성"),
    WriteBookReview("책 후기 작성"),
}

// displayStatus → (primary, secondary)
fun actionsForStatus(displayStatus: String?): Pair<TrackerAction, TrackerAction> = when (displayStatus) {
    "READING" -> TrackerAction.RecordProgress to TrackerAction.WriteReadingCard
    "REVIEW_WRITING",
    "EXCHANGE_REVIEW_WRITING" -> TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard
    else -> TrackerAction.None to TrackerAction.None
}
