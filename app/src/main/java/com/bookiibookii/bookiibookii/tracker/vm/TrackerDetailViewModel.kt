package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerDetailUiState
import com.bookiibookii.bookiibookii.tracker.model.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerDetailViewModel(
    private val groupId: Long,
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerDetailUiState())
    val state: StateFlow<TrackerDetailUiState> = _state

    private val _deliveryAddress = MutableStateFlow<DeliveryAddressResDTO?>(null)
    val deliveryAddress: StateFlow<DeliveryAddressResDTO?> = _deliveryAddress

    // 약속 장소: "나의 희망교환장소 불러오기"로 채워지는 대표 장소
    private val _meetingPlace = MutableStateFlow<ExchangeAddress?>(null)
    val meetingPlace: StateFlow<ExchangeAddress?> = _meetingPlace

    // 등록된 약속 정보 (약속 확인 조회)
    private val _meetingInfo = MutableStateFlow<MeetingResDTO?>(null)
    val meetingInfo: StateFlow<MeetingResDTO?> = _meetingInfo

    init {
        load()
    }

    fun loadDeliveryAddress(onLoaded: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.fetchDeliveryAddress(groupId)
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    _deliveryAddress.value = body.result
                    onLoaded()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun clearDeliveryAddress() {
        _deliveryAddress.value = null
    }

    fun updateMyDeliveryAddress(
        request: DeliveryAddressUpdateReqDTO,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.updateMyDeliveryAddress(groupId, request)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun registerDelivery(deliveryCompany: String, trackingNumber: String) {
        viewModelScope.launch {
            try {
                val res = repository.registerDelivery(groupId, deliveryCompany, trackingNumber)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시 (다음 단계에서 에러 표시)
            }
        }
    }

    // 대표(희망) 교환 장소를 불러와 약속 장소로 채운다. isDefault 우선, 없으면 첫 항목.
    fun loadMyExchangePlace() {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.locationApi().getExchanges()
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    val list = body.result.orEmpty()
                    _meetingPlace.value = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun clearMeetingPlace() {
        _meetingPlace.value = null
    }

    // 등록된 약속 조회 후 다이얼로그 표시
    fun loadMeeting(onLoaded: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.fetchMeeting(groupId)
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    _meetingInfo.value = body.result
                    onLoaded()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun clearMeeting() {
        _meetingInfo.value = null
    }

    // 직접 교환 완료 확인
    fun completeMeeting(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.completeMeeting(groupId)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun registerMeeting(
        locationId: Long,
        addressDetail: String?,
        scheduledAt: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.registerMeeting(
                    groupId,
                    MeetingRegisterReqDTO(
                        locationId = locationId,
                        addressDetail = addressDetail,
                        scheduledAt = scheduledAt,
                    ),
                )
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun recordProgress(currentPage: Int) {
        viewModelScope.launch {
            try {
                val res = repository.recordReadingProgress(groupId, currentPage)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시 (다음 단계에서 에러 표시 추가)
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = repository.fetchTrackerDetail(groupId)
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    val dto = body.result
                    if (dto != null) {
                        _state.value = dto.toUiState()
                    } else {
                        _state.update { it.copy(error = "트래커를 불러오지 못했어요", loading = false) }
                    }
                } else {
                    _state.update { it.copy(error = body?.message ?: "트래커를 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }

    companion object {
        fun factory(groupId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TrackerDetailViewModel(groupId)
            }
        }
    }
}
