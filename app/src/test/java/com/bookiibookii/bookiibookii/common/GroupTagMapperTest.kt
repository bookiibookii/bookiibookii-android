package com.bookiibookii.bookiibookii.common

import org.junit.Assert.assertEquals
import org.junit.Test

// bindTags는 Material Chip(View) 의존이라 제외 — toKoreanTag만 검증
class GroupTagMapperTest {
    @Test fun `알려진 태그 매핑`() = assertEquals("#메모환영", GroupTagMapper.toKoreanTag("MEMO"))
    @Test fun `미지 태그는 # 접두`() = assertEquals("#커스텀", GroupTagMapper.toKoreanTag("커스텀"))
    @Test fun `이미 #로 시작하면 그대로`() = assertEquals("#이미태그", GroupTagMapper.toKoreanTag("#이미태그"))
    @Test fun `빈 문자열은 # 하나`() = assertEquals("#", GroupTagMapper.toKoreanTag(""))
    @Test fun `대소문자 구분 - 소문자는 미매핑`() = assertEquals("#memo", GroupTagMapper.toKoreanTag("memo"))
    @Test fun `중복 한글 매핑 - POEM_ESSAY와 POETRY_ESSAY 동일`() =
        assertEquals(GroupTagMapper.toKoreanTag("POEM_ESSAY"), GroupTagMapper.toKoreanTag("POETRY_ESSAY"))
}
