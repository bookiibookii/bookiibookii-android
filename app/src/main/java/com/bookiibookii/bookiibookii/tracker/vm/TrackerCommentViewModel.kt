package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateResponse
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerCommentUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 트래커 1:1 댓글 VM
// - 그룹 댓글과 동일한 groupId 사용
// - 조회는 트리 그대로 보여주되, 작성은 공개 댓글만(parentId=null, secret=false).
class TrackerCommentViewModel(
    private val groupId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerCommentUiState())
    val state: StateFlow<TrackerCommentUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: SharedFlow<Event> = _eventFlow.asSharedFlow()

    // 일회성 이벤트 — 토스트용
    sealed class Event {
        data class ShowError(val message: String) : Event()
        // 삭제된/존재하지 않는 그룹(404) → 삭제된 페이지 화면
        data object NotFound : Event()
    }

    init {
        load()
    }

    // -- 데이터 로드 --

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = RetrofitClient.grpApi().getComments(groupId)
                val list = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && list != null) {
                    _state.update { it.copy(comments = list, loading = false, error = null) }
                } else if (res.code() == 404) {
                    // 삭제된/존재하지 않는 그룹(예: 예전 알림으로 진입) → 삭제된 페이지 안내
                    _eventFlow.emit(Event.NotFound)
                    _state.update { it.copy(loading = false) }
                } else {
                    _state.update { it.copy(error = "댓글을 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    fun retry() = load()

    // 바텀 pull-up 새로고침 — 같은 조회 API 재호출. 진행 표시는 isRefreshing으로만(전체 로딩 X)
    fun refresh() {
        if (_state.value.isRefreshing) return
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                val res = RetrofitClient.grpApi().getComments(groupId)
                val list = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && list != null) {
                    _state.update { it.copy(comments = list, isRefreshing = false, error = null) }
                } else {
                    _eventFlow.emit(Event.ShowError("댓글을 불러오지 못했어요"))
                    _state.update { it.copy(isRefreshing = false) }
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    // -- 입력 필드 (250자, 멘션 없음) --

    fun onDraftChange(text: String) {
        val capped = if (text.length > MAX_CONTENT_LEN) text.take(MAX_CONTENT_LEN) else text
        _state.update { it.copy(draft = capped) }
    }

    // -- 댓글 작성 --

    fun submit() {
        val s = _state.value
        val content = s.draft.trim()
        if (content.isEmpty() || s.submitting) return

        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val res = RetrofitClient.grpApi().postComment(
                    groupId = groupId,
                    request = CommentCreateRequest(
                        content = content,
                        parentId = null,
                        secret = false,
                    ),
                )
                val created = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && created != null) {
                    _state.update { addLocally(it, created.toCommentItem()) }
                } else {
                    _eventFlow.emit(Event.ShowError("댓글 작성에 실패했어요"))
                    _state.update { it.copy(submitting = false) }
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    // 작성한 최상위 댓글을 리스트 끝에 append + 입력 상태 리셋
    private fun addLocally(s: TrackerCommentUiState, item: CommentItem): TrackerCommentUiState =
        s.copy(
            comments = s.comments + item,
            draft = "",
            submitting = false,
        )

    // -- 댓글 삭제 (DELETE 성공 시 reload) --

    fun delete(commentId: Long) {
        val s = _state.value
        if (commentId in s.deletingIds) return

        viewModelScope.launch {
            _state.update { it.copy(deletingIds = it.deletingIds + commentId) }
            try {
                val res = RetrofitClient.grpApi().deleteComment(
                    groupId = groupId,
                    commentId = commentId,
                )
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                } else {
                    _eventFlow.emit(Event.ShowError("댓글 삭제에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                _state.update { it.copy(deletingIds = it.deletingIds - commentId) }
            }
        }
    }

    companion object {
        private const val MAX_CONTENT_LEN = 250

        fun factory(groupId: Long) = viewModelFactory {
            initializer { TrackerCommentViewModel(groupId) }
        }
    }
}

// POST 응답을 화면용 CommentItem으로 변환
private fun CommentCreateResponse.toCommentItem(): CommentItem = CommentItem(
    id = commentId,
    deleted = false,
    secret = false,
    content = content,
    parentId = parentId,
    writer = writer,
    createdAt = createdAt,
    children = null,
)
