package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.GrpApi
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 도서 검색의 취소 처리 검증 (#493).
 *
 * 코루틴 취소는 오류가 아니다. 검색이 취소됐을 때 "네트워크 오류"를 표시하거나
 * 로딩 상태에 갇히면 안 된다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JoinRequestBookSearchTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<GrpApi>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.grpApi() } returns api
        // 응답이 오지 않는 느린 서버 — 요청이 "진행 중"인 상태를 만든다
        coEvery { api.searchBooks(any(), any(), any()) } coAnswers { awaitCancellation() }
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        Dispatchers.resetMain()
    }

    // 검색이 진행 중인 ViewModel을 만든다
    private fun searchingViewModel(query: String = "해리"): JoinRequestViewModel {
        val vm = JoinRequestViewModel()
        vm.onBookSearchQueryChange(query)
        return vm
    }

    @Test
    fun `검색 중 화면을 벗어나면 네트워크 오류를 표시하지 않는다`() = runTest(dispatcher) {
        val vm = searchingViewModel()
        advanceUntilIdle()
        assertTrue("요청이 진행 중이어야 한다", vm.state.value.bookSearchLoading)

        // 화면 이탈 / ViewModel 소멸 — 진행 중인 요청이 취소된다
        vm.viewModelScope.cancel()
        advanceUntilIdle()

        assertNull(vm.state.value.bookSearchError)
    }

    // 취소를 다시 던지도록 고치면 catch가 loading을 내려주지 않게 되므로,
    // 최소 길이 미만으로 줄었을 때 로딩이 갇히지 않는지 함께 지킨다
    @Test
    fun `검색 중 쿼리가 최소 길이 미만으로 줄면 오류 없이 로딩이 풀린다`() = runTest(dispatcher) {
        val vm = searchingViewModel()
        advanceUntilIdle()
        assertTrue("요청이 진행 중이어야 한다", vm.state.value.bookSearchLoading)

        vm.onBookSearchQueryChange("해")
        advanceUntilIdle()

        assertNull(vm.state.value.bookSearchError)
        assertFalse("로딩이 갇히면 안 된다", vm.state.value.bookSearchLoading)

        vm.viewModelScope.cancel()
    }
}
