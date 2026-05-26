package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateResponse
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.group.model.GroupCommentUiState
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupCommentViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // 라우트 인자로 받은 그룹 id. GroupDetail VM과 동일한 키 공유
    private val groupId: Long = checkNotNull(savedStateHandle[GroupDestinations.ARG_GROUP_ID]) {
        "groupId is required"
    }

    private val _state = MutableStateFlow(GroupCommentUiState())
    val state: StateFlow<GroupCommentUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: SharedFlow<Event> = _eventFlow.asSharedFlow()

    // 일회성 이벤트 — 토스트/스낵바용
    sealed class Event {
        data class ShowError(val message: String) : Event()
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
                    _state.update {
                        it.copy(
                            comments = list,
                            totalCount = countAll(list),
                            loading = false,
                            error = null,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "댓글을 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    fun retry() = load()

    // -- 입력 필드 --

    // 250자 cap
    // 답글 모드일 땐 mention prefix atomicity 적용
    // "한 글자라도 지우면 멘션 전체 삭제 + 답글 해제" 요구사항
    fun onDraftChange(text: String) {
        val s = _state.value
        if (s.mentionNickname != null) {
            val prefix = mentionPrefix(s.mentionNickname)
            if (!text.startsWith(prefix)) {
                _state.update {
                    it.copy(
                        draft = "",
                        replyTargetId = null,
                        mentionNickname = null,
                        draftSecret = false,
                    )
                }
                return
            }
        }
        val capped = if (text.length > MAX_CONTENT_LEN) text.take(MAX_CONTENT_LEN) else text
        _state.update { it.copy(draft = capped) }
    }

    fun toggleSecret() {
        _state.update { it.copy(draftSecret = !it.draftSecret) }
    }

    // 답글 모드 진입. replyRequestId를 증가시켜 같은 댓글 재클릭에도 UI가 focus/키보드를 재요청하게 함
    fun startReply(parentId: Long, nickname: String) {
        _state.update {
            it.copy(
                replyTargetId = parentId,
                mentionNickname = nickname,
                draft = mentionPrefix(nickname),
                replyRequestId = it.replyRequestId + 1,
            )
        }
    }

    // 답글 모드 해제
    fun cancelReply() {
        _state.update {
            it.copy(
                draft = "",
                replyTargetId = null,
                mentionNickname = null,
                draftSecret = false,
            )
        }
    }

    // -- 댓글 작성 --

    fun submit() {
        val s = _state.value
        val content = if (s.mentionNickname != null) {
            s.draft.removePrefix(mentionPrefix(s.mentionNickname)).trim()
        } else {
            s.draft.trim()
        }
        if (content.isEmpty() || s.submitting) return

        // secret은 대댓글일 때만 가능. 일반 댓글이면 false 강제
        val isReply = s.replyTargetId != null
        val secret = isReply && s.draftSecret

        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val res = RetrofitClient.grpApi().postComment(
                    groupId = groupId,
                    request = CommentCreateRequest(
                        content = content,
                        parentId = s.replyTargetId,
                        secret = secret,
                    ),
                )
                val created = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && created != null) {
                    val newItem = created.toCommentItem(secret = secret)
                    _state.update { addLocally(it, newItem) }
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

    // 응답 댓글을 트리에 끼워넣고 입력 상태 리셋
    private fun addLocally(s: GroupCommentUiState, item: CommentItem): GroupCommentUiState {
        val nextComments = if (item.parentId == null) {
            // 일반 댓글 — 시간순 정렬이라 맨 뒤에 append
            s.comments + item
        } else {
            // 대댓글 — 부모 찾아서 children 끝에 append
            s.comments.map { parent ->
                if (parent.id == item.parentId) {
                    parent.copy(children = (parent.children ?: emptyList()) + item)
                } else {
                    parent
                }
            }
        }
        return s.copy(
            comments = nextComments,
            totalCount = countAll(nextComments),
            // 입력 상태 리셋
            draft = "",
            draftSecret = false,
            replyTargetId = null,
            mentionNickname = null,
            submitting = false,
        )
    }

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
                    // 성공 후 재조회
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
    }
}

// "@{닉네임} " (뒤에 공백 1) — 입력 필드 멘션 prefix
private fun mentionPrefix(nickname: String): String = "@$nickname "

// 부모 + 답글 합산
private fun countAll(comments: List<CommentItem>): Int =
    comments.sumOf { 1 + (it.children?.size ?: 0) }

// POST 응답을 화면용 CommentItem으로 변환
private fun CommentCreateResponse.toCommentItem(secret: Boolean): CommentItem = CommentItem(
    id = commentId,
    deleted = false,
    secret = secret,
    content = content,
    parentId = parentId,
    writer = writer,
    createdAt = createdAt,
    children = null,
)
