package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.BookItem

// 그룹 생성/수정 화면 UI 상태
data class GroupEditorUiState(
    val isbn13: String? = null,
    val bookSearchQuery: String = "",
    val bookSearchResults: List<BookItem> = emptyList(),
    val bookSearchLoading: Boolean = false,
    val bookSearchError: String? = null,
    val groupName: String = "",
    val tradeType: ExchangeType? = null,
    val places: List<SelectablePlace> = emptyList(),  // tradeType에 해당하는 주소 목록
    val placesLoading: Boolean = false,
    val selectedPlaceId: Long? = null,                // 선택한 주소 id (DIRECT→userExchangeId / DELIVERY→userDeliveryId)
    val readingPeriodIndex: Int = 0,
    val groupComment: String = "",         // 선택, 최대 500자
    val ruleStyle: ReadingStyle? = null,   // 프리셋 1개 (드롭다운)
    val customRules: List<String> = emptyList(),  // CUSTOM 항목, 최대 4개
    val submitting: Boolean = false,              // 그룹 생성/수정 요청 중
    val isEdit: Boolean = false,                  // 수정 모드 (groupId로 진입). 생성=false
) {
    val readingPeriod: Int get() = PERIODS[readingPeriodIndex]

    // 수정 모드는 PATCH 가능 필드(그룹명/독서기간/소개/규칙)만 검증 — 도서/교환유형/주소는 수정 불가
    val canSubmit: Boolean
        get() = if (isEdit) {
            groupName.isNotBlank() &&
                groupComment.length <= 500 &&
                ruleStyle != null &&
                (1 + customRules.count { it.isNotBlank() }) in 1..5
        } else {
            isbn13 != null &&
                groupName.isNotBlank() &&
                tradeType != null &&
                selectedPlaceId != null &&
                groupComment.length <= 500 &&
                ruleStyle != null &&
                (1 + customRules.count { it.isNotBlank() }) in 1..5
        }

    companion object {
        val PERIODS = listOf(3, 7, 14, 21, 28)
        const val MAX_CUSTOM_RULES = 4
    }
}
