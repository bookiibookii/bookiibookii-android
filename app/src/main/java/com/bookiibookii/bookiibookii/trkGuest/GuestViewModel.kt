package com.bookiibookii.bookiibookii.trkGuest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.TrackerCheckImageResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReadingStartResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartResponseDto
import com.bookiibookii.bookiibookii.trkHost.Phase
import com.bookiibookii.bookiibookii.trkHost.Role
import com.bookiibookii.bookiibookii.trkHost.StepId
import com.bookiibookii.bookiibookii.trkHost.TrackerStatus
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import com.bookiibookii.bookiibookii.trkHost.UiState
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

    private val _confirmReceptionState =
        MutableStateFlow<UiState<TrackerDetailResponseDto>>(UiState.Idle)
    val confirmReceptionState: StateFlow<UiState<TrackerDetailResponseDto>> =
        _confirmReceptionState.asStateFlow()

    private val _receiveState =
        MutableStateFlow<UiState<TrackerReceiveResponseDto>>(UiState.Idle)
    val receiveState: StateFlow<UiState<TrackerReceiveResponseDto>> =
        _receiveState.asStateFlow()

    private val _readingStartState =
        MutableStateFlow<UiState<TrackerReadingStartResponseDto>>(UiState.Idle)
    val readingStartState: StateFlow<UiState<TrackerReadingStartResponseDto>> =
        _readingStartState.asStateFlow()

    private val _extensionState =
        MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val extensionState: StateFlow<UiState<Unit>> =
        _extensionState.asStateFlow()

    private val _doneState =
        MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val doneState: StateFlow<UiState<Unit>> =
        _doneState.asStateFlow()

    private val _shippingStartState =
        MutableStateFlow<UiState<TrackerShippingStartResponseDto>>(UiState.Idle)
    val shippingStartState: StateFlow<UiState<TrackerShippingStartResponseDto>> =
        _shippingStartState.asStateFlow()

    private val _checkImageState =
        MutableStateFlow<UiState<TrackerCheckImageResponseDto>>(UiState.Idle)
    val checkImageState: StateFlow<UiState<TrackerCheckImageResponseDto>> =
        _checkImageState.asStateFlow()

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

    fun resetExtensionState() {
        _extensionState.value = UiState.Idle
    }

    fun resetReceiveState() {
        _receiveState.value = UiState.Idle
    }

    fun resetReadingStartState() {
        _readingStartState.value = UiState.Idle
    }

    fun resetDoneState() {
        _doneState.value = UiState.Idle
    }

    fun resetShippingStartState() {
        _shippingStartState.value = UiState.Idle
    }

    fun loadCheckImage(groupId: Long) {
        viewModelScope.launch {
            _checkImageState.value = UiState.Loading
            try {
                val body = RetrofitClient.api().getTrackerCheckReceivedImage(groupId)

                if (!body.isSuccess || body.result == null) {
                    _checkImageState.value = UiState.Error(body.message ?: "check image API error")
                    return@launch
                }

                _checkImageState.value = UiState.Success(body.result)
            } catch (e: Exception) {
                _checkImageState.value = UiState.Error(e.message ?: "network error")
            }
        }
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

                _currentTrackerStatus.value = TrackerStatus.from(dto.trackerStatus)
                _remainingDays.value = dto.remainingDays

                setPhaseFromApiStatus(dto.trackerStatus)

            } catch (e: Exception) {
                _uiState.value = GuestTrackerUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "network error"
                )
            }
        }
    }

    fun patchTrackerExtension(groupId: Long, days: Int) {
        viewModelScope.launch {
            _extensionState.value = UiState.Loading
            android.util.Log.d("IMG_DEBUG", "confirmReception start groupId=$groupId")

            try {
                val body = RetrofitClient.api()
                    .patchTrackerExtension(groupId, days)

                android.util.Log.d(
                    "IMG_DEBUG",
                    "confirmReception resp isSuccess=${body.isSuccess} msg=${body.message} result=${body.result}"
                )

                if (!body.isSuccess) {
                    _extensionState.value = UiState.Error(body.message ?: "독서 기간 연장 실패")
                    return@launch
                }

                _extensionState.value = UiState.Success(Unit)
                loadTracker(groupId)

            } catch (e: Exception) {
                android.util.Log.e("IMG_DEBUG", "confirmReception error", e)
                _extensionState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun patchTrackerDone(groupId: Long) {
        viewModelScope.launch {
            _doneState.value = UiState.Loading

            try {
                val body = RetrofitClient.api()
                    .patchTrackerDone(groupId)

                if (!body.isSuccess) {
                    _doneState.value = UiState.Error(body.message ?: "독서 완료 등록 실패")
                    return@launch
                }

                _doneState.value = UiState.Success(Unit)
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
                loadTracker(groupId)

            } catch (e: Exception) {
                _receiveState.value = UiState.Error(e.message ?: "network error")
            }
        }
    }

    fun patchTrackerReadingStart(groupId: Long) {
        viewModelScope.launch {
            _readingStartState.value = UiState.Loading

            try {
                val body = RetrofitClient.api()
                    .patchTrackerReadingStart(groupId)

                if (!body.isSuccess || body.result == null) {
                    _readingStartState.value =
                        UiState.Error(body.message ?: "독서 시작 실패")
                    return@launch
                }

                _readingStartState.value = UiState.Success(body.result)
                loadTracker(groupId)

            } catch (e: Exception) {
                _readingStartState.value =
                    UiState.Error(e.message ?: "network error")
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

                _confirmReceptionState.value = UiState.Success(body.result)
                loadTracker(groupId)

            } catch (e: Exception) {
                _confirmReceptionState.value =
                    UiState.Error(e.message ?: "network error")
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

    private suspend fun uploadToPresignedUrl(
        putUrl: String,
        bytes: ByteArray,
        contentType: String
    ): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        android.util.Log.d(TAG, "PUT start url=${putUrl.take(60)}... type=$contentType bytes=${bytes.size}")

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

    private fun phaseFromServerStatus(status: String?): Phase {
        return when (status?.uppercase()) {
            "READY" -> Phase.INIT

            "HOST_READING", "READING" -> Phase.HOST_READING
            "HOST_DONE" -> Phase.HOST_SHIPPING_READY
            "SHIPPING_TO_GUEST" -> Phase.HOST_SHIPPED

            "RECEIVED",
            "GUEST_READING",
            "GUEST_EXTENSION" -> Phase.GUEST_READING

            "GUEST_DONE" -> Phase.GUEST_SHIPPING_READY
            "SHIPPING_TO_HOST" -> Phase.GUEST_SHIPPED

            "COMPLETED", "RETURNED" -> Phase.FINISHED
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
        val base = buildSteps(role, _phase.value)
        _steps.value = applyRemainingDaysBadgeIfNeeded(base)
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
