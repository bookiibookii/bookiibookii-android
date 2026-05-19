package com.bookiibookii.bookiibookii.group.model

// 그룹 생성/수정 화면 UI 상태
data class GroupEditorUiState(
    val isbn13: String? = null,
    val groupName: String = "",
    val tradeType: ExchangeType? = null,
    val preferRegion: String? = null,      // DIRECT/DELIVERY
    val readingPeriodIndex: Int = 0,
    val groupComment: String = "",         // 선택, 최대 500자
    val ruleStyle: ReadingStyle? = null,   // 프리셋 1개 (드롭다운)
    val customRules: List<String> = emptyList(),  // CUSTOM 항목, 최대 4개
) {
    val readingPeriod: Int get() = PERIODS[readingPeriodIndex]

    val canSubmit: Boolean
        get() = isbn13 != null &&
            groupName.isNotBlank() &&
            tradeType != null &&
            !preferRegion.isNullOrBlank() &&
            groupComment.length <= 500 &&
            ruleStyle != null &&
            (1 + customRules.count { it.isNotBlank() }) in 1..5

    companion object {
        val PERIODS = listOf(3, 7, 14, 21, 28)
        const val MAX_CUSTOM_RULES = 4
    }
}
