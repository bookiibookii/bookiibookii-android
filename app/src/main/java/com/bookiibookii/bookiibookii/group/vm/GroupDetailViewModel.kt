package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.group.model.GroupDetailUiState
import com.bookiibookii.bookiibookii.group.model.groupDetailActionButton
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // 라우트로 전달된 그룹 id (NavType.LongType이라 Long으로 꺼냄)
    private val groupId: Long = checkNotNull(savedStateHandle[GroupDestinations.ARG_GROUP_ID]) {
        "groupId is required"
    }

    private val _state = MutableStateFlow(GroupDetailUiState())
    val state: StateFlow<GroupDetailUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        data object Deleted : Event()
        data class ShowError(val message: String) : Event()
    }

    // 삭제 중복 호출 가드
    private var deleting = false

    init {
        // 진입 시 즉시 상세 조회
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = RetrofitClient.grpApi().getGroupDetail(groupId)
                val detail = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && detail != null) {
                    _state.update {
                        it.copy(
                            detail = detail,
                            actionButton = groupDetailActionButton(
                                buttonStatus = detail.buttonStatus,
                                waitingCount = detail.waitingCount,
                            ),
                            loading = false,
                            error = null,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "그룹 정보를 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    fun retry() = load()

    // 그룹 삭제 (DELETE /api/groups/{groupId}). 성공 시 Deleted 이벤트
    fun deleteGroup() {
        if (deleting) return
        deleting = true
        viewModelScope.launch {
            try {
                val res = RetrofitClient.grpApi().deleteGroup(groupId)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.Deleted)
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "그룹 삭제에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                deleting = false
            }
        }
    }
}
