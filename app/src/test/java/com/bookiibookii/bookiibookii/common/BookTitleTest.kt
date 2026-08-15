package com.bookiibookii.bookiibookii.common

import org.junit.Assert.assertEquals
import org.junit.Test

class BookTitleTest {
    @Test fun `null은 빈 문자열`() = assertEquals("", (null as String?).stripBookSubtitle())
    @Test fun `구분자 없으면 원본`() = assertEquals("프로젝트 헤일메리", "프로젝트 헤일메리".stripBookSubtitle())
    @Test fun `첫 구분자 앞만 남김`() = assertEquals("마션", "마션 - 스페셜 에디션 - 개정판".stripBookSubtitle())
    @Test fun `단어 내 하이픈은 유지`() = assertEquals("K-팝 시대", "K-팝 시대".stripBookSubtitle())
    @Test fun `선행 구분자면 빈 문자열`() = assertEquals("", " - 부제목만".stripBookSubtitle().trim())
    @Test fun `끝 공백 제거`() = assertEquals("제목", "제목  - 부제".stripBookSubtitle())
}
