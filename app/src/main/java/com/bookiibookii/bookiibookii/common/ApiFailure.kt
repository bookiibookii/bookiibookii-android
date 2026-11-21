package com.bookiibookii.bookiibookii.common

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import retrofit2.Response

/** HTTP 상태와 서버 오류 코드를 보존하고, 문자열 메시지만 사용자에게 전달한다. */
data class ApiFailure(val status: Int, val code: String?, val message: String)

fun parseApiFailure(status: Int, raw: String?, fallback: String): ApiFailure {
    fun JsonElement?.text(): String? = this?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
        ?.asString?.trim()?.takeIf { it.isNotEmpty() }
    return try {
        val json = JsonParser.parseString(raw ?: "").asJsonObject
        val validation = json.get("result")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull().text()
        ApiFailure(status, json.get("code").text(), validation ?: json.get("message").text() ?: fallback)
    } catch (_: Exception) {
        ApiFailure(status, null, fallback)
    }
}

fun <T> Response<ApiResponse<T>>.apiFailure(fallback: String): ApiFailure {
    if (isSuccessful) {
        val body = body()
        return ApiFailure(code(), body?.code,
            body?.takeIf { !it.isSuccess }?.message?.takeIf { it.isNotBlank() } ?: fallback)
    }
    val raw = try { errorBody()?.string() } catch (_: java.io.IOException) { null }
    return parseApiFailure(code(), raw, fallback)
}

/** 제공처 장애만 해당 도서 요청 화면에 맡긴다. 다른 서버 오류는 전역 처리한다. */
fun isLocalBookFailure(method: String, path: String, status: Int, raw: String?): Boolean {
    val bookRequest = when (method) {
        "GET" -> path == "/api/books/search"
        "POST" -> path in setOf("/api/groups", "/api/onboarding", "/api/mypage/bookshelf/favorites") ||
            Regex("/api/groups/[0-9]+/apply").matches(path)
        "PATCH" -> Regex("/api/mypage/bookshelf/favorites/[0-9]+").matches(path)
        else -> false
    }
    return bookRequest && status == 503 && parseApiFailure(status, raw, "").code == "BOOK503_1"
}
