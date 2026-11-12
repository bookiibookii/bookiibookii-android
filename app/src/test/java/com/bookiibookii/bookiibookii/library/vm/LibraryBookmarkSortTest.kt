package com.bookiibookii.bookiibookii.library.vm

import com.bookiibookii.bookiibookii.data.api.LibApi
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * 북마크 정렬 유지 (#505).
 *
 * 화면은 복귀할 때마다 다시 조회한다. 과거순을 고른 뒤 돌아와도 과거순이어야 한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryBookmarkSortTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<LibApi>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.libApi() } returns api
        coEvery { api.getBookmarkedCards() } returns Response.success(
            ApiResponse(isSuccess = true, code = "COMMON200", message = "성공", result = listOf(card(2), card(3), card(1))),
        )
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        Dispatchers.resetMain()
    }

    private fun card(id: Int) = MemberCardResponseDTO(
        cardId = id,
        memberBookId = 10,
        cardType = "TEXT",
        page = id,
        totalPages = 100,
        memo = null,
        quotation = "인용 $id",
        cardImage = null,
        createdAt = "2026-11-0${id}T10:00:00",
        completedAt = null,
        genre = null,
        bookTitle = "책",
        isMine = true,
        isBookmarked = true,
        creatorName = "나",
        creatorProfileImageUrl = null,
        reactionCounts = emptyList(),
        myReactions = emptyList(),
    )

    private fun LibraryBookmarkViewModel.cardIds() = uiState.value.cards.map { it.cardId }

    @Test
    fun `처음 조회하면 최신순으로 보여준다`() = runTest(dispatcher) {
        val vm = LibraryBookmarkViewModel()

        vm.fetchBookmarkedCards()
        advanceUntilIdle()

        assertEquals(listOf(3L, 2L, 1L), vm.cardIds())
    }

    @Test
    fun `과거순을 고른 뒤 다시 조회해도 과거순을 유지한다`() = runTest(dispatcher) {
        val vm = LibraryBookmarkViewModel()
        vm.fetchBookmarkedCards()
        advanceUntilIdle()

        vm.sortByLatest(false)
        vm.fetchBookmarkedCards() // 카드 상세에서 돌아온 ON_RESUME
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLatest)
        assertEquals(listOf(1L, 2L, 3L), vm.cardIds())
    }
}
