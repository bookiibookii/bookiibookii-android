package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.GrpApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.BookSearchResponse
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

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

    private fun book(title: String, isbn13: String) = BookItem(
        title = title,
        author = "조앤 K. 롤링",
        image = "",
        publisher = "문학수첩",
        isbn13 = isbn13,
        category = "novel",
        categoryLabel = "영미소설",
        link = "",
    )

    private fun searchResponse(books: List<BookItem>) = Response.success(
        ApiResponse(
            isSuccess = true,
            code = "COMMON200",
            message = "성공",
            result = BookSearchResponse(books = books, totalPage = 1, totalResults = books.size),
        )
    )

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

    // 검색 A 결과에서 책을 고른 직후 검색 B 응답이 도착하면, 잠긴 입력 위로
    // 엉뚱한 목록이 다시 뜨고 거기서 누르면 선택이 조용히 바뀐다
    @Test
    fun `책을 선택한 뒤 늦게 도착한 검색 결과는 버린다`() = runTest(dispatcher) {
        val late = CompletableDeferred<Response<ApiResponse<BookSearchResponse>>>()
        coEvery { api.searchBooks(any(), any(), any()) } coAnswers { late.await() }

        val vm = searchingViewModel()
        advanceUntilIdle()

        // 응답을 기다리는 사이 사용자가 (앞선 검색 결과에서) 책을 선택
        vm.onBookSelect(book("해리 포터와 마법사의 돌", "9788983920775"))
        advanceUntilIdle()

        late.complete(searchResponse(listOf(book("해리 포터와 비밀의 방", "9788983921093"))))
        advanceUntilIdle()

        assertEquals("9788983920775", vm.state.value.isbn13)
        assertTrue("늦게 온 결과로 드롭다운이 다시 열리면 안 된다", vm.state.value.bookSearchResults.isEmpty())

        vm.viewModelScope.cancel()
    }

    @Test
    fun `검색 결과가 0건이면 결과 없음을 상태에 남긴다`() = runTest(dispatcher) {
        coEvery { api.searchBooks(any(), any(), any()) } returns searchResponse(emptyList())

        val vm = searchingViewModel()
        advanceUntilIdle()

        assertTrue("0건이면 안내할 수 있어야 한다", vm.state.value.bookSearchNoResult)
        assertEquals("검색 결과가 없어요. 제목을 다시 확인해 주세요", vm.state.value.bookSearchHint)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `검색 결과가 있으면 드롭다운을 열고 선택하라고 안내한다`() = runTest(dispatcher) {
        coEvery { api.searchBooks(any(), any(), any()) } returns
            searchResponse(listOf(book("해리 포터와 마법사의 돌", "9788983920775")))

        val vm = searchingViewModel()
        advanceUntilIdle()

        assertTrue(vm.state.value.showBookDropdown)
        assertEquals("목록에서 책을 선택해 주세요", vm.state.value.bookSearchHint)

        // 사용자가 드롭다운을 닫아도 안내는 남아야 한다 — 아직 고르지 않았으므로
        vm.onDismissBookDropdown()
        assertFalse(vm.state.value.showBookDropdown)
        assertEquals("목록에서 책을 선택해 주세요", vm.state.value.bookSearchHint)

        // 필드를 다시 누르면 남아 있는 결과로 드롭다운이 다시 열린다
        vm.onBookFieldFocused()
        assertTrue(vm.state.value.showBookDropdown)

        vm.viewModelScope.cancel()
    }

    // reset()이 디바운스 쿼리를 비우지 않으면 StateFlow가 같은 값을 무시해
    // 다이얼로그를 다시 열고 같은 책을 검색할 때 아무 일도 일어나지 않는다
    @Test
    fun `다이얼로그를 닫았다 열고 같은 검색어를 입력하면 다시 검색한다`() = runTest(dispatcher) {
        coEvery { api.searchBooks(any(), any(), any()) } returns
            searchResponse(listOf(book("해리 포터와 마법사의 돌", "9788983920775")))

        val vm = searchingViewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value.showBookDropdown)

        vm.reset()
        advanceUntilIdle()

        vm.onBookSearchQueryChange("해리")
        advanceUntilIdle()

        assertTrue("같은 검색어라도 다시 검색해야 한다", vm.state.value.showBookDropdown)

        vm.viewModelScope.cancel()
    }

    @Test
    fun `책을 선택하면 드롭다운이 닫히고 안내가 사라진다`() = runTest(dispatcher) {
        coEvery { api.searchBooks(any(), any(), any()) } returns
            searchResponse(listOf(book("해리 포터와 마법사의 돌 - 개정판", "9788983920775")))

        val vm = searchingViewModel()
        advanceUntilIdle()
        vm.onBookSelect(book("해리 포터와 마법사의 돌 - 개정판", "9788983920775"))

        assertFalse(vm.state.value.showBookDropdown)
        assertNull(vm.state.value.bookSearchHint)
        assertEquals("9788983920775", vm.state.value.isbn13)

        vm.viewModelScope.cancel()
    }
}
