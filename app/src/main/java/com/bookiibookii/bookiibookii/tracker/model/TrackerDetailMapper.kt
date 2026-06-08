package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerStepDTO
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus

fun TrackerDetailResDTO.toUiState(): TrackerDetailUiState {
    val dDayChip = "D-${(dDay ?: 0).coerceAtLeast(0)}"
    val safeSteps = steps.orEmpty()
    val currentStepStatus = safeSteps.firstOrNull { it.completed != true }?.status
    val (primary, secondary) = actionsForStatus(displayStatus)
    val (mineIsOwner, partnerIsOwner) = ownerBookBadges(myBook, partnerBook)
    return TrackerDetailUiState(
        groupName = groupName.orEmpty(),
        dDay = dDayChip,
        dDayCount = dDay,
        statusLabel = displayStatusText.orEmpty(),
        currentStepLabel = currentStepStatus.toPhaseLabel(),
        currentStepLabelStyle = currentStepStatus.toPhaseStyle(),
        currentStepPosition = currentStepStatus.toPhasePosition(),
        myProfile = myBook.toProfile().copy(isOwnerBook = mineIsOwner),
        partnerProfile = partnerBook.toProfile().copy(isOwnerBook = partnerIsOwner),
        exchangeLabel = tradeType.toExchangeLabel(),
        primaryAction = primary,
        secondaryAction = secondary,
        isHost = myRole == "HOST",
        steps = safeSteps.toUiSteps(dDayChip),
    )
}

// tradeType → 교환 방식 라벨
private fun String?.toExchangeLabel(): String = when (this) {
    "DIRECT" -> "직접 교환"
    "DELIVERY" -> "택배 교환"
    else -> ""
}

// 8개 step.status → 4개 phase 라벨
private fun String?.toPhaseLabel(): String = when (this) {
    "MY_BOOK_READING", "MY_BOOK_REVIEWING" -> "내 책 읽기"
    "EXCHANGING", "EXCHANGED" -> "교환"
    "PARTNER_BOOK_READING", "PARTNER_BOOK_REVIEWING" -> "파트너 책 읽기"
    "RETURNING", "RETURNED", "COMPLETED", null -> "반납"
    else -> ""
}

// 내 책 읽기 / 교환 → Main, 파트너 책 읽기 / 반납 → Sub
private fun String?.toPhaseStyle(): TrackerStepLabelStyle = when (this) {
    "MY_BOOK_READING", "MY_BOOK_REVIEWING",
    "EXCHANGING", "EXCHANGED" -> TrackerStepLabelStyle.Main
    else -> TrackerStepLabelStyle.Sub
}

// 4단계 phase 중 현재 위치 (1: 내 책 읽기, 2: 교환, 3: 파트너 책 읽기, 4: 반납)
private fun String?.toPhasePosition(): Int = when (this) {
    "MY_BOOK_READING", "MY_BOOK_REVIEWING" -> 1
    "EXCHANGING", "EXCHANGED" -> 2
    "PARTNER_BOOK_READING", "PARTNER_BOOK_REVIEWING" -> 3
    else -> 4 // RETURNING, COMPLETED, null
}

// completed=true는 보여주고 나머지 false는 숨김
// 화면에는 최신 단계가 위로 오도록 역순 표시
private fun List<TrackerStepDTO>.toUiSteps(dDayChipText: String): List<TrackerStep> {
    val firstPendingIdx = indexOfFirst { it.completed != true }
    return mapIndexedNotNull { index, step ->
        when {
            step.completed == true -> TrackerStep(
                title = step.title.orEmpty(),
                description = step.description.orEmpty(),
                status = TrackerStepStatus.Completed,
            )
            index == firstPendingIdx -> TrackerStep(
                title = step.title.orEmpty(),
                description = step.description.orEmpty(),
                status = TrackerStepStatus.InProgress(chipText = dDayChipText),
            )
            else -> null
        }
    }.reversed()
}
