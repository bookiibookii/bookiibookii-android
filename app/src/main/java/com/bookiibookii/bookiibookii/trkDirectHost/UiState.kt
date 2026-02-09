package com.bookiibookii.bookiibookii.trkDirectHost

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface DirectHostEvent {

    data object ReadingStartSuccess : DirectHostEvent
    data class ReadingStartFail(val message: String) : DirectHostEvent

    data object DoneSuccess : DirectHostEvent
    data class DoneFail(val message: String) : DirectHostEvent

    data object ExtensionSuccess : DirectHostEvent
    data class ExtensionFail(val message: String) : DirectHostEvent

    data object MeetingSuccess : DirectHostEvent
    data class MeetingFail(val message: String) : DirectHostEvent

    data object ExchangeCompleteSuccess : DirectHostEvent
    data class ExchangeCompleteFail(val message: String) : DirectHostEvent
}

