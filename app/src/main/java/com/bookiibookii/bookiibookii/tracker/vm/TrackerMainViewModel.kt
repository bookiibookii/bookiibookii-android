package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
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
import kotlinx.coroutines.async
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

    // 마이페이지에 등록된 배송지 목록 (배송지 수정 다이얼로그용)
    private val _savedDeliveries = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    val savedDeliveries: StateFlow<List<DeliveryAddress>> = _savedDeliveries

    // 상대방 운송장 정보 (운송장 정보 확인 다이얼로그용)
    private val _partnerDelivery = MutableStateFlow<PartnerDeliveryResponseDTO?>(null)
    val partnerDelivery: StateFlow<PartnerDeliveryResponseDTO?> = _partnerDelivery

    // 약속 장소: 희망교환장소 불러오기 또는 카카오 검색으로 채워짐
    private val _meetingPlace = MutableStateFlow<MeetingPlace?>(null)
    val meetingPlace: StateFlow<MeetingPlace?> = _meetingPlace

    // 등록된 약속 정보 (약속 확인 조회)
    private val _meetingInfo = MutableStateFlow<MeetingResDTO?>(null)
    val meetingInfo: StateFlow<MeetingResDTO?> = _meetingInfo

    // 최초 조회는 여기서. 화면의 ON_RESUME은 '첫 진입을 건너뛰고' 복귀 때만 재조회하므로 중복되지 않는다.
    init {
        load()
        fetchNotificationDot()
    }

    // 상단 알림 아이콘 배지 점: SYSTEM·KEYWORD 알림 중 미읽음이 하나라도 있으면 표시
    fun fetchNotificationDot() {
        viewModelScope.launch {
            val systemDeferred = async {
                runCatching {
                    RetrofitClient.notiApi().getNotifications("SYSTEM", null, 20)
                }.getOrNull()?.body()?.result?.items.orEmpty()
            }
            val keywordDeferred = async {
                runCatching {
                    RetrofitClient.notiApi().getNotifications("KEYWORD", null, 20)
                }.getOrNull()?.body()?.result?.items.orEmpty()
            }
            val hasUnread = (systemDeferred.await() + keywordDeferred.await()).any { !it.isRead }
            _state.update { it.copy(hasNewNotification = hasUnread) }
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

    // "독서카드 작성" — 한 그룹에 책이 여러 권일 수 있어 groupId + 현재 읽는 책 제목으로 매칭해 해석
    fun openReadingCard(groupId: Long, bookTitle: String, onResolved: (ReadingCardTarget) -> Unit) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.libApi().getLibraryBooks()
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    body.result
                        ?.firstOrNull { it.groupId.toLong() == groupId && it.title == bookTitle }
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

    // 배송지 수정 다이얼로그 진입 시 마이페이지 등록 배송지 목록 조회
    fun loadSavedDeliveries(onLoaded: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.fetchSavedDeliveries()
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    _savedDeliveries.value = body.result.orEmpty()
                }
            } catch (_: Exception) {
                // 실패해도 다이얼로그는 연다 (직접 입력 가능)
            } finally {
                onLoaded()
            }
        }
    }

    // 이번 교환 배송지 변경 - 기존 배송지 선택
    fun changeDeliveryAddressSaved(
        groupId: Long,
        userDeliveryId: Long,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.changeDeliveryAddressSaved(groupId, userDeliveryId)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    // 이번 교환 배송지 변경 - 직접 입력 (주소만)
    fun changeDeliveryAddressDirect(
        groupId: Long,
        zipCode: String,
        address: String,
        addressDetail: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val res = repository.changeDeliveryAddressDirect(groupId, zipCode, address, addressDetail)
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
                            nickname = result?.nickname.orEmpty(),
                            notifications = result?.topBanners.orEmpty().map { b -> b.toNotificationItem() },
                            totalCount = summary?.totalCount ?: 0,
                            readingCount = summary?.readingCount ?: 0,
                            exchangingCount = summary?.exchangingCount ?: 0,
                            reviewCount = summary?.reviewCount ?: 0,
                            loading = false,
                            hasLoadedOnce = true,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "트래커를 불러오지 못했어요", loading = false, hasLoadedOnce = true) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false, hasLoadedOnce = true) }
            }
        }
    }
}
