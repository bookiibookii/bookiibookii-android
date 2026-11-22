package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.GrpApi
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.BookSearchResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import retrofit2.Response

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class GroupBookSelectionTest(private val editor: Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "editor={0}")
        fun cases() = listOf(arrayOf(true), arrayOf(false))
    }

    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<GrpApi>()
    private val first = BookItem("해리 포터 - 개정판", "작가", "", "출판사", "9788983920775", "novel", "소설", "")
    private val second = first.copy(title = "다른 책", isbn13 = "9788983921093")
    private lateinit var vm: ViewModel
    private lateinit var query: (String) -> Unit
    private lateinit var select: (BookItem) -> Unit
    private lateinit var clear: () -> Unit
    private lateinit var search: () -> Unit
    private lateinit var isbn: () -> String?
    private lateinit var title: () -> String

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.grpApi() } returns api
        coEvery { api.searchBooks(any(), any(), any()) } returns Response.success(
            ApiResponse(true, "COMMON200", "성공", BookSearchResponse(listOf(first, second), 1, 2)))
        if (editor) {
            val model = GroupEditorViewModel(SavedStateHandle())
            vm = model
            query = model::onBookSearchQueryChange
            select = model::onBookSelect
            clear = model::onClearBookSearch
            search = model::searchBooks
            isbn = { model.state.value.isbn13 }
            title = { model.state.value.bookSearchQuery }
        } else {
            val model = JoinRequestViewModel()
            vm = model
            query = model::onBookSearchQueryChange
            select = model::onBookSelect
            clear = model::onClearBookSearch
            search = model::searchBooks
            isbn = { model.state.value.isbn13 }
            title = { model.state.value.bookSearchQuery }
        }
    }

    @After fun teardown() {
        vm.viewModelScope.cancel()
        unmockkObject(RetrofitClient)
        Dispatchers.resetMain()
    }

    @Test fun `한글 입력 직후 선택하면 늦은 입력이 선택을 해제하지 않는다`() = runTest(dispatcher) {
        query("해리")
        advanceUntilIdle()
        select(first)
        // 포커스 해제로 IME가 이전 검색어를 다시 전달하는 순서.
        query("해리")
        query("해리 ")
        advanceUntilIdle()
        assertEquals(first.isbn13, isbn())
        assertEquals(first.title, title())
    }

    @Test fun `검색 확정 후 선택해도 같은 제목 콜백으로 선택이 해제되지 않는다`() = runTest(dispatcher) {
        query("해리")
        search()
        advanceUntilIdle()
        select(first)
        query(first.title)
        advanceUntilIdle()
        assertEquals(first.isbn13, isbn())
        assertEquals(first.title, title())
    }

    @Test fun `X 초기화 뒤 다른 검색어로 다른 책을 다시 선택할 수 있다`() = runTest(dispatcher) {
        select(first)
        clear()
        assertNull(isbn())
        assertEquals("", title())
        query("다른")
        advanceUntilIdle()
        assertEquals("다른", title())
        coVerify { api.searchBooks("다른", any(), any()) }
        select(second)
        advanceUntilIdle()
        assertEquals(second.isbn13, isbn())
        assertEquals(second.title, title())
    }
}
