package com.bookiibookii.bookiibookii.trkDirectGuest

enum class StepBadgeState { PLANNED, DONE }

data class StepUiModel(
    val id: StepId,
    val title: String,
    val desc: String?,
    val badgeState: StepBadgeState
)

enum class StepId {
    HOST_READING,
    APPOINTMENT_TO_GUEST,
    HANDOVER_TO_GUEST,
    GUEST_READING,
    APPOINTMENT_TO_HOST,
    RETURN_TO_HOST,
    FINISH
}

