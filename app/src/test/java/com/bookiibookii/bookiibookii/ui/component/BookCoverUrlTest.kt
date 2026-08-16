package com.bookiibookii.bookiibookii.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class BookCoverUrlTest {
    private val base = "https://image.aladin.co.kr/product/123/45"

    @Test fun `coversum을 지정 사이즈로 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/coversum/img.jpg".toAladinCover("cover500"))

    @Test fun `cover200 등 숫자 토큰 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/cover200/img.jpg".toAladinCover("cover500"))

    @Test fun `bare cover 토큰 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/cover/img.jpg".toAladinCover("cover500"))

    @Test fun `파일명 직전 세그먼트만 교체 - 중간 세그먼트는 유지`() =
        assertEquals("$base/cover/extra/img.jpg", "$base/cover/extra/img.jpg".toAladinCover("cover500"))

    @Test fun `알라딘 도메인 아니면 원본 유지`() {
        val other = "https://example.com/cover200/img.jpg"
        assertEquals(other, other.toAladinCover("cover500"))
    }
}
