package com.bookiibookii.bookiibookii.trkDirectHost

enum class StepBadgeState { PLANNED, DONE }

data class StepUiModel(
    val id: StepId,
    val title: String,
    val desc: String?,
    val badgeState: StepBadgeState
)

enum class StepId {
    HOST_READ,
    HOST_SET_APPOINTMENT,
    HOST_EXCHANGE_HANDOVER,
    GUEST_READ,
    GUEST_SET_APPOINTMENT,
    GUEST_RETURN_SHIP,
    FINISH
}
