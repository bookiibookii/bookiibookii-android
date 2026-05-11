package com.bookiibookii.bookiibookii.trkDirectHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerMeetingRequest
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerMeetingResponse
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DirectHostViewModel : ViewModel() {

    private val _trackerState =
        MutableStateFlow<UiState<TrackerDetailResponse>>(UiState.Idle)
    val trackerState: StateFlow<UiState<TrackerDetailResponse>> =
        _trackerState.asStateFlow()

    private val _event = Channel<DirectHostEvent>(Channel.BUFFERED)
    val event: Flow<DirectHostEvent> = _event.receiveAsFlow()

    private val _extensionApplied = MutableStateFlow(false)
    val extensionApplied: StateFlow<Boolean> = _extensionApplied.asStateFlow()

    private val _meetingState =
        MutableStateFlow<UiState<TrackerMeetingResponse>>(UiState.Idle)
    val meetingState: StateFlow<UiState<TrackerMeetingResponse>> =
        _meetingState.asStateFlow()

    fun markExtensionApplied() { _extensionApplied.value = true }
    fun resetExtensionApplied() { _extensionApplied.value = false }

    private val _tradeStepList = MutableStateFlow<List<TradeStatusItem>>(emptyList())
    val tradeStepList: StateFlow<List<TradeStatusItem>> = _tradeStepList.asStateFlow()

    private fun updateStepsByTrackerStatus(status: String?) {
        val steps = buildSteps(status)

        _tradeStepList.value = steps.map { s ->
            TradeStatusItem(
                id = s.id.name,
                title = s.title,
                description = s.desc.orEmpty(),
                badge = when (s.badgeState) {
                    StepBadgeState.DONE -> "완료"
                    StepBadgeState.PLANNED -> "예정"
                }
            )
        }
    }

    fun loadTracker(groupId: Long) {
        viewModelScope.launch {
            _trackerState.value = UiState.Loading
            try {
                val res = RetrofitClient.trkApi().getTrackerDetail(groupId)

                if (res.isSuccess && res.result != null) {
                    _trackerState.value = UiState.Success(res.result)
                    updateStepsByTrackerStatus(res.result.trackerStatus)
                } else {
                    _trackerState.value = UiState.Error(res.message ?: "API 실패")
                }
            } catch (e: Exception) {
                _trackerState.value = UiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun startReading(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.trkApi().patchTrackerReadingStart(groupId)
                if (res.isSuccess) {
                    _event.send(DirectHostEvent.ReadingStartSuccess)
                    loadTracker(groupId)
                } else {
                    _event.send(DirectHostEvent.ReadingStartFail(res.message ?: "reading 시작 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectHostEvent.ReadingStartFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun doneTracker(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.trkApi().patchTrackerDone(groupId)
                if (res.isSuccess) {
                    _event.send(DirectHostEvent.DoneSuccess)
                    loadTracker(groupId)
                } else {
                    _event.send(DirectHostEvent.DoneFail(res.message ?: "독서 완료 처리 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectHostEvent.DoneFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun extendPeriod(groupId: Long, days: Int) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.trkApi().patchTrackerExtension(groupId, days)
                if (res.isSuccess) {
                    _extensionApplied.value = true
                    _event.send(DirectHostEvent.ExtensionSuccess)
                    loadTracker(groupId)
                } else {
                    _event.send(DirectHostEvent.ExtensionFail(res.message ?: "독서 기간 연장 실패"))
                }
            } catch (e: Exception) {
                _event.send(DirectHostEvent.ExtensionFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun makeMeeting(groupId: Long, date: String, place: String) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.trkApi().makeMeeting(
                    groupId = groupId,
                    request = TrackerMeetingRequest(meetingTime = date, meetingPlace = place)
                )

                if (res.isSuccessful) _event.send(DirectHostEvent.MeetingSuccess)
                else _event.send(DirectHostEvent.MeetingFail("약속 등록 실패"))
            } catch (e: Exception) {
                _event.send(DirectHostEvent.MeetingFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    fun loadMeeting(groupId: Long) {
        viewModelScope.launch {
            _meetingState.value = UiState.Loading
            try {
                val res = RetrofitClient.trkApi().getTrackerMeeting(groupId)
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

    fun completeMeeting(groupId: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.trkApi().patchMeetingComplete(groupId)
                if (res.isSuccessful) _event.send(DirectHostEvent.ExchangeCompleteSuccess)
                else _event.send(DirectHostEvent.ExchangeCompleteFail("교환 완료 처리 실패"))
            } catch (e: Exception) {
                _event.send(DirectHostEvent.ExchangeCompleteFail(e.message ?: "네트워크 오류"))
            }
        }
    }

    private val steps = listOf(
        StepUiModel(StepId.GUEST_RETURN_SHIP, "책을 읽고 있어요", "독서카드를 작성하면 교환독서가 더 즐거워져요!", StepBadgeState.PLANNED),
        StepUiModel(StepId.GUEST_SET_APPOINTMENT, "게스트와 만날 약속을 정해요", "게스트와 협의 후 약속을 정해주세요.", StepBadgeState.PLANNED),
        StepUiModel(StepId.GUEST_READ, "게스트에게 책을 전달해주세요", "약속 장소에서 게스트를 만나 책을 전달해주세요.", StepBadgeState.PLANNED),
        StepUiModel(StepId.HOST_EXCHANGE_HANDOVER, "게스트가 책을 읽고 있어요", "게스트의 독서 카드를 확인해볼까요?", StepBadgeState.PLANNED),
        StepUiModel(StepId.HOST_SET_APPOINTMENT, "게스트와 만날 약속을 정해요", "게스트와 협의 후 약속을 등록해주세요.", StepBadgeState.PLANNED),
        StepUiModel(StepId.HOST_READ, "게스트에게 책을 돌려받아요", "약속 장소에서 게스트를 만나 책을 받으세요.", StepBadgeState.PLANNED),
        StepUiModel(StepId.FINISH, "교환독서가 종료되었어요!", "책과 파트너에 대한 후기를 남겨주세요.", StepBadgeState.PLANNED)
    )

    private data class Progress(val visibleCount: Int, val doneCount: Int)

    private fun progressByStatus(status: String?): Progress = when (status) {
        "READY" -> Progress(1, 0)

        "HOST_READING", "HOST_EXTENSION" -> Progress(1, 0)
        "HOST_DONE" -> Progress(2, 1)

        "SHIPPING_TO_GUEST" -> Progress(3, 2)
        "RECEIVED" -> Progress(4, 3)

        "GUEST_READING", "GUEST_EXTENSION" -> Progress(4, 3)
        "GUEST_DONE" -> Progress(5, 4)

        "SHIPPING_TO_HOST" -> Progress(6, 5)
        "RETURNED", "COMPLETED" -> Progress(6, 6)

        else -> Progress(1, 0)
    }

    fun buildSteps(status: String?): List<StepUiModel> {
        val p = progressByStatus(status)

        val visible = steps
            .take(p.visibleCount.coerceAtMost(steps.size))
            .mapIndexed { idx, item ->
                item.copy(badgeState = if (idx < p.doneCount) StepBadgeState.DONE else StepBadgeState.PLANNED)
            }

        return visible.reversed()
    }
}
