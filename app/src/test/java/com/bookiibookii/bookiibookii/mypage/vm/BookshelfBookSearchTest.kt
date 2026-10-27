package com.bookiibookii.bookiibookii.mypage.vm

import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.GrpApi
import com.bookiibookii.bookiibookii.data.api.MypApi
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.BookSearchResponse
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

/**
 * 마이페이지 인생 책 검색.
 *
 * 그룹 화면과 달리 LiveData를 쓰므로 메인 루퍼가 필요해 Robolectric으로 돌린다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BookshelfBookSearchTest {

    private val dispatcher = StandardTestDispatcher()
    private val grpApi = mockk<GrpApi>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.grpApi() } returns grpApi
        every { RetrofitClient.mypApi() } returns mockk<MypApi>(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        Dispatchers.resetMain()
    }

    private fun searchResponse(books: List<BookItem>) = Response.success(
        ApiResponse(
            isSuccess = true,
            code = "COMMON200",
            message = "성공",
            result = BookSearchResponse(books = books, totalPage = 1, totalResults = books.size),
        )
    )

    private val book = BookItem(
        title = "해리 포터와 마법사의 돌",
        author = "조앤 K. 롤링",
        image = "",
        publisher = "문학수첩",
        isbn13 = "9788983920775",
        category = "novel",
        categoryLabel = "영미소설",
        link = "",
    )

    // 검색 버튼이 별도 코루틴으로 돌면 다이얼로그를 닫아도 취소되지 않아,
    // 늦게 온 응답이 결과를 되살려 다음에 열 때 엉뚱한 목록이 남아 있다
    @Test
    fun `검색을 닫은 뒤 늦게 도착한 결과가 되살아나지 않는다`() = runTest(dispatcher) {
        val late = CompletableDeferred<Response<ApiResponse<BookSearchResponse>>>()
        coEvery { grpApi.searchBooks(any(), any(), any()) } coAnswers { late.await() }

        val vm = BookshelfViewModel()
        vm.onBookSearchQueryChange("해리")
        vm.searchBooks()
        advanceUntilIdle()

        // 다이얼로그 닫기 — 진행 중인 검색도 함께 버려져야 한다
        vm.clearBookSearch()
        advanceUntilIdle()

        late.complete(searchResponse(listOf(book)))
        advanceUntilIdle()

        assertTrue(
            "닫은 검색의 결과가 되살아났다: ${vm.bookSearchState.value}",
            vm.bookSearchState.value is BookSearchState.Idle,
        )

        vm.viewModelScope.cancel()
    }
}
