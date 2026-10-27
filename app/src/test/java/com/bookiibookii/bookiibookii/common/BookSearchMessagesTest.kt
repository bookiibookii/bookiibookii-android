package com.bookiibookii.bookiibookii.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 도서 검색 안내 문구.
 *
 * 그룹 생성/참여 신청은 isbn13이 있어야 제출할 수 있는데, 검색어만 입력하고 목록에서
 * 고르지 않으면 텍스트는 채워져 있는데 버튼이 비활성이라 사용자가 이유를 알 수 없다.
 */
class BookSearchMessagesTest {

    private fun hint(
        query: String = "해리포터",
        isbn13: String? = null,
        hasResults: Boolean = false,
        noResult: Boolean = false,
        error: String? = null,
    ) = bookSelectionHint(
        query = query,
        isbn13 = isbn13,
        hasResults = hasResults,
        noResult = noResult,
        error = error,
    )

    @Test
    fun `책을 선택했으면 안내하지 않는다`() {
        assertNull(hint(isbn13 = "9788983920775", hasResults = true))
    }

    @Test
    fun `입력 전에는 안내하지 않는다`() {
        assertNull(hint(query = ""))
        assertNull(hint(query = "   "))
    }

    @Test
    fun `검색 결과가 있는데 고르지 않았으면 선택하라고 안내한다`() {
        assertEquals("목록에서 책을 선택해 주세요", hint(hasResults = true))
    }

    // 검색은 됐는데 0건이면 드롭다운도 안 뜨고 아무 반응이 없어 보인다
    @Test
    fun `검색 결과가 0건이면 결과가 없다고 알린다`() {
        assertEquals("검색 결과가 없어요. 제목을 다시 확인해 주세요", hint(noResult = true))
    }

    @Test
    fun `오류 메시지가 이미 보이면 안내를 겹치지 않는다`() {
        assertNull(hint(noResult = true, error = "네트워크 오류가 발생했어요"))
    }

    @Test
    fun `검색 중에는 안내하지 않는다`() {
        assertNull(hint())
    }
}
