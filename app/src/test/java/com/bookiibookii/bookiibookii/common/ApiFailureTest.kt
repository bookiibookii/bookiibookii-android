package com.bookiibookii.bookiibookii.common

import org.junit.Assert.*
import org.junit.Test

class ApiFailureTest {
    @Test fun `도서 오류 상태 코드와 메시지를 보존한다`() {
        listOf(422 to "BOOK422_1", 422 to "BOOK422_3", 400 to "BOOK400_1",
            400 to "BOOK400_2", 400 to "BOOK400_3", 404 to "BOOK404_1", 503 to "BOOK503_1").forEach { (status, code) ->
            val error = parseApiFailure(status, """{"code":"$code","message":"서버 사유","result":null}""", "기본 안내")
            assertEquals(ApiFailure(status, code, "서버 사유"), error)
        }
    }

    @Test fun `잘못된 응답과 메시지 자료형은 기본 안내로 대체한다`() {
        listOf(null, "", "<html>error</html>", "[]", "null", "{}",
            """{"message":null}""", """{"message":{}}""", """{"message":123}""",
            """{"message":"  "}""").forEach {
            assertEquals("기본 안내", parseApiFailure(503, it, "기본 안내").message)
        }
        assertEquals("입력 확인", parseApiFailure(400,
            """{"message":"실패","result":["입력 확인"]}""", "기본 안내").message)
    }

    @Test fun `도서 요청의 정확한 제공처 장애만 국소 처리한다`() {
        val raw = """{"code":"BOOK503_1","message":"잠시 후 재시도"}"""
        listOf("GET" to "/api/books/search", "POST" to "/api/groups",
            "POST" to "/api/groups/1/apply", "POST" to "/api/onboarding",
            "POST" to "/api/mypage/bookshelf/favorites", "PATCH" to "/api/mypage/bookshelf/favorites/7"
        ).forEach { (method, path) ->
            assertTrue(isLocalBookFailure(method, path, 503, raw))
            assertFalse(isLocalBookFailure(method, path, 500, raw))
            assertFalse(isLocalBookFailure(method, path, 503, "{}"))
            assertFalse(isLocalBookFailure(method, path, 503, "<html>error</html>"))
        }
        assertFalse(isLocalBookFailure("POST", "/api/report", 503, raw))
        assertFalse(isLocalBookFailure("DELETE", "/api/mypage/bookshelf/favorites/7", 503, raw))
    }
}
