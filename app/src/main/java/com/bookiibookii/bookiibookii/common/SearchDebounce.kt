package com.bookiibookii.bookiibookii.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/** 실시간 검색 공통 기본값 */
const val DEFAULT_SEARCH_DEBOUNCE_MS = 350L
const val DEFAULT_SEARCH_MIN_LENGTH = 2

/**
 * 실시간(타이핑) 검색용 공통 트리거.
 *
 * 두 가지 시점에 [onSearch]가 호출된다:
 *  1. **글자 수가 늘어나는 순간 즉시** — 사용자가 글자를 추가하면 기다리지 않고 바로 검색
 *  2. **입력이 [debounceMs] 멈췄을 때** — 한글은 마지막 음절 완성 시 length가 그대로라(한ㄱ→한글)
 *     즉시 트리거를 놓치므로, 멈춤 시점에 완성된 문자열로 보정 검색
 *
 * - [minLength] 미만이면 검색하지 않고 [onBelowMinLength] 호출(보통 결과 비우기)
 * - 직전과 동일한 쿼리는 중복 검색하지 않음(즉시+디바운스가 같은 값을 낼 때 1회만)
 * - [collectLatest]로 이전 검색을 취소해 빠른 타이핑 시 중간 요청을 버리고 응답 race도 막는다
 * - viewModelScope에 묶이므로 VM 소멸 시 자동 취소
 */
@OptIn(FlowPreview::class)
fun ViewModel.observeSearchQuery(
    queryFlow: Flow<String>,
    minLength: Int = DEFAULT_SEARCH_MIN_LENGTH,
    debounceMs: Long = DEFAULT_SEARCH_DEBOUNCE_MS,
    onBelowMinLength: () -> Unit,
    onSearch: suspend (String) -> Unit,
) {
    viewModelScope.launch {
        val trimmed = queryFlow.map { it.trim() }.distinctUntilChanged()

        // 직전 길이 추적용 — 즉시 트리거가 "늘어났는지" 판정
        var prevLength = 0
        // 직전에 실제 검색한 쿼리 — 즉시/디바운스 중복 발화 방지
        var lastSearched: String? = null

        merge(
            // 1) 글자 수가 늘어나는 순간
            trimmed.filter { query ->
                val grew = query.length > prevLength
                prevLength = query.length
                grew
            },
            // 2) 입력이 멈췄을 때
            trimmed.debounce(debounceMs),
        ).collectLatest { query ->
            when {
                query.length < minLength -> {
                    lastSearched = null
                    onBelowMinLength()
                }
                query != lastSearched -> {
                    lastSearched = query
                    onSearch(query)
                }
            }
        }
    }
}
