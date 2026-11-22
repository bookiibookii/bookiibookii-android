package com.bookiibookii.bookiibookii.group.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Test fun `필수값이 모두 있으면 안내 없음`() = assertNull(createValid().missingRequirement)

    @Test fun `여러 필수값이 비면 화면 위쪽 항목 하나만 안내`() {
        assertEquals("도서를 선택해주세요", GroupEditorUiState().missingRequirement)
        assertEquals(
            "그룹명을 입력해주세요",
            GroupEditorUiState(isbn13 = "9791234567890", groupName = " ").missingRequirement,
        )
        assertEquals(
            "교환 유형을 선택해주세요",
            createValid().copy(tradeType = null, selectedPlaceId = null, ruleStyle = null).missingRequirement,
        )
        assertEquals("독서 스타일을 선택해주세요", createValid().copy(ruleStyle = null).missingRequirement)
        assertEquals(
            "그룹 소개는 500자 이하로 작성해주세요",
            createValid().copy(groupComment = "가".repeat(501)).missingRequirement,
        )
    }

    @Test fun `주소는 목록이 비면 등록, 있으면 선택을 안내`() {
        val direct = createValid().copy(tradeType = ExchangeType.DIRECT, selectedPlaceId = null)
        assertEquals("희망 교환 장소를 등록해주세요", direct.missingRequirement)
        assertEquals("희망 교환 장소를 선택해주세요", direct.copy(placesLoading = true).missingRequirement)
        val delivery = createValid().copy(
            tradeType = ExchangeType.DELIVERY, selectedPlaceId = null,
            places = listOf(SelectablePlace(id = 1L, placeName = "집", address = "서울", isDefault = true)),
        )
        assertEquals("배송지를 선택해주세요", delivery.missingRequirement)
    }

    @Test fun `수정 모드는 바꿀 수 없는 도서-교환유형-주소를 안내하지 않음`() {
        assertNull(GroupEditorUiState(isEdit = true, groupName = "그룹", ruleStyle = style).missingRequirement)
        assertEquals(
            "그룹명을 입력해주세요",
            GroupEditorUiState(isEdit = true, ruleStyle = style).missingRequirement,
        )
    }

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
