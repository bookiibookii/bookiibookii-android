package com.bookiibookii.bookiibookii.trkDirectGuest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.MakeMeetingRequest
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerMeetingResponseDto
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

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

    private val _tradeStepList = MutableStateFlow<List<TradeStatusItem>>(emptyList())
    val tradeStepList: StateFlow<List<TradeStatusItem>> = _tradeStepList.asStateFlow()

    fun markExtensionApplied() {
        _extensionApplied.value = true
    }

    private val _stepUiList = MutableStateFlow<List<StepUiModel>>(emptyList())
    val stepUiList: StateFlow<List<StepUiModel>> = _stepUiList.asStateFlow()

    private val REMAINING_BADGE_STATUSES = setOf(
        "HOST_READING",
        "HOST_DONE",
        "SHIPPING_TO_GUEST",
        "RECEIVED",
        "GUEST_READING",
        "GUEST_EXTENSION",
        "GUEST_DONE",
        "SHIPPING_TO_HOST",
        "RETURNED"
    )

    private val _currentTrackerStatus = MutableStateFlow<String?>(null)
    private val _remainingDays = MutableStateFlow<Int?>(null)

    fun loadTracker(groupId: Long) {
        viewModelScope.launch {
            _trackerState.value = UiState.Loading
            try {
                val res = RetrofitClient.api().getTrackerDetail(groupId)

                if (res.isSuccess && res.result != null) {
                    val dto = res.result
                    _trackerState.value = UiState.Success(dto)

                    _currentTrackerStatus.value = dto.trackerStatus
                    _remainingDays.value = dto.remainingDays

                    updateStepsByTrackerStatus(dto.trackerStatus)
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
                    loadTracker(groupId)
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
                    loadTracker(groupId)
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
                    // [MOD] 상태 정합성 위해 재조회
                    loadTracker(groupId)
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

    private fun updateStepsByTrackerStatus(status: String?) {
        val steps = buildSteps(status)

        _stepUiList.value = steps

        val base = steps.map { s ->
            TradeStatusItem(
                s.id.name,
                s.title,
                s.desc.orEmpty(),
                when (s.badgeState) {
                    StepBadgeState.DONE -> "완료"
                    StepBadgeState.PLANNED -> "예정"
                }
            )
        }

        _tradeStepList.value = applyRemainingDaysBadgeIfNeeded(base)
    }

    private fun applyRemainingDaysBadgeIfNeeded(list: List<TradeStatusItem>): List<TradeStatusItem> {
        val status = _currentTrackerStatus.value
        if (status !in REMAINING_BADGE_STATUSES) return list

        val remainingDays = _remainingDays.value ?: return list
        if (list.isEmpty()) return list

        val badgeText = formatRemainingDaysBadge(remainingDays)
        val first = list.first().copy(badge = badgeText)
        return listOf(first) + list.drop(1)
    }

    private fun formatRemainingDaysBadge(remainingDays: Int): String {
        return when {
            remainingDays > 0 -> "D-$remainingDays"
            remainingDays == 0 -> "D-day"
            else -> "D+${abs(remainingDays)}"
        }
    }

    private val steps = listOf(
        StepUiModel(
            StepId.HOST_READING,
            "호스트가 책을 읽고 있어요",
            "호스트의 독서 카드를 확인해볼까요?",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.APPOINTMENT_TO_GUEST,
            "호스트와 만날 약속을 정해요",
            "호스트와 협의 후 약속을 정해주세요.",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.HANDOVER_TO_GUEST,
            "호스트에게 책을 받아요",
            "약속 장소에서 호스트를 만나 책을 받으세요.",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.GUEST_READING,
            "책을 읽고 있어요",
            "독서카드를 작성하면 교환독서가 더 즐거워져요!",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.APPOINTMENT_TO_HOST,
            "호스트와 만날 약속을 정해요",
            "호스트와 협의 후 약속을 등록해주세요.",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.RETURN_TO_HOST,
            "호스트에게 책을 반납해주세요",
            "약속 장소에서 호스트를 만나 책을 반납해주세요.",
            StepBadgeState.PLANNED
        ),
        StepUiModel(
            StepId.FINISH,
            "교환독서가 종료되었어요!",
            "책과 파트너에 대한 후기를 남겨주세요.",
            StepBadgeState.PLANNED
        )
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
        "RETURNED" -> Progress(6, 6)

        "COMPLETED" -> Progress(7, 7)

        else -> Progress(1, 0)
    }

    fun buildSteps(status: String?): List<StepUiModel> {
        val p = progressByStatus(status)

        val visible = steps
            .take(p.visibleCount.coerceAtMost(steps.size))
            .mapIndexed { idx, item ->
                item.copy(
                    badgeState = if (idx < p.doneCount) StepBadgeState.DONE else StepBadgeState.PLANNED
                )
            }

        return visible.reversed()
    }
}
