package com.bookiibookii.bookiibookii.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * 실시간 검색 트리거([observeSearchQuery]) 단위 테스트.
 *
 * 서버 응답을 [SLOW_RESPONSE_MS] 지연으로 흉내 내고, 어떤 쿼리가 요청됐는지(started)와
 * 취소되지 않고 끝까지 갔는지(completed)를 따로 기록해 취소 동작을 검증한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchDebounceTest {

    private companion object {
        /** 디바운스(350ms)보다 느린 서버 응답 */
        const val SLOW_RESPONSE_MS = 1_000L
    }

    private val dispatcher = StandardTestDispatcher()

    /** viewModelScope가 Dispatchers.Main.immediate를 쓰므로 테스트 디스패처로 바꿔둔다 */
    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeViewModel : ViewModel()

    /** 검색 호출 기록기 — 시작한 쿼리와 취소 없이 완료된 쿼리를 나눠 담는다 */
    private class SearchRecorder {
        val started = mutableListOf<String>()
        val completed = mutableListOf<String>()
        val belowMinLengthCount get() = _belowMinLengthCount
        private var _belowMinLengthCount = 0

        fun onBelowMinLength() { _belowMinLengthCount++ }

        suspend fun onSearch(query: String) {
            started += query
            delay(SLOW_RESPONSE_MS)
            completed += query
        }
    }

    private fun CoroutineScope.observe(
        vm: ViewModel,
        queryFlow: MutableStateFlow<String>,
        recorder: SearchRecorder,
    ) = vm.observeSearchQuery(
        queryFlow = queryFlow,
        onBelowMinLength = recorder::onBelowMinLength,
        onSearch = recorder::onSearch,
    )

    // 디바운스가 즉시 트리거와 같은 값을 다시 흘릴 때 진행 중인 요청이 취소되면
    // ViewModel의 catch가 CancellationException을 네트워크 오류로 표시한다 (#493)
    @Test
    fun `디바운스가 같은 쿼리를 다시 흘려도 진행 중인 검색을 취소하지 않는다`() = runTest(dispatcher) {
        val vm = FakeViewModel()
        val query = MutableStateFlow("")
        val recorder = SearchRecorder()
        observe(vm, query, recorder)
        advanceUntilIdle()

        // 마지막 입력이 글자 수를 늘리면 즉시 트리거가 검색을 시작한다
        query.value = "ab"
        // 350ms 뒤 디바운스가 같은 "ab"를 다시 방출하는 구간을 지난다
        advanceTimeBy(400)
        advanceUntilIdle()

        assertEquals(listOf("ab"), recorder.started)
        assertEquals(listOf("ab"), recorder.completed)

        vm.viewModelScope.cancel()
    }

    // 두벌식 IME는 조합 중 "해ㄹ" 같은 자모 상태를 그대로 흘린다 — 검색 가치가 없고
    // 요청만 늘어나며, 그만큼 진행 중인 요청 취소도 늘어난다 (#493)
    @Test
    fun `한글 조합 중인 자모 상태로는 검색하지 않는다`() = runTest(dispatcher) {
        val vm = FakeViewModel()
        val query = MutableStateFlow("")
        val recorder = SearchRecorder()
        observe(vm, query, recorder)
        advanceUntilIdle()

        query.value = "해"
        runCurrent()
        query.value = "해ㄹ"
        advanceUntilIdle()

        assertEquals(emptyList<String>(), recorder.started)

        vm.viewModelScope.cancel()
    }

    // 쿼리가 실제로 바뀌면 이전 검색은 버리고 새 검색을 해야 한다 — 위 두 수정이
    // 정상적인 취소까지 막지 않는지 확인하는 회귀 테스트
    @Test
    fun `쿼리가 바뀌면 이전 검색을 취소하고 마지막 쿼리만 완료된다`() = runTest(dispatcher) {
        val vm = FakeViewModel()
        val query = MutableStateFlow("")
        val recorder = SearchRecorder()
        observe(vm, query, recorder)
        advanceUntilIdle()

        query.value = "ab"
        runCurrent()
        query.value = "abc"
        advanceUntilIdle()

        assertEquals(listOf("ab", "abc"), recorder.started)
        assertEquals(listOf("abc"), recorder.completed)

        vm.viewModelScope.cancel()
    }
}
