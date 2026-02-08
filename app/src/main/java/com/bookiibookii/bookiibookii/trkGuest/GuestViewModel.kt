package com.bookiibookii.bookiibookii.trkGuest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkHost.Phase
import com.bookiibookii.bookiibookii.trkHost.Role
import com.bookiibookii.bookiibookii.trkHost.StepId
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GuestTrackerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: TrackerDetailResponseDto? = null
)

class GuestViewModel : ViewModel() {

    private val role: Role = Role.GUEST

    private val _uiState = MutableStateFlow(GuestTrackerUiState())
    val uiState: StateFlow<GuestTrackerUiState> = _uiState.asStateFlow()

    private val _phase = MutableStateFlow(Phase.INIT)
    val phase: StateFlow<Phase> = _phase.asStateFlow()

    private val _steps = MutableStateFlow<List<TradeStatusItem>>(emptyList())
    val steps: StateFlow<List<TradeStatusItem>> = _steps.asStateFlow()

    init {
        recomputeSteps()
    }

    fun loadTracker(groupId: Long) {
        viewModelScope.launch {
            _uiState.value = GuestTrackerUiState(isLoading = true)

            try {
                val body = RetrofitClient.api().getTrackerDetail(groupId)

                if (!body.isSuccess || body.result == null) {
                    _uiState.value = GuestTrackerUiState(
                        isLoading = false,
                        errorMessage = body.message ?: "API error"
                    )
                    return@launch
                }

                val dto = body.result

                _uiState.value = GuestTrackerUiState(
                    isLoading = false,
                    data = dto
                )

                setPhaseFromApiStatus(dto.trackerStatus)

            } catch (e: Exception) {
                _uiState.value = GuestTrackerUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "network error"
                )
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

            "HOST_READING", "READING" ->
                Phase.HOST_READING

            "HOST_DONE" ->
                Phase.HOST_SHIPPING_READY

            "SHIPPING_TO_GUEST" ->
                Phase.HOST_SHIPPED

            "RECEIVED",
            "GUEST_READING",
            "GUEST_EXTENSION" ->
                Phase.GUEST_READING

            "GUEST_DONE" ->
                Phase.GUEST_SHIPPING_READY

            "SHIPPING_TO_HOST" ->
                Phase.GUEST_SHIPPED

            "COMPLETED", "RETURNED" ->
                Phase.FINISHED

            else -> Phase.INIT
        }
    }


    fun onAction(action: GuestAction) {
        _phase.value = nextPhase(_phase.value, action)
        recomputeSteps()
    }

    private fun nextPhase(current: Phase, action: GuestAction): Phase {
        return when (current) {
            Phase.INIT -> current
            Phase.HOST_READING -> current
            Phase.HOST_SHIPPING_READY -> current

            Phase.HOST_SHIPPED -> when (action) {
                GuestAction.SET_RECEIVE_REGISTER -> Phase.GUEST_READING
                else -> current
            }

            Phase.GUEST_READING -> when (action) {
                GuestAction.SET_GUEST_SHIPPING_READY -> Phase.GUEST_SHIPPING_READY
                else -> current
            }

            Phase.GUEST_SHIPPING_READY -> when (action) {
                GuestAction.SET_GUEST_SHIPPED -> Phase.GUEST_SHIPPED
                else -> current
            }

            Phase.GUEST_SHIPPED -> when (action) {
                GuestAction.SET_FINISHED -> Phase.FINISHED
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
            title = "호스트가 책을 읽고 있어요",
            description = "호스트의 독서 카드를 확인해볼까요?",
            badge = doneIf(phase.ordinal > Phase.HOST_READING.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.HOST_SHIP,
            title = "호스트가 책을 발송해요",
            description = "호스트가 곧 책을 발송할 예정이에요.",
            badge = doneIf(phase.ordinal >= Phase.HOST_SHIPPED.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.RECEIVE_REGISTER,
            title = "수령 인증을 등록해주세요",
            description = "책을 받으면 수령 인증을 등록해주세요.",
            badge = doneIf(phase.ordinal >= Phase.GUEST_READING.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.GUEST_READING,
            title = "책을 읽고 있어요",
            description = "독서카드를 작성하면 교환독서가 더 즐거워져요!",
            badge = doneIf(phase.ordinal > Phase.GUEST_READING.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.GUEST_SHIP,
            title = "호스트에게 책을 반환해요",
            description = "책이 파손되지 않도록 꼼꼼하게 포장해주세요.",
            badge = doneIf(phase.ordinal >= Phase.GUEST_SHIPPED.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.RECEIVE_CHECK,
            title = "수령 인증을 확인해주세요",
            description = "호스트가 책을 잘 받았는지 확인해주세요.",
            badge = doneIf(phase.ordinal >= Phase.FINISHED.ordinal)
        )

        list += TradeStatusItem(
            id = StepId.FINISH_HEADER,
            title = "교환독서가 종료되었어요!",
            description = "책과 파트너에 대한 후기를 남겨주세요.",
            badge = doneIf(phase == Phase.FINISHED)
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

    private fun doneIf(condition: Boolean): String = if (condition) "완료" else "예정"
}

enum class GuestAction {
    SET_RECEIVE_REGISTER,
    SET_GUEST_SHIPPING_READY,
    SET_GUEST_SHIPPED,
    SET_FINISHED
}
