package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerStepDTO
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus

fun TrackerDetailResDTO.toUiState(): TrackerDetailUiState {
    val dDayChip = "D-${(dDay ?: 0).coerceAtLeast(0)}"
    return TrackerDetailUiState(
        groupName = groupName.orEmpty(),
        dDay = dDayChip,
        statusLabel = displayStatusText.orEmpty(),
        myProfile = myBook.toProfile(),
        partnerProfile = partnerBook.toProfile(),
        steps = steps.orEmpty().toUiSteps(dDayChip),
    )
}

// completed=true → Completed, 첫 false → InProgress(dDay chip), 나머지 false → 숨김
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
