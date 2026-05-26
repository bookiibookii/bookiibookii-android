package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.BookItem

// 그룹 참여 신청 다이얼로그 UI 상태
// - isbn13: 검색 결과에서 책을 선택해야 채워짐 (텍스트만 입력하면 null)
// - applyMsg: 신청 한 마디 (0~50자)
// - canSubmit: isbn13 != null && applyMsg 비어있지 않음 (스펙 required=둘 다)
data class GroupApplyUiState(
    val bookSearchQuery: String = "",
    val bookSearchResults: List<BookItem> = emptyList(),
    val bookSearchLoading: Boolean = false,
    val bookSearchError: String? = null,
    val isbn13: String? = null,
    val applyMsg: String = "",
    val submitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = isbn13 != null &&
            applyMsg.isNotBlank() &&
            applyMsg.length <= APPLY_MSG_MAX

    companion object {
        const val APPLY_MSG_MAX = 50
    }
}
