package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDoneResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerExtensionResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReadingStartResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HostTrackerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: TrackerDetailResponseDto? = null
)

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class HostViewModel : ViewModel() {

    private val role: Role = Role.HOST

    private val _uiState = MutableStateFlow(HostTrackerUiState())
    val uiState: StateFlow<HostTrackerUiState> = _uiState.asStateFlow()

    private val _phase = MutableStateFlow(Phase.INIT)
    val phase: StateFlow<Phase> = _phase.asStateFlow()

    private val _steps = MutableStateFlow<List<TradeStatusItem>>(emptyList())
    val steps: StateFlow<List<TradeStatusItem>> = _steps.asStateFlow()

    private val _readingStartState =
        MutableStateFlow<UiState<TrackerReadingStartResponseDto>>(UiState.Idle)
    val readingStartState: StateFlow<UiState<TrackerReadingStartResponseDto>> =
        _readingStartState.asStateFlow()

    private val _extensionState =
        MutableStateFlow<UiState<TrackerExtensionResponseDto>>(UiState.Idle)
    val extensionState: StateFlow<UiState<TrackerExtensionResponseDto>> = _extensionState.asStateFlow()

    private val _doneState =
        MutableStateFlow<UiState<TrackerDoneResponseDto>>(UiState.Idle)
    val doneState: StateFlow<UiState<TrackerDoneResponseDto>> = _doneState.asStateFlow()

    init {
        recomputeSteps()
    }

    fun resetDoneState() {
        _doneState.value = UiState.Idle
    }

    fun resetReadingStartState() {
        _readingStartState.value = UiState.Idle
    }

    fun resetExtensionState() {
        _extensionState.value = UiState.Idle
    }

    fun loadTracker(groupId: Long) {
        viewModelScope.launch {
            _uiState.value = HostTrackerUiState(isLoading = true)

            try {
                val body = RetrofitClient.api().getTrackerDetail(groupId)

                if (!body.isSuccess || body.result == null) {
                    _uiState.value = HostTrackerUiState(
                        isLoading = false,
                        errorMessage = body.message ?: "API error"
                    )
                    return@launch
                }

                val dto = body.result

                _uiState.value = HostTrackerUiState(
                    isLoading = false,
                    data = dto
                )

                setPhaseFromApiStatus(dto.trackerStatus)

            } catch (e: Exception) {
                _uiState.value = HostTrackerUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "network error"
                )
            }
        }
    }

    fun patchTrackerReadingStart(groupId: Long) {
        viewModelScope.launch {
            _readingStartState.value = UiState.Loading

            try {
                val body = RetrofitClient.api().patchTrackerReadingStart(groupId)

                if (!body.isSuccess || body.result == null) {
                    _readingStartState.value = UiState.Error(body.message ?: "API error")
                    return@launch
                }

                val dto = body.result
                _readingStartState.value = UiState.Success(dto)

                loadTracker(groupId)

            } catch (e: Exception) {
                _readingStartState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun patchTrackerExtension(groupId: Long, days: Int) {
        viewModelScope.launch {
            _extensionState.value = UiState.Loading

            try {
                val body = RetrofitClient.api().patchTrackerExtension(groupId, days)

                if (!body.isSuccess || body.result == null) {
                    _extensionState.value = UiState.Error(body.message ?: "API error")
                    return@launch
                }

                val dto = body.result
                _extensionState.value = UiState.Success(dto)

                loadTracker(groupId)

            } catch (e: Exception) {
                _extensionState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun patchTrackerDone(groupId: Long) {
        viewModelScope.launch {
            _doneState.value = UiState.Loading

            try {
                val body = RetrofitClient.api().patchTrackerDone(groupId)

                if (!body.isSuccess || body.result == null) {
                    _doneState.value = UiState.Error(body.message ?: "API error")
                    return@launch
                }

                val dto = body.result
                _doneState.value = UiState.Success(dto)

                loadTracker(groupId)

            } catch (e: Exception) {
                _doneState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun setPhaseFromApiStatus(trackerStatus: String?) {
        _phase.value = phaseFromServerStatus(trackerStatus)
        recomputeSteps()
    }

    private fun phaseFromServerStatus(status: String?): Phase {
        return when (status?.uppercase()) {
            "READY" -> Phase.INIT

            "HOST_READING", "HOST_EXTENSION" -> Phase.HOST_READING

            "HOST_DONE" -> Phase.HOST_SHIPPING_READY
            "SHIPPING_TO_GUEST" -> Phase.HOST_SHIPPED

            "RECEIVED", "GUEST_READING", "GUEST_EXTENSION" -> Phase.GUEST_READING
            "GUEST_DONE" -> Phase.GUEST_SHIPPING_READY
            "SHIPPING_TO_HOST" -> Phase.GUEST_SHIPPED

            "COMPLETED", "RETURNED" -> Phase.FINISHED
            else -> Phase.INIT
        }
    }

    fun onAction(action: HostAction) {
        _phase.value = nextPhase(_phase.value, action)
        recomputeSteps()
    }

    private fun nextPhase(current: Phase, action: HostAction): Phase {
        return when (current) {
            Phase.INIT -> when (action) {
                HostAction.SET_HOST_READING -> Phase.HOST_READING
                else -> current
            }

            Phase.HOST_READING -> when (action) {
                HostAction.SET_HOST_SHIPPING_READY -> Phase.HOST_SHIPPING_READY
                else -> current
            }

            Phase.HOST_SHIPPING_READY -> when (action) {
                HostAction.SET_HOST_SHIPPED -> Phase.HOST_SHIPPED
                else -> current
            }

            Phase.HOST_SHIPPED -> when (action) {
                HostAction.SET_GUEST_READING -> Phase.GUEST_READING
                else -> current
            }

            Phase.GUEST_READING -> when (action) {
                HostAction.SET_GUEST_SHIPPING_READY -> Phase.GUEST_SHIPPING_READY
                else -> current
            }

            Phase.GUEST_SHIPPING_READY -> when (action) {
                HostAction.SET_GUEST_SHIPPED -> Phase.GUEST_SHIPPED
                else -> current
            }

            Phase.GUEST_SHIPPED -> when (action) {
                HostAction.SET_FINISHED -> Phase.FINISHED
                else -> current
            }

            Phase.FINISHED -> current
        }
    }

    private fun recomputeSteps() {
        _steps.value = buildSteps(role, _phase.value)
    }

    private fun buildSteps(role: Role, phase: Phase): List<TradeStatusItem> {
        val list = mutableListOf<TradeStatusItem>()

        list += TradeStatusItem(
            id = StepId.HOST_READING,
            title = "책을 읽고 있어요",
            description = hostReadingDescription(phase),
            badge = hostReadingBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.HOST_SHIP,
            title = "게스트에게 책을 발송해요",
            description = "책이 파손되지 않도록 꼼꼼하게 포장해주세요.",
            badge = hostShipBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.RECEIVE_CHECK,
            title = "수령 인증을 확인해주세요",
            description = "게스트가 책을 잘 받았는지 확인해주세요.",
            badge = receiveCheckBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.GUEST_READING,
            title = "게스트가 책을 읽고 있어요",
            description = "게스트의 독서 카드를 확인해볼까요?",
            badge = guestReadingBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.GUEST_SHIP,
            title = "게스트가 책을 발송해요",
            description = "게스트가 곧 책을 발송할 예정이에요.",
            badge = guestShipBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.RECEIVE_REGISTER,
            title = "수령 인증을 등록해주세요",
            description = "책을 받으면 수령 인증을 등록해주세요.",
            badge = receiveRegisterBadge(phase)
        )

        list += TradeStatusItem(
            id = StepId.FINISH_HEADER,
            title = "교환독서가 종료되었어요!",
            description = "책과 파트너에 대한 후기를 남겨주세요.",
            badge = finishDDayBadge()
        )

        val visibleCount = when (phase) {
            Phase.INIT -> 1
            Phase.HOST_READING -> 1
            Phase.HOST_SHIPPING_READY -> 2
            Phase.HOST_SHIPPED -> 3
            Phase.GUEST_READING -> 4
            Phase.GUEST_SHIPPING_READY -> 5
            Phase.GUEST_SHIPPED -> 6
            Phase.FINISHED -> 7
        }

        return list.take(visibleCount).reversed()
    }

    private fun finishDDayBadge(): String = "D-7"

    private fun hostReadingDescription(phase: Phase): String {
        return if (phase.ordinal < Phase.HOST_READING.ordinal) {
            "아직 독서를 시작하지 않았어요.\n독서를 시작하면 아래 시작하기 버튼을 눌러주세요"
        } else {
            "독서카드를 작성하면 교환독서가 더 즐거워져요!"
        }
    }

    private fun hostReadingBadge(phase: Phase) =
        if (phase.ordinal > Phase.HOST_READING.ordinal) "완료" else "예정"

    private fun hostShipBadge(phase: Phase) =
        if (phase.ordinal >= Phase.HOST_SHIPPED.ordinal) "완료" else "예정"

    private fun receiveCheckBadge(phase: Phase) =
        if (phase.ordinal >= Phase.GUEST_READING.ordinal) "완료" else "예정"

    private fun guestReadingBadge(phase: Phase) =
        if (phase.ordinal > Phase.GUEST_READING.ordinal) "완료" else "예정"

    private fun guestShipBadge(phase: Phase) =
        if (phase.ordinal >= Phase.GUEST_SHIPPED.ordinal) "완료" else "예정"

    private fun receiveRegisterBadge(phase: Phase) =
        if (phase.ordinal >= Phase.FINISHED.ordinal) "완료" else "예정"
}

enum class HostAction {
    SET_GUEST_SHIPPING_READY,
    SET_GUEST_SHIPPED,
    SET_GUEST_READING,
    SET_HOST_SHIPPING_READY,
    SET_HOST_SHIPPED,
    SET_HOST_READING,
    SET_FINISHED
}
