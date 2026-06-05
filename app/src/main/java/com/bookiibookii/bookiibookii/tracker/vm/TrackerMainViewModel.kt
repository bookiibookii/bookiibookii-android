package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingPlace
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.PartnerDeliveryResponseDTO
import com.bookiibookii.bookiibookii.data.model.tracker.toMeetingPlace
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.ReadingCardTarget
import com.bookiibookii.bookiibookii.tracker.model.toReadingCardTarget
import com.bookiibookii.bookiibookii.tracker.model.TrackerMainUiState
import com.bookiibookii.bookiibookii.tracker.model.toCardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerMainViewModel(
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerMainUiState())
    val state: StateFlow<TrackerMainUiState> = _state

    private val _deliveryAddress = MutableStateFlow<DeliveryAddressResDTO?>(null)
    val deliveryAddress: StateFlow<DeliveryAddressResDTO?> = _deliveryAddress

    // 상대방 운송장 정보 (운송장 정보 확인 다이얼로그용)
    private val _partnerDelivery = MutableStateFlow<PartnerDeliveryResponseDTO?>(null)
    val partnerDelivery: StateFlow<PartnerDeliveryResponseDTO?> = _partnerDelivery

    // 약속 장소: 희망교환장소 불러오기 또는 카카오 검색으로 채워짐
    private val _meetingPlace = MutableStateFlow<MeetingPlace?>(null)
    val meetingPlace: StateFlow<MeetingPlace?> = _meetingPlace

    // 등록된 약속 정보 (약속 확인 조회)
    private val _meetingInfo = MutableStateFlow<MeetingResDTO?>(null)
    val meetingInfo: StateFlow<MeetingResDTO?> = _meetingInfo

    init {
        load()
    }

    // 대표(희망) 교환 장소를 불러와 약속 장소로 채운다. isDefault 우선, 없으면 첫 항목.
    fun loadMyExchangePlace() {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.locationApi().getExchanges()
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    val list = body.result.orEmpty()
                    val picked = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
                    _meetingPlace.value = picked?.toMeetingPlace()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    // 카카오 검색에서 선택한 장소를 약속 장소로 채운다 (좌표 포함)
    fun setMeetingPlace(result: PlaceSearchResult) {
        _meetingPlace.value = result.toMeetingPlace()
    }

    fun clearMeetingPlace() {
        _meetingPlace.value = null
    }

    // "독서카드 작성" — groupId로 서재 책(memberBookId 등)을 해석해 콜백. 트래커엔 memberBookId가 없어서 필요
    fun openReadingCard(groupId: Long, onResolved: (ReadingCardTarget) -> Unit) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.libApi().getLibraryBooks()
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    body.result?.firstOrNull { it.groupId.toLong() == groupId }
                        ?.let { onResolved(it.toReadingCardTarget()) }
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    // 등록된 약속 조회 후 다이얼로그 표시
    fun loadMeeting(groupId: Long, onLoaded: () -> Unit) {
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
    fun completeMeeting(groupId: Long, onSuccess: () -> Unit) {
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
        groupId: Long,
        placeName: String,
        address: String,
        zipCode: String?,
        x: Double,
        y: Double,
        addressDetail: String?,
        scheduledAt: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.registerMeeting(
                    groupId,
                    MeetingRegisterReqDTO(
                        placeName = placeName,
                        address = address,
                        zipCode = zipCode,
                        x = x,
                        y = y,
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

    fun loadDeliveryAddress(groupId: Long, onLoaded: () -> Unit) {
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
        groupId: Long,
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

    // 상대방 운송장 정보 조회 후 다이얼로그 표시
    fun loadPartnerDelivery(groupId: Long, onLoaded: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.fetchPartnerDelivery(groupId)
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    _partnerDelivery.value = body.result
                    onLoaded()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun clearPartnerDelivery() {
        _partnerDelivery.value = null
    }

    // 상대방 운송장 수령 확인
    fun confirmReceive(groupId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.confirmPartnerReceive(groupId)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun registerDelivery(groupId: Long, deliveryCompany: String, trackingNumber: String) {
        viewModelScope.launch {
            try {
                val res = repository.registerDelivery(groupId, deliveryCompany, trackingNumber)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun recordProgress(groupId: Long, currentPage: Int) {
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
                val res = repository.fetchMyTrackers()
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    val result = res.body()?.result
                    val items = result?.items.orEmpty()
                    val summary = result?.summary
                    _state.update {
                        it.copy(
                            cards = items.map { dto -> dto.toCardModel() },
                            totalCount = summary?.totalCount ?: 0,
                            readingCount = summary?.readingCount ?: 0,
                            exchangingCount = summary?.exchangingCount ?: 0,
                            reviewCount = summary?.reviewCount ?: 0,
                            loading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "트래커를 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }
}
