package com.bookiibookii.bookiibookii.common

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RunCatchingCancellableTest {

    @Test
    fun `성공하면 결과를 담은 success를 돌려준다`() {
        assertEquals("책", runCatchingCancellable { "책" }.getOrNull())
    }

    @Test
    fun `일반 예외는 failure로 감싼다`() {
        val result = runCatchingCancellable { throw IOException("연결 실패") }
        assertTrue(result.exceptionOrNull() is IOException)
    }

    // runCatching은 CancellationException까지 삼켜서 취소된 요청이
    // 사용자에게 "네트워크 오류"로 표시된다 (#493)
    @Test(expected = CancellationException::class)
    fun `코루틴 취소는 삼키지 않고 다시 던진다`() {
        runCatchingCancellable { throw CancellationException("검색 취소됨") }
    }
}
