package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.TrackerCheckImageResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDoneResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerExtensionResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReadingStartResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.abs

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

    private val _shippingStartState =
        MutableStateFlow<UiState<TrackerShippingStartResponseDto>>(UiState.Idle)
    val shippingStartState: StateFlow<UiState<TrackerShippingStartResponseDto>> =
        _shippingStartState.asStateFlow()

    private val _receiveState =
        MutableStateFlow<UiState<TrackerReceiveResponseDto>>(UiState.Idle)
    val receiveState: StateFlow<UiState<TrackerReceiveResponseDto>> =
        _receiveState.asStateFlow()

    private val _confirmReceptionState =
        MutableStateFlow<UiState<TrackerDetailResponseDto>>(UiState.Idle)
    val confirmReceptionState: StateFlow<UiState<TrackerDetailResponseDto>> = _confirmReceptionState

    private val _receivedImageState =
        MutableStateFlow<UiState<TrackerCheckImageResponseDto>>(UiState.Idle)
    val receivedImageState: StateFlow<UiState<TrackerCheckImageResponseDto>> =
        _receivedImageState.asStateFlow()

    private companion object {
        const val TAG = "IMG_DEBUG"
    }

    private val REMAINING_BADGE_STATUSES = setOf(
        TrackerStatus.HOST_READING,
        TrackerStatus.HOST_DONE,
        TrackerStatus.RECEIVED,
        TrackerStatus.GUEST_READING,
        TrackerStatus.GUEST_EXTENSION,
        TrackerStatus.GUEST_DONE,
        TrackerStatus.RETURNED
    )

    private val _currentTrackerStatus = MutableStateFlow(TrackerStatus.UNKNOWN)
    private val _remainingDays = MutableStateFlow<Int?>(null)

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

    fun resetShippingStartState() {
        _shippingStartState.value = UiState.Idle
    }

    fun resetReceiveState() {
        _receiveState.value = UiState.Idle
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

                _currentTrackerStatus.value = TrackerStatus.from(dto.trackerStatus)
                _remainingDays.value = dto.remainingDays

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

    fun startShipping(
        groupId: Long,
        deliveryCompany: String,
        trackingNumber: String,
        imageBytes: ByteArray,
        contentType: String
    ) {
        viewModelScope.launch {
            _shippingStartState.value = UiState.Loading

            try {
                val presignedBody = RetrofitClient.api().getTrackerImagePresignedUrl(groupId)
                if (!presignedBody.isSuccess || presignedBody.result == null) {
                    _shippingStartState.value =
                        UiState.Error(presignedBody.message ?: "presigned-url error")
                    return@launch
                }

                val s3Key = presignedBody.result.s3Key
                val putUrl = presignedBody.result.presignedPutUrl

                val uploaded = uploadToPresignedUrl(
                    putUrl = putUrl,
                    bytes = imageBytes,
                    contentType = contentType
                )
                if (!uploaded) {
                    _shippingStartState.value = UiState.Error("이미지 업로드에 실패했습니다.")
                    return@launch
                }

                val req = TrackerShippingStartRequestDto(
                    deliveryCompany = deliveryCompany,
                    trackingNumber = trackingNumber,
                    s3Key = s3Key
                )

                val shipBody = RetrofitClient.api().postTrackerShippingStart(groupId, req)
                if (!shipBody.isSuccess || shipBody.result == null) {
                    _shippingStartState.value =
                        UiState.Error(shipBody.message ?: "shipping start error")
                    return@launch
                }

                _shippingStartState.value = UiState.Success(shipBody.result)

                loadTracker(groupId)

            } catch (e: Exception) {
                _shippingStartState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }


    fun patchTrackerReceiveWithImage(
        groupId: Long,
        imageBytes: ByteArray,
        contentType: String
    ) {
        viewModelScope.launch {
            _receiveState.value = UiState.Loading

            try {
                val presignedBody = RetrofitClient.api()
                    .getTrackerImagePresignedUrl(groupId)

                if (!presignedBody.isSuccess || presignedBody.result == null) {
                    _receiveState.value =
                        UiState.Error(presignedBody.message ?: "presigned-url error")
                    return@launch
                }

                val s3Key = presignedBody.result.s3Key
                val putUrl = presignedBody.result.presignedPutUrl

                val uploaded = uploadToPresignedUrl(
                    putUrl = putUrl,
                    bytes = imageBytes,
                    contentType = contentType
                )
                if (!uploaded) {
                    _receiveState.value = UiState.Error("이미지 업로드 실패")
                    return@launch
                }

                val body = RetrofitClient.api().patchTrackerReceive(
                    groupId = groupId,
                    request = TrackerReceiveRequestDto(s3Key)
                )

                if (!body.isSuccess || body.result == null) {
                    _receiveState.value =
                        UiState.Error(body.message ?: "receive error")
                    return@launch
                }

                _receiveState.value = UiState.Success(body.result)

                loadTracker(groupId) // HostViewModel에 있는 트래커 재조회 함수

            } catch (e: Exception) {
                _receiveState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun patchConfirmReception(groupId: Long) {
        viewModelScope.launch {
            _confirmReceptionState.value = UiState.Loading

            try {
                val body = RetrofitClient.api().patchConfirmReception(groupId)

                if (!body.isSuccess || body.result == null) {
                    _confirmReceptionState.value =
                        UiState.Error(body.message ?: "confirm reception API error")
                    return@launch
                }

                val dto = body.result
                _confirmReceptionState.value = UiState.Success(dto)

                loadTracker(groupId)

            } catch (e: Exception) {
                _confirmReceptionState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun loadReceivedCheckImage(groupId: Long) {
        viewModelScope.launch {
            _receivedImageState.value = UiState.Loading
            try {
                val body = RetrofitClient.api().getTrackerCheckReceivedImage(groupId)

                if (!body.isSuccess || body.result == null) {
                    _receivedImageState.value =
                        UiState.Error(body.message ?: "received image API error")
                    return@launch
                }

                _receivedImageState.value = UiState.Success(body.result)

            } catch (e: Exception) {
                _receivedImageState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    private suspend fun uploadToPresignedUrl(
        putUrl: String,
        bytes: ByteArray,
        contentType: String
    ): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val body = bytes.toRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(putUrl)
            .put(body)
            .addHeader("Content-Type", contentType)
            .build()

        client.newCall(request).execute().use { resp ->
            resp.isSuccessful
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
        val base = buildSteps(role, _phase.value)
        _steps.value = applyRemainingDaysBadgeIfNeeded(base)
    }

    private fun applyRemainingDaysBadgeIfNeeded(list: List<TradeStatusItem>): List<TradeStatusItem> {
        val status = _currentTrackerStatus.value
        if (status !in REMAINING_BADGE_STATUSES) return list

        val remainingDays = _remainingDays.value ?: return list
        val badgeText = formatRemainingDaysBadge(remainingDays)

        if (list.isEmpty()) return list

        val first = list.first()
        val replacedFirst = first.copy(badge = badgeText)

        return listOf(replacedFirst) + list.drop(1)
    }

    private fun formatRemainingDaysBadge(remainingDays: Int): String {
        return when {
            remainingDays > 0 -> "D-$remainingDays"
            remainingDays == 0 -> "D-day"
            else -> "D+${abs(remainingDays)}"
        }
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
