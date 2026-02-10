package com.bookiibookii.bookiibookii.trkDirectGuest

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface DirectGuestEvent {
    data object ReadingStartSuccess : DirectGuestEvent
    data class ReadingStartFail(val message: String) : DirectGuestEvent

    data object DoneSuccess : DirectGuestEvent
    data class DoneFail(val message: String) : DirectGuestEvent

    data object ExtensionSuccess : DirectGuestEvent
    data class ExtensionFail(val message: String) : DirectGuestEvent

    data object MeetingSuccess : DirectGuestEvent
    data class MeetingFail(val message: String) : DirectGuestEvent

    data object ExchangeCompleteSuccess : DirectGuestEvent
    data class ExchangeCompleteFail(val message: String) : DirectGuestEvent
}
