package com.bookiibookii.bookiibookii.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.*
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.group.*
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import com.bookiibookii.bookiibookii.group.vm.GroupEditorViewModel
import com.bookiibookii.bookiibookii.group.vm.JoinRequestViewModel
import com.bookiibookii.bookiibookii.mypage.vm.BookshelfViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnboardingSubmitState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BookRegistrationTest {
    private val dispatcher = StandardTestDispatcher()
    private val groups = mockk<GrpApi>()
    private val shelf = mockk<MypApi>(relaxed = true)
    private val user = mockk<UserApi>()
    private val book = BookItem("책", "작가", "", "출판사", "9788983920775", "novel", "소설", "")
    private val errors = listOf(422 to "BOOK422_1", 422 to "BOOK422_3", 400 to "BOOK400_1",
        400 to "BOOK400_2", 404 to "BOOK404_1", 503 to "BOOK503_1")

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.grpApi() } returns groups
        every { RetrofitClient.mypApi() } returns shelf
        every { RetrofitClient.userApi() } returns user
        every { RetrofitClient.locationApi() } returns mockk(relaxed = true)
    }
    @After fun teardown() {
        unmockkObject(RetrofitClient)
        Dispatchers.resetMain()
    }
    private fun <T> failure(status: Int, code: String): Response<ApiResponse<T>> = Response.error(status,
        """{"isSuccess":false,"code":"$code","message":"사유 $code","result":null}""".toResponseBody("application/json".toMediaType()))
    private fun success() = Response.success(ApiResponse(true, "COMMON200", "성공", ""))

    @Test fun `인생책 등록 교체는 오류 후 선택 검색을 보존하고 수동 재시도만 한다`() = runTest(dispatcher) {
        for (replace in listOf(false, true)) {
            val vm = BookshelfViewModel()
            coEvery { groups.searchBooks(any(), any(), any()) } returns Response.success(
                ApiResponse(true, "COMMON200", "성공", BookSearchResponse(listOf(book), 1, 1)))
            vm.onBookSearchQueryChange("소설")
            vm.searchBooks()
            advanceUntilIdle()
            for ((status, code) in errors) {
                val pending = CompletableDeferred<Response<ApiResponse<String>>>()
                coEvery { shelf.addFavoriteBook(any()) } coAnswers { pending.await() }
                coEvery { shelf.replaceFavoriteBook(any(), any()) } coAnswers { pending.await() }
                clearMocks(shelf, answers = false)
                repeat(2) { if (replace) vm.replaceFavoriteBook(7, book.isbn13) else vm.addFavoriteBook(book.isbn13) }
                assertTrue(vm.favoriteSubmitState.value!!.loading)
                runCurrent()
                if (replace) coVerify(exactly = 1) { shelf.replaceFavoriteBook(7, any()) }
                else coVerify(exactly = 1) { shelf.addFavoriteBook(any()) }
                pending.complete(failure(status, code))
                advanceUntilIdle()
                assertFalse(vm.favoriteSubmitState.value!!.loading)
                assertEquals(code, vm.favoriteSubmitState.value!!.failure!!.code)
                assertEquals(status, vm.favoriteSubmitState.value!!.failure!!.status)
                assertEquals(0, vm.favoriteSubmitState.value!!.completed)
                assertEquals(listOf(book), (vm.bookSearchState.value as BookSearchState.Success).books)
                if (replace) coVerify(exactly = 1) { shelf.replaceFavoriteBook(7, any()) }
                else coVerify(exactly = 1) { shelf.addFavoriteBook(any()) }
            }
            coEvery { shelf.addFavoriteBook(any()) } returns success()
            coEvery { shelf.replaceFavoriteBook(any(), any()) } returns success()
            if (replace) vm.replaceFavoriteBook(7, "9788936434267") else vm.addFavoriteBook("9788936434267")
            advanceUntilIdle()
            assertEquals(1, vm.favoriteSubmitState.value!!.completed)
            assertNull(vm.favoriteSubmitState.value!!.failure)
            vm.viewModelScope.cancel()
        }
    }

    @Test fun `온보딩은 0권을 차단하고 실패 후 1권 입력을 보존해 재시도한다`() = runTest(dispatcher) {
        val vm = OnbViewModel()
        vm.submitOnboarding()
        advanceUntilIdle()
        coVerify(exactly = 0) { user.postOnboarding(any()) }
        assertTrue(vm.onboardingSubmitState.value is OnboardingSubmitState.Error)
        vm.setLifeBook(0, book)
        vm.setSelfIntro("보존할 소개")
        for ((status, code) in errors) {
            val pending = CompletableDeferred<Response<ApiResponse<String>>>()
            coEvery { user.postOnboarding(any()) } coAnswers { pending.await() }
            clearMocks(user, answers = false)
            vm.submitOnboarding()
            vm.submitOnboarding()
            assertEquals(OnboardingSubmitState.Loading, vm.onboardingSubmitState.value)
            runCurrent()
            coVerify(exactly = 1) { user.postOnboarding(match { it.userBooks.size == 1 }) }
            pending.complete(failure(status, code))
            advanceUntilIdle()
            assertEquals("사유 $code", (vm.onboardingSubmitState.value as OnboardingSubmitState.Error).message)
            assertEquals(book, vm.state.value!!.lifeBooks[0])
            assertEquals("보존할 소개", vm.state.value!!.selfIntro)
            coVerify(exactly = 1) { user.postOnboarding(any()) }
        }
        vm.setLifeBook(0, book.copy(isbn13 = "9788936434267"))
        coEvery { user.postOnboarding(any()) } returns success()
        vm.submitOnboarding()
        advanceUntilIdle()
        assertEquals(OnboardingSubmitState.Success, vm.onboardingSubmitState.value)
        vm.viewModelScope.cancel()
    }

    @Test fun `그룹 신청은 연속 제출을 차단하고 오류 후 신청문과 책을 보존한다`() = runTest(dispatcher) {
        val vm = JoinRequestViewModel()
        val events = mutableListOf<JoinRequestViewModel.Event>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.collect { events += it } }
        vm.onBookSelect(book)
        vm.onApplyMsgChange("함께 읽어요")
        for ((status, code) in errors) {
            val pending = CompletableDeferred<Response<ApiResponse<GroupApplyResponse>>>()
            coEvery { groups.applyGroup(any(), any()) } coAnswers { pending.await() }
            clearMocks(groups, answers = false)
            vm.apply(1)
            vm.apply(1)
            assertTrue(vm.state.value.submitting)
            runCurrent()
            coVerify(exactly = 1) { groups.applyGroup(1, any()) }
            pending.complete(failure(status, code))
            advanceUntilIdle()
            assertFalse(vm.state.value.submitting)
            assertEquals(book.isbn13, vm.state.value.isbn13)
            assertEquals("함께 읽어요", vm.state.value.applyMsg)
            assertEquals("사유 $code", (events.last() as JoinRequestViewModel.Event.ShowError).message)
            assertFalse(events.any { it is JoinRequestViewModel.Event.Applied })
        }
        coEvery { groups.applyGroup(any(), any()) } returns Response.success(ApiResponse(true, "COMMON200", "성공", null))
        vm.onBookSelect(book.copy(isbn13 = "9788936434267"))
        vm.apply(1)
        advanceUntilIdle()
        assertTrue(events.last() is JoinRequestViewModel.Event.Applied)
        vm.viewModelScope.cancel()
    }

    @Test fun `그룹 생성은 오류 후 입력을 보존하며 재선택 후 성공할 수 있다`() = runTest(dispatcher) {
        val vm = GroupEditorViewModel(SavedStateHandle())
        val events = mutableListOf<GroupEditorViewModel.Event>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.collect { events += it } }
        vm.onTradeTypeSelect(ExchangeType.DIRECT)
        advanceUntilIdle()
        vm.onPlaceSelect(1)
        vm.onBookSelect(book)
        vm.onGroupNameChange("독서 모임")
        vm.onRuleStyleSelect(ReadingStyle.ANY)
        for ((status, code) in errors) {
            val pending = CompletableDeferred<Response<ApiResponse<GroupCreateResponse>>>()
            coEvery { groups.createGroup(any()) } coAnswers { pending.await() }
            clearMocks(groups, answers = false)
            vm.submit()
            vm.submit()
            assertTrue(vm.state.value.submitting)
            runCurrent()
            coVerify(exactly = 1) { groups.createGroup(any()) }
            pending.complete(failure(status, code))
            advanceUntilIdle()
            assertFalse(vm.state.value.submitting)
            assertEquals(book.isbn13, vm.state.value.isbn13)
            assertEquals("독서 모임", vm.state.value.groupName)
            assertEquals("사유 $code", (events.last() as GroupEditorViewModel.Event.ShowError).message)
            assertFalse(events.any { it is GroupEditorViewModel.Event.Created })
        }
        coEvery { groups.createGroup(any()) } returns Response.success(ApiResponse(true, "COMMON200", "성공", null))
        vm.onBookSelect(book.copy(isbn13 = "9788936434267"))
        vm.submit()
        advanceUntilIdle()
        assertTrue(events.last() is GroupEditorViewModel.Event.Created)
        vm.viewModelScope.cancel()
    }

    @Test fun `요청 취소는 등록 실패 안내를 만들지 않고 로딩을 해제한다`() = runTest(dispatcher) {
        coEvery { shelf.addFavoriteBook(any()) } coAnswers { awaitCancellation() }
        coEvery { user.postOnboarding(any()) } coAnswers { awaitCancellation() }
        coEvery { groups.applyGroup(any(), any()) } coAnswers { awaitCancellation() }
        val favoriteVm = BookshelfViewModel()
        val onboardingVm = OnbViewModel()
        val applyVm = JoinRequestViewModel()
        favoriteVm.addFavoriteBook(book.isbn13)
        onboardingVm.setLifeBook(0, book)
        onboardingVm.submitOnboarding()
        applyVm.onBookSelect(book)
        applyVm.onApplyMsgChange("신청")
        applyVm.apply(1)
        runCurrent()
        favoriteVm.viewModelScope.cancel()
        onboardingVm.viewModelScope.cancel()
        applyVm.viewModelScope.cancel()
        advanceUntilIdle()
        assertFalse(favoriteVm.favoriteSubmitState.value!!.loading)
        assertNull(favoriteVm.favoriteSubmitState.value!!.failure)
        assertEquals(OnboardingSubmitState.Idle, onboardingVm.onboardingSubmitState.value)
        assertFalse(applyVm.state.value.submitting)
    }

    @Test fun `그룹과 온보딩 검색은 빈 결과와 오류 및 잘못된 성공 응답을 구분한다`() = runTest(dispatcher) {
        val editor = GroupEditorViewModel(SavedStateHandle())
        val apply = JoinRequestViewModel()
        val onboarding = OnbViewModel()
        val empty = Response.success(ApiResponse(true, "COMMON200", "성공", BookSearchResponse(emptyList(), 0, 0)))
        val broken = Response.success(ApiResponse<BookSearchResponse>(true, "COMMON200", "성공", null))
        for (response in listOf(empty, failure(400, "BOOK400_3"), failure(503, "BOOK503_1"), broken)) {
            coEvery { groups.searchBooks(any(), any(), any()) } returns response
            // Each consumer needs its own non-success body (ResponseBody is one-shot).
            coEvery { groups.searchBooks(any(), any(), any()) } coAnswers {
                if (response.isSuccessful) response
                else failure(response.code(), if (response.code() == 400) "BOOK400_3" else "BOOK503_1")
            }
            editor.onBookSearchQueryChange("검색어")
            apply.onBookSearchQueryChange("검색어")
            onboarding.onBookSearchQueryChange("검색어")
            editor.searchBooks()
            apply.searchBooks()
            onboarding.searchBooks()
            advanceUntilIdle()
            if (response === empty) {
                assertTrue(editor.state.value.bookSearchNoResult)
                assertTrue(apply.state.value.bookSearchNoResult)
                assertTrue((onboarding.bookSearchState.value as BookSearchState.Success).books.isEmpty())
            } else {
                assertFalse(editor.state.value.bookSearchNoResult)
                assertFalse(apply.state.value.bookSearchNoResult)
                assertNotNull(editor.state.value.bookSearchError)
                assertNotNull(apply.state.value.bookSearchError)
                assertTrue(onboarding.bookSearchState.value is BookSearchState.Error)
                assertNotEquals("성공", (onboarding.bookSearchState.value as BookSearchState.Error).message)
            }
        }
        editor.viewModelScope.cancel()
        apply.viewModelScope.cancel()
        onboarding.viewModelScope.cancel()
    }

    @Test fun `검색 장애와 정상 빈 결과는 구분된다`() = runTest(dispatcher) {
        val vm = BookshelfViewModel()
        coEvery { groups.searchBooks(any(), any(), any()) } returns failure(503, "BOOK503_1")
        vm.onBookSearchQueryChange("검색어")
        vm.searchBooks()
        advanceUntilIdle()
        assertEquals("사유 BOOK503_1", (vm.bookSearchState.value as BookSearchState.Error).message)
        coEvery { groups.searchBooks(any(), any(), any()) } returns Response.success(
            ApiResponse(true, "COMMON200", "성공", BookSearchResponse(emptyList(), 0, 0)))
        vm.searchBooks()
        advanceUntilIdle()
        assertTrue((vm.bookSearchState.value as BookSearchState.Success).books.isEmpty())
        vm.viewModelScope.cancel()
    }
}
