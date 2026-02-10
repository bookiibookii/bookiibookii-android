package com.bookiibookii.bookiibookii.trkDirectGuest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.MakeMeetingRequest
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerMeetingResponseDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DirectGuestViewModel : ViewModel() {

    private val _trackerState =
        MutableStateFlow<UiState<TrackerDetailResponseDto>>(UiState.Idle)
    val trackerState: StateFlow<UiState<TrackerDetailResponseDto>> =
        _trackerState.asStateFlow()

    private val _meetingState =
        MutableStateFlow<UiState<TrackerMeetingResponseDto>>(UiState.Idle)
    val meetingState: StateFlow<UiState<TrackerMeetingResponseDto>> =
        _meetingState.asStateFlow()

    private val _event = Channel<DirectGuestEvent>(Channel.BUFFERED)
    val event: Flow<DirectGuestEvent> = _event.receiveAsFlow()

    private val _extensionApplied = MutableStateFlow(false)
    val extensionApplied: StateFlow<Boolean> = _extensionApplied.asStateFlow()

    fun markExtensionApplied() {
        _extensionApplied.value = true
    }

    fun loadTracker(groupId: Long) {
        viewModelScope.launch {
            _trackerState.value = UiState.Loading
            try {
                val res = RetrofitClient.api().getTrackerDetail(groupId)

                if (res.isSuccess && res.result != null) {
                    _trackerState.value = UiState.Success(res.result)
                } else {
                    _trackerState.value = UiState.Error(res.message ?: "API 실패")
                }
            } catch (e: Exception) {
                _trackerState.value = UiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun loadMeeting(groupId: Long) {
        viewModelScope.launch {
            _meetingState.value = UiState.Loading
            try {
                val res = RetrofitClient.api().getTrackerMeeting(groupId)
                if (res.isSuccess && res.result != null) {
                    _meetingState.value = UiState.Success(res.result)
                } else {
                    _meetingState.value = UiState.Error(res.message ?: "약속 조회 실패")
                }
            } catch (e: Exception) {
                _meetingState.value = UiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun startReading(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api().patchTrackerReadingStart(groupId)
                if (res.isSuccess) {
                    _event.send(DirectGuestEvent.ReadingStartSuccess)
                } else {
                    _event.send(
                        DirectGuestEvent.ReadingStartFail(res.message ?: "reading 시작 실패")
                    )
                }
            } catch (e: Exception) {
                _event.send(
                    DirectGuestEvent.ReadingStartFail(e.message ?: "네트워크 오류")
                )
            }
        }
    }

    fun doneTracker(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api().patchTrackerDone(groupId)
                if (res.isSuccess) {
                    _event.send(DirectGuestEvent.DoneSuccess)
                } else {
                    _event.send(DirectGuestEvent.DoneFail(res.message ?: "독서 완료 처리 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectGuestEvent.DoneFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun extendPeriod(groupId: Long, days: Int) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api().patchTrackerExtension(groupId, days)
                if (res.isSuccess) {
                    _extensionApplied.value = true
                    _event.send(DirectGuestEvent.ExtensionSuccess)
                } else {
                    _event.send(DirectGuestEvent.ExtensionFail(res.message ?: "독서 기간 연장 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectGuestEvent.ExtensionFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun makeMeeting(groupId: Long, date: String, place: String) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api().makeMeeting(
                    groupId = groupId,
                    request = MakeMeetingRequest(
                        meetingTime = date,
                        meetingPlace = place
                    )
                )

                if (res.isSuccessful) {
                    _event.send(DirectGuestEvent.MeetingSuccess)
                } else {
                    _event.send(DirectGuestEvent.MeetingFail("약속 등록 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectGuestEvent.MeetingFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun completeMeeting(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api().patchMeetingComplete(groupId)

                if (res.isSuccessful) {
                    _event.send(DirectGuestEvent.ExchangeCompleteSuccess)
                } else {
                    _event.send(DirectGuestEvent.ExchangeCompleteFail("교환 완료 처리 실패"))
                }
            } catch (e: Exception) {
                _event.send(
                    DirectGuestEvent.ExchangeCompleteFail(e.message ?: "네트워크 오류")
                )
            }
        }
    }
}
