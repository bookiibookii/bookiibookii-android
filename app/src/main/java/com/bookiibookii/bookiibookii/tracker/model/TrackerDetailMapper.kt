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
    return TrackerDetailUiState(
        groupName = groupName.orEmpty(),
        dDay = dDayChip,
        statusLabel = displayStatusText.orEmpty(),
        currentStepLabel = currentStepStatus.toPhaseLabel(),
        currentStepLabelStyle = currentStepStatus.toPhaseStyle(),
        myProfile = myBook.toProfile(),
        partnerProfile = partnerBook.toProfile(),
        primaryAction = primary,
        secondaryAction = secondary,
        steps = safeSteps.toUiSteps(dDayChip),
    )
}

// 8개 step.status → 4개 phase 라벨
private fun String?.toPhaseLabel(): String = when (this) {
    "MY_BOOK_READING", "MY_BOOK_REVIEWING" -> "내 책 읽기"
    "EXCHANGING", "EXCHANGED" -> "교환"
    "PARTNER_BOOK_READING", "PARTNER_BOOK_REVIEWING" -> "파트너 책 읽기"
    "RETURNING", "COMPLETED", null -> "반납"
    else -> ""
}

// 내 책 읽기 / 교환 → Main, 파트너 책 읽기 / 반납 → Sub
private fun String?.toPhaseStyle(): TrackerStepLabelStyle = when (this) {
    "MY_BOOK_READING", "MY_BOOK_REVIEWING",
    "EXCHANGING", "EXCHANGED" -> TrackerStepLabelStyle.Main
    else -> TrackerStepLabelStyle.Sub
}

// completed=true는 보여주고 나머지 false는 숨김
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
    }
}
