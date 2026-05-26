package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.CommentItem

// 그룹 상세 화면의 댓글 바텀시트 UI 상태
data class GroupCommentUiState(
    // GET 응답
    val comments: List<CommentItem> = emptyList(),
    // 헤더 "댓글 N" 표시값. sumOf로 단순 계산
    val totalCount: Int = 0,

    val loading: Boolean = false,
    val error: String? = null,

    // 입력 필드 상태
    val draft: String = "",
    // 비밀 댓글 토글
    val draftSecret: Boolean = false,
    // null이면 일반 댓글 모드, 값 있으면 그 댓글에 대한 답글 모드
    val replyTargetId: Long? = null,
    // 답글 멘션에 표시할 닉네임
    val mentionNickname: String? = null,
    // startReply 호출마다 증가하는 토큰. 같은 댓글 재클릭(값 동일)에도 focus/키보드 재요청을 트리거하기 위함
    val replyRequestId: Int = 0,

    // POST 진행 중
    val submitting: Boolean = false,
    // 삭제 진행 중인 댓글 id들
    val deletingIds: Set<Long> = emptySet(),
) {
    val isReplyMode: Boolean get() = replyTargetId != null
}
