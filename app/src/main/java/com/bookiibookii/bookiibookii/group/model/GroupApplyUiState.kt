package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.common.bookSelectionHint
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
    val bookSearchNoResult: Boolean = false,       // 마지막 검색이 성공했지만 0건
    val bookSearchDropdownVisible: Boolean = false, // 결과가 있어도 사용자가 닫았으면 false
    val isbn13: String? = null,
    val applyMsg: String = "",
    val submitting: Boolean = false,
) {
    // 결과가 남아 있으면 다시 열 수 있으므로 닫힘 여부와 결과 유무를 함께 본다
    val showBookDropdown: Boolean
        get() = bookSearchDropdownVisible && bookSearchResults.isNotEmpty()

    val bookSearchHint: String?
        get() = bookSelectionHint(
            query = bookSearchQuery,
            isbn13 = isbn13,
            hasResults = bookSearchResults.isNotEmpty(),
            noResult = bookSearchNoResult,
            error = bookSearchError,
        )

    val canSubmit: Boolean
        get() = isbn13 != null &&
            applyMsg.isNotBlank() &&
            applyMsg.length <= APPLY_MSG_MAX

    companion object {
        const val APPLY_MSG_MAX = 50
    }
}
