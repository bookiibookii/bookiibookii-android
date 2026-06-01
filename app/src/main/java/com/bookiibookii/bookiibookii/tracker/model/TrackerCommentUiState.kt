package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.group.CommentItem

// 트래커 1:1 댓글 화면 UI 상태
// - 그룹 댓글과 API/스레드를 공유한다. 그룹 단계에서 달린 대댓글·비밀댓글 과거 내역이
//   그대로 남아 있으므로, 리스트는 그룹과 동일하게 트리(children) + 비밀(secret) 표시로 렌더
// - 1:1 대화라 새로 작성하는 건 최상위·공개 댓글만 가능
data class TrackerCommentUiState(
    // 헤더 타이틀 (트래커명)
    val title: String = "",
    // GET 응답 — 그룹과 동일한 트리 구조(children 포함)
    val comments: List<CommentItem> = emptyList(),

    val loading: Boolean = false,
    // 바텀 pull-up 새로고침 진행 중
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // 입력 필드
    val draft: String = "",
    // POST 진행 중
    val submitting: Boolean = false,
    // 삭제 진행 중인 댓글 id들
    val deletingIds: Set<Long> = emptySet(),
)
