package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.GroupApplyRequest
import com.bookiibookii.bookiibookii.group.model.GroupApplyUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 그룹 참여 신청 관련 도메인 VM
// 게스트 신청 (POST /apply), 게스트 취소 (DELETE /apply)
// 후속(자발적 wiring 금지 — 합의 후 추가):
//   - PATCH /api/groups/apply/{applyId} (수락/거절)
//   - GET /api/groups/{groupId}/applylist (신청자 명단 조회)
//   - GET /api/groups/apply/me (내가 신청한 그룹 목록)
class JoinRequestViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupApplyUiState())
    val state: StateFlow<GroupApplyUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 1회성 이벤트
    sealed class Event {
        // 신청 성공 — 다이얼로그 닫고 상세 새로고침 트리거용
        data object Applied : Event()
        // 신청 취소 성공 — 토스트 + 상세 새로고침 트리거용
        data object Canceled : Event()
        data class ShowError(val message: String) : Event()
    }

    // 취소 중복 호출 가드
    private var canceling = false

    // 다이얼로그 닫힐 때 호출
    fun reset() {
        _state.value = GroupApplyUiState()
    }

    // 입력 텍스트 변경. 텍스트를 수정하면 기존 책 선택은 해제
    fun onBookSearchQueryChange(value: String) =
        _state.update { it.copy(bookSearchQuery = value, isbn13 = null) }

    // ic_search 클릭 또는 키보드 검색 액션으로 호출
    fun searchBooks() {
        val q = _state.value.bookSearchQuery
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(bookSearchLoading = true, bookSearchError = null) }
            try {
                val res = RetrofitClient.grpApi().searchBooks(q, page = 1, size = 10)
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
    }

    // 책 선택 -> isbn13 채움 + 검색 결과 닫음 (Editor와 동일)
    fun onBookSelect(book: BookItem) = _state.update {
        it.copy(
            isbn13 = book.isbn13,
            bookSearchQuery = book.title,
            bookSearchResults = emptyList(),
        )
    }

    // 신청 한 마디 입력 (다이얼로그에서 50자 컷오프 후 호출)
    fun onApplyMsgChange(value: String) = _state.update { it.copy(applyMsg = value) }

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
