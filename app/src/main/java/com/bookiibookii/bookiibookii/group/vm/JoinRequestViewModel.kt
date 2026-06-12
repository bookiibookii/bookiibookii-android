package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.common.observeSearchQuery
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.GroupAppStatusRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupApplyRequest
import com.bookiibookii.bookiibookii.group.model.ApplicationListUiState
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupApplyUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 그룹 참여 신청 관련 도메인 VM
// 게스트 신청 (POST /apply), 게스트 취소 (DELETE /apply),
// 호스트 명단 조회 (GET /applylist), 호스트 수락/거절 (PATCH /apply/{applyId})
// GET /api/groups/apply/me (내가 신청한 그룹 목록)
class JoinRequestViewModel : ViewModel() {

    // 게스트 신청 다이얼로그 상태 (apply/cancel)
    private val _state = MutableStateFlow(GroupApplyUiState())
    val state: StateFlow<GroupApplyUiState> = _state

    // 호스트 신청자 명단 화면 상태 (loadApplicationList) — 다이얼로그 상태와 분리
    private val _applicationListState = MutableStateFlow(ApplicationListUiState())
    val applicationListState: StateFlow<ApplicationListUiState> = _applicationListState

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 실시간 도서 검색: 타이핑은 이 쿼리만 갱신하고 디바운스(공통 헬퍼)가 검색을 호출.
    // ic_search 버튼/키보드 액션은 searchBooks()로 즉시 검색(둘 다 지원)
    private val _bookSearchQuery = MutableStateFlow("")

    init {
        observeSearchQuery(
            queryFlow = _bookSearchQuery,
            onBelowMinLength = {
                _state.update { it.copy(bookSearchResults = emptyList(), bookSearchError = null) }
            },
            onSearch = { performBookSearch(it) },
        )
    }

    // 1회성 이벤트
    sealed class Event {
        // 신청 성공 — 다이얼로그 닫고 상세 새로고침 트리거용
        data object Applied : Event()
        // 신청 취소 성공 — 토스트 + 상세 새로고침 트리거용
        data object Canceled : Event()
        // 수락/거절 성공 — Route가 토스트 띄움 ("{이름} 님의 요청을 {수락|거절}했어요")
        // status: "ACCEPTED" | "REJECTED"
        data class ApplicationUpdated(val status: String, val applicantName: String) : Event()
        data class ShowError(val message: String) : Event()
        // APPLY 전 주소 등록 여부 확인 결과
        data object AddressReady : Event()    // 주소 있음 → 참여 신청 다이얼로그
        data object AddressMissing : Event()  // 주소 없음 → 주소 등록 안내 다이얼로그
    }

    // 주소 조회 중복 호출 가드
    private var checkingAddress = false

    // 취소 중복 호출 가드
    private var canceling = false

    private val updatingApplyIds = mutableSetOf<Long>()

    // 다이얼로그 닫힐 때 호출
    fun reset() {
        _state.value = GroupApplyUiState()
    }

    // 입력 텍스트 변경. 텍스트를 수정하면 기존 책 선택은 해제.
    // 쿼리를 디바운스 플로우에도 흘려 타이핑 검색이 동작하게 함
    fun onBookSearchQueryChange(value: String) {
        _state.update { it.copy(bookSearchQuery = value, isbn13 = null) }
        _bookSearchQuery.value = value
    }

    // ic_search 클릭 또는 키보드 검색 액션 — 디바운스 기다리지 않고 즉시 검색
    fun searchBooks() {
        val q = _state.value.bookSearchQuery.trim()
        if (q.isBlank()) return
        viewModelScope.launch { performBookSearch(q) }
    }

    private suspend fun performBookSearch(query: String) {
        // 이미 책을 고른 뒤 늦게 도착한 디바운스 검색이 선택을 덮어쓰지 않도록 가드
        if (_state.value.isbn13 != null) return
        _state.update { it.copy(bookSearchLoading = true, bookSearchError = null) }
        try {
            val res = RetrofitClient.grpApi().searchBooks(query, page = 1, size = 10)
            if (res.isSuccessful && res.body()?.isSuccess == true) {
                _state.update {
                    it.copy(
                        bookSearchResults = res.body()?.result?.books.orEmpty(),
                        bookSearchLoading = false,
                    )
                }
            } else {
                _state.update {
                    it.copy(bookSearchError = "검색에 실패했어요", bookSearchLoading = false)
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(bookSearchError = "네트워크 오류가 발생했어요", bookSearchLoading = false)
            }
        }
    }

    // 책 선택 -> isbn13 채움 + 검색 결과 닫음 (Editor와 동일)
    fun onBookSelect(book: BookItem) = _state.update {
        it.copy(
            isbn13 = book.isbn13,
            bookSearchQuery = book.title,
            bookSearchResults = emptyList(),
        )
    }

    // 텍스트 삭제 시 전체 초기화 (Editor와 동일)
    fun onClearBookSearch() {
        _bookSearchQuery.value = ""
        _state.update {
            it.copy(
                bookSearchQuery = "",
                isbn13 = null,
                bookSearchResults = emptyList(),
                bookSearchError = null,
            )
        }
    }

    // 신청 한 마디 입력 (다이얼로그에서 50자 컷오프 후 호출)
    fun onApplyMsgChange(value: String) = _state.update { it.copy(applyMsg = value) }

    // 참여 신청 전, 교환 유형에 맞는 주소(배송지/희망 교환 장소) 등록 여부 확인
    //   조회 성공 + 목록 있음 -> AddressReady (참여 신청 다이얼로그)
    //   조회 성공 + 목록 없음 -> AddressMissing (주소 등록 안내 다이얼로그)
    //   조회 실패           -> ShowError (토스트, 다이얼로그 미표시)
    fun checkAddressBeforeApply(tradeType: ExchangeType) {
        if (checkingAddress) return
        viewModelScope.launch {
            checkingAddress = true
            try {
                val api = RetrofitClient.locationApi()
                // null = 조회 실패
                val addresses = when (tradeType) {
                    ExchangeType.DELIVERY -> {
                        val res = api.getDeliveries()
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            res.body()?.result.orEmpty()
                        } else {
                            null
                        }
                    }
                    ExchangeType.DIRECT -> {
                        val res = api.getExchanges()
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            res.body()?.result.orEmpty()
                        } else {
                            null
                        }
                    }
                }
                when {
                    addresses == null -> _eventFlow.emit(Event.ShowError("주소 정보를 불러오지 못했어요"))
                    addresses.isNotEmpty() -> _eventFlow.emit(Event.AddressReady)
                    else -> _eventFlow.emit(Event.AddressMissing)
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                checkingAddress = false
            }
        }
    }

    // 그룹 참여 신청 제출 — POST /api/groups/{groupId}/apply
    // 성공 시 Event.Applied -> 호출부가 다이얼로그 닫고 상세 새로고침
    fun apply(groupId: Long) {
        val s = _state.value
        val isbn13 = s.isbn13 ?: return
        if (s.applyMsg.isBlank() || s.submitting) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val res = RetrofitClient.grpApi().applyGroup(
                    groupId = groupId,
                    request = GroupApplyRequest(isbn13 = isbn13, applyMsg = s.applyMsg),
                )
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.Applied)
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "참여 신청에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    // 신청자 명단 조회 — GET /api/groups/{groupId}/applylist
    // 결과는 applicationListState로 노출. 에러는 inline(state.error)
    fun loadApplicationList(groupId: Long) {
        viewModelScope.launch {
            _applicationListState.update { it.copy(loading = true, error = null) }
            try {
                val res = RetrofitClient.grpApi().getGroupApplications(groupId)
                val result = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && result != null) {
                    _applicationListState.update {
                        it.copy(
                            items = result.applicationList,
                            totalCount = result.totalCount,
                            loading = false,
                            error = null,
                        )
                    }
                } else {
                    _applicationListState.update {
                        it.copy(error = "신청자 명단을 불러오지 못했어요", loading = false)
                    }
                }
            } catch (e: Exception) {
                _applicationListState.update {
                    it.copy(error = "네트워크 오류가 발생했어요", loading = false)
                }
            }
        }
    }

    // 호스트가 게스트의 신청을 수락/거절 — PATCH /api/groups/apply/{applyId}
    // status: "ACCEPTED" | "REJECTED". 성공 시 명단 새로고침
    fun updateApplicationStatus(
        applyId: Long,
        status: String,
        applicantName: String,
        groupId: Long,
    ) {
        if (applyId in updatingApplyIds) return
        updatingApplyIds += applyId
        viewModelScope.launch {
            try {
                val res = RetrofitClient.grpApi().updateApplicationStatus(
                    applyId = applyId,
                    request = GroupAppStatusRequest(status = status),
                )
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.ApplicationUpdated(status, applicantName))
                    // 명단 새로고침 (상태 갱신: 수락된 항목 제거 등은 서버가 결정)
                    loadApplicationList(groupId)
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "처리에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                updatingApplyIds -= applyId
            }
        }
    }

    // 그룹 참여 신청 취소 — DELETE /api/groups/{groupId}/apply
    // 성공 시 토스트 + 상세 새로고침
    fun cancelApply(groupId: Long) {
        if (canceling) return
        canceling = true
        viewModelScope.launch {
            try {
                val res = RetrofitClient.grpApi().cancelGroupApplication(groupId)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.Canceled)
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "신청 취소에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                canceling = false
            }
        }
    }
}
