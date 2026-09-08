package com.bookiibookii.bookiibookii.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/** 실시간 검색 공통 기본값 */
const val DEFAULT_SEARCH_DEBOUNCE_MS = 350L
const val DEFAULT_SEARCH_MIN_LENGTH = 2

/** 한글 호환 자모(ㄱ~ㆎ) — 두벌식 IME가 조합 중일 때 마지막 글자로 흘리는 미완성 상태 */
private val HANGUL_COMPAT_JAMO = 'ㄱ'..'ㆎ'

/** "해ㄹ"처럼 마지막 음절이 아직 조합 중인 문자열인지 */
private fun String.isComposingHangul(): Boolean {
    val last = lastOrNull() ?: return false
    return last in HANGUL_COMPAT_JAMO
}

/**
 * 실시간(타이핑) 검색용 공통 트리거.
 *
 * 두 가지 시점에 [onSearch]가 호출된다:
 *  1. **글자 수가 늘어나는 순간 즉시** — 사용자가 글자를 추가하면 기다리지 않고 바로 검색
 *  2. **입력이 [debounceMs] 멈췄을 때** — 한글은 마지막 음절 완성 시 length가 그대로라(한ㄱ→한글)
 *     즉시 트리거를 놓치므로, 멈춤 시점에 완성된 문자열로 보정 검색
 *
 * - [minLength] 미만이면 검색하지 않고 [onBelowMinLength] 호출(결과 비우기)
 * - 조합 중인 자모("해ㄹ")는 아예 흘려보내지 않는다 — 검색 가치가 없고 요청만 늘어난다
 * - [collectLatest]로 이전 검색을 취소해 빠른 타이핑 시 중간 요청을 버리고 응답 race도 막는다
 * - viewModelScope에 묶이므로 VM 소멸 시 자동 취소
 *
 * [onSearch]는 쿼리가 바뀌면 취소되므로, 구현부는 `CancellationException`을
 * 오류로 표시하지 말고 그대로 다시 던져야 한다.
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
        val trimmed = queryFlow
            .map { it.trim() }
            // 조합 중인 자모를 여기서 거르면 아래 prevLength도 완성된 글자만 세게 되어
            // "해"→"해리"처럼 음절이 늘어날 때 즉시 트리거가 제대로 걸린다
            .filterNot { it.isComposingHangul() }
            .distinctUntilChanged()

        // 직전 길이 추적용 — 즉시 트리거가 "늘어났는지" 판정
        var prevLength = 0

        merge(
            // 1) 글자 수가 늘어나는 순간
            trimmed.filter { query ->
                val grew = query.length > prevLength
                prevLength = query.length
                grew
            },
            // 2) 입력이 멈췄을 때
            trimmed.debounce(debounceMs),
        )
            // 즉시 트리거가 이미 검색한 값을 디바운스가 그대로 다시 흘리면, collectLatest가
            // 진행 중인 요청을 취소만 하고 재검색은 하지 않아 응답이 영영 오지 않는다.
            // 중복을 상류에서 버려 취소 자체를 만들지 않는다
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.length < minLength) onBelowMinLength() else onSearch(query)
            }
    }
}
