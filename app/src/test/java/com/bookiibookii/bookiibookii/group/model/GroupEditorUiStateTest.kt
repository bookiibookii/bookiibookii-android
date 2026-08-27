package com.bookiibookii.bookiibookii.group.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupEditorUiStateTest {
    // enum 값 이름에 의존하지 않도록 entries.first() 사용
    private val style = ReadingStyle.entries.first()
    private val trade = ExchangeType.entries.first()

    private fun createValid() = GroupEditorUiState(
        isbn13 = "9791234567890", groupName = "그룹", tradeType = trade,
        selectedPlaceId = 1L, ruleStyle = style,
    )

    @Test fun `생성 모드 필수값 충족 시 제출 가능`() = assertTrue(createValid().canSubmit)

    @Test fun `소개 500자는 허용, 501자는 불가`() {
        assertTrue(createValid().copy(groupComment = "가".repeat(500)).canSubmit)
        assertFalse(createValid().copy(groupComment = "가".repeat(501)).canSubmit)
    }

    @Test fun `커스텀 규칙 4개까지 허용, 5개는 불가 - 프리셋 포함 5 상한`() {
        assertTrue(createValid().copy(customRules = List(4) { "규칙$it" }).canSubmit)
        assertFalse(createValid().copy(customRules = List(5) { "규칙$it" }).canSubmit)
    }

    @Test fun `blank 규칙은 카운트 제외`() =
        assertTrue(createValid().copy(customRules = listOf("규칙", "", "  ", "", "")).canSubmit)

    @Test fun `수정 모드는 도서-교환유형-주소 없이도 제출 가능`() =
        assertTrue(GroupEditorUiState(isEdit = true, groupName = "그룹", ruleStyle = style).canSubmit)

    @Test fun `생성 모드는 항상 dirty`() = assertTrue(createValid().isDirty)

    @Test fun `수정 모드 - 원본과 같으면 not dirty, blank 규칙 추가도 not dirty`() {
        val original = GroupEditorUiState.EditOriginal(
            groupName = "그룹", readingPeriodIndex = 0, groupComment = "",
            ruleStyle = style, customRules = listOf("규칙1"),
        )
        val state = GroupEditorUiState(
            isEdit = true, groupName = "그룹", readingPeriodIndex = 0,
            ruleStyle = style, customRules = listOf("규칙1", "", "  "), editOriginal = original,
        )
        assertFalse(state.isDirty)
        assertTrue(state.copy(groupComment = "변경").isDirty)
    }
}
