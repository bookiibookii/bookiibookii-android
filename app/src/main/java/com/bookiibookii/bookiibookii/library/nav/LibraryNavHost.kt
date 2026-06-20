package com.bookiibookii.bookiibookii.library.nav

import android.app.Activity
import android.view.View
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.GroupReviewRoute
import com.bookiibookii.bookiibookii.library.ui.LibraryAddCardRoute
import com.bookiibookii.bookiibookii.library.ui.LibraryBookmarkRoute
import com.bookiibookii.bookiibookii.library.ui.LibraryDetailRoute
import com.bookiibookii.bookiibookii.library.ui.LibraryMainRoute
import com.bookiibookii.bookiibookii.library.ui.ReadingCardDetailRoute
import com.bookiibookii.bookiibookii.library.ui.ReviewEditRoute
import com.google.gson.Gson

// 트래커(tracker) 모듈과 동일한 패턴: 단일 Fragment(LibraryFragment) 안에서
// 7개 화면 전환을 전부 Compose Navigation으로 처리한다.
@Composable
fun LibraryNavHost(
    onProfileClick: () -> Unit = {},
    // 딥링크(newInstanceAtDetail 등)로 MAIN이 아닌 화면이 시작점일 때, 내부적으로 더 pop할
    // 곳이 없으면 이 콜백으로 Fragment 자체를 종료한다(그렇지 않으면 뒤로가기가 조용히 무시됨).
    onExitLibrary: () -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = LibraryDestinations.MAIN,
) {
    val navController = rememberNavController()

    // 내부 스택에 더 갈 곳이 있으면 popBackStack, 없으면(딥링크 시작점) Fragment 종료
    val popOrExit: () -> Unit = {
        if (!navController.popBackStack()) onExitLibrary()
    }

    // 바텀 네비 표시는 여기서만 제어 — MAIN에서만 표시, 그 외 화면은 숨김
    // (구 BaseLibraryFragment.onResume/onDetach가 하던 일을 대체)
    val context = LocalContext.current
    val currentRoute by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentRoute) {
        val route = currentRoute?.destination?.route ?: return@LaunchedEffect
        val bottomNav = (context as? Activity)?.findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (route == LibraryDestinations.MAIN) View.VISIBLE else View.GONE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // 화면 전환 애니메이션 제거(기본 크로스페이드 시 이전 화면이 잔상처럼 겹쳐 보이는 현상 방지)
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(LibraryDestinations.MAIN) {
            LibraryMainRoute(
                onProfileClick = onProfileClick,
                onBookmarkClick = { navController.navigate(LibraryDestinations.BOOKMARK) },
                onBookClick = { book ->
                    navController.navigate(
                        LibraryDestinations.detail(
                            groupId = book.groupId,
                            memberBookId = book.memberBookId,
                            groupName = book.groupName,
                            bookTitle = book.title,
                            author = book.author,
                            genre = book.genre,
                            coverUrl = book.coverUrl ?: "",
                            startDate = book.startDate,
                            endDate = book.endDate ?: "",
                            completedAt = book.completedAt ?: "",
                            rating = book.rating?.toDouble() ?: 0.0,
                            isDone = book.rating != null,
                            progressRate = ((book.progress ?: 0f) * 100).toInt(),
                            totalPages = book.totalPages ?: 0,
                        ),
                    )
                },
            )
        }

        composable(
            route = LibraryDestinations.DETAIL_ROUTE,
            arguments = listOf(
                navArgument(LibraryDestinations.DETAIL_ARG_GROUP_ID) { type = NavType.IntType },
                navArgument(LibraryDestinations.DETAIL_ARG_MEMBER_BOOK_ID) { type = NavType.IntType },
                navArgument(LibraryDestinations.DETAIL_ARG_GROUP_NAME) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_BOOK_TITLE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_AUTHOR) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_GENRE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_COVER_URL) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_START_DATE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_END_DATE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_COMPLETED_AT) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.DETAIL_ARG_RATING) { type = NavType.StringType; defaultValue = "0.0" },
                navArgument(LibraryDestinations.DETAIL_ARG_IS_DONE) { type = NavType.BoolType; defaultValue = false },
                navArgument(LibraryDestinations.DETAIL_ARG_PROGRESS_RATE) { type = NavType.IntType; defaultValue = 0 },
                navArgument(LibraryDestinations.DETAIL_ARG_TOTAL_PAGES) { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val groupId = args?.getInt(LibraryDestinations.DETAIL_ARG_GROUP_ID) ?: -1
            val memberBookId = args?.getInt(LibraryDestinations.DETAIL_ARG_MEMBER_BOOK_ID) ?: -1
            val bookTitle = args?.getString(LibraryDestinations.DETAIL_ARG_BOOK_TITLE).orEmpty()
            val totalPages = args?.getInt(LibraryDestinations.DETAIL_ARG_TOTAL_PAGES) ?: 0
            LibraryDetailRoute(
                groupId = groupId,
                memberBookId = memberBookId,
                groupName = args?.getString(LibraryDestinations.DETAIL_ARG_GROUP_NAME).orEmpty(),
                bookTitle = bookTitle,
                author = args?.getString(LibraryDestinations.DETAIL_ARG_AUTHOR).orEmpty(),
                genre = args?.getString(LibraryDestinations.DETAIL_ARG_GENRE).orEmpty(),
                coverUrl = args?.getString(LibraryDestinations.DETAIL_ARG_COVER_URL).orEmpty(),
                startDate = args?.getString(LibraryDestinations.DETAIL_ARG_START_DATE).orEmpty(),
                endDate = args?.getString(LibraryDestinations.DETAIL_ARG_END_DATE).orEmpty(),
                completedAt = args?.getString(LibraryDestinations.DETAIL_ARG_COMPLETED_AT).orEmpty(),
                rating = args?.getString(LibraryDestinations.DETAIL_ARG_RATING)?.toDoubleOrNull() ?: 0.0,
                isDone = args?.getBoolean(LibraryDestinations.DETAIL_ARG_IS_DONE) ?: false,
                progressRate = args?.getInt(LibraryDestinations.DETAIL_ARG_PROGRESS_RATE) ?: 0,
                totalPages = totalPages,
                onBackClick = popOrExit,
                onAddTextCard = { navController.navigate(LibraryDestinations.addCard(AddCardMode.TEXT, memberBookId, bookTitle, totalPages)) },
                onAddPhotoCard = { navController.navigate(LibraryDestinations.addCard(AddCardMode.PHOTO, memberBookId, bookTitle, totalPages)) },
                onCardClick = { initialIndex, sortedCards, sortByLatest ->
                    navController.navigate(LibraryDestinations.cardDetail(initialIndex, sortByLatest, Gson().toJson(sortedCards)))
                },
                onReviewClick = {
                    navController.navigate(
                        LibraryDestinations.groupReview(
                            groupId = groupId,
                            groupName = args?.getString(LibraryDestinations.DETAIL_ARG_GROUP_NAME).orEmpty(),
                            bookTitle = bookTitle,
                            startDate = args?.getString(LibraryDestinations.DETAIL_ARG_START_DATE).orEmpty(),
                            endDate = args?.getString(LibraryDestinations.DETAIL_ARG_END_DATE).orEmpty(),
                        ),
                    )
                },
            )
        }

        composable(
            route = LibraryDestinations.ADD_CARD_ROUTE,
            arguments = listOf(
                navArgument(LibraryDestinations.ADD_CARD_ARG_MODE) { type = NavType.StringType },
                navArgument(LibraryDestinations.ADD_CARD_ARG_MEMBER_BOOK_ID) { type = NavType.IntType },
                navArgument(LibraryDestinations.ADD_CARD_ARG_CARD_ID) { type = NavType.LongType; defaultValue = -1L },
                navArgument(LibraryDestinations.ADD_CARD_ARG_QUOTE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_PAGE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_MEMO) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_IMAGE_URL) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_S3KEY) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_BOOK_TITLE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.ADD_CARD_ARG_TOTAL_PAGES) { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val mode = args?.getString(LibraryDestinations.ADD_CARD_ARG_MODE)?.let { AddCardMode.valueOf(it) } ?: AddCardMode.TEXT
            LibraryAddCardRoute(
                mode = mode,
                memberBookId = args?.getInt(LibraryDestinations.ADD_CARD_ARG_MEMBER_BOOK_ID) ?: -1,
                cardId = args?.getLong(LibraryDestinations.ADD_CARD_ARG_CARD_ID) ?: -1L,
                initialQuote = args?.getString(LibraryDestinations.ADD_CARD_ARG_QUOTE).orEmpty(),
                initialPage = args?.getString(LibraryDestinations.ADD_CARD_ARG_PAGE).orEmpty(),
                initialMemo = args?.getString(LibraryDestinations.ADD_CARD_ARG_MEMO).orEmpty(),
                initialImageUrl = args?.getString(LibraryDestinations.ADD_CARD_ARG_IMAGE_URL)?.ifBlank { null },
                initialS3Key = args?.getString(LibraryDestinations.ADD_CARD_ARG_S3KEY)?.ifBlank { null },
                bookTitle = args?.getString(LibraryDestinations.ADD_CARD_ARG_BOOK_TITLE).orEmpty(),
                totalPages = args?.getInt(LibraryDestinations.ADD_CARD_ARG_TOTAL_PAGES) ?: 0,
                onBackClick = popOrExit,
                onSaved = { isEdit ->
                    // 수정은 카드 상세(중간 화면)까지 같이 닫고 목록으로, 등록은 작성 화면만 닫고 상세로 복귀
                    navController.popBackStack()
                    if (isEdit) navController.popBackStack()
                },
            )
        }

        composable(
            route = LibraryDestinations.CARD_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(LibraryDestinations.CARD_DETAIL_ARG_INDEX) { type = NavType.IntType },
                navArgument(LibraryDestinations.CARD_DETAIL_ARG_SORT) { type = NavType.BoolType },
                navArgument(LibraryDestinations.CARD_DETAIL_ARG_CARDS) { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            ReadingCardDetailRoute(
                initialIndex = args?.getInt(LibraryDestinations.CARD_DETAIL_ARG_INDEX) ?: 0,
                sortByLatest = args?.getBoolean(LibraryDestinations.CARD_DETAIL_ARG_SORT) ?: true,
                cardsJson = args?.getString(LibraryDestinations.CARD_DETAIL_ARG_CARDS).orEmpty(),
                onBackClick = popOrExit,
                onEditCard = { card -> navController.navigate(LibraryDestinations.addCardEdit(card)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable(
            route = LibraryDestinations.GROUP_REVIEW_ROUTE,
            arguments = listOf(
                navArgument(LibraryDestinations.GROUP_REVIEW_ARG_GROUP_ID) { type = NavType.IntType },
                navArgument(LibraryDestinations.GROUP_REVIEW_ARG_GROUP_NAME) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.GROUP_REVIEW_ARG_BOOK_TITLE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.GROUP_REVIEW_ARG_START_DATE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.GROUP_REVIEW_ARG_END_DATE) { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val groupId = args?.getInt(LibraryDestinations.GROUP_REVIEW_ARG_GROUP_ID) ?: -1
            GroupReviewRoute(
                groupId = groupId,
                groupName = args?.getString(LibraryDestinations.GROUP_REVIEW_ARG_GROUP_NAME).orEmpty(),
                bookTitle = args?.getString(LibraryDestinations.GROUP_REVIEW_ARG_BOOK_TITLE).orEmpty(),
                startDate = args?.getString(LibraryDestinations.GROUP_REVIEW_ARG_START_DATE).orEmpty(),
                endDate = args?.getString(LibraryDestinations.GROUP_REVIEW_ARG_END_DATE).orEmpty(),
                onBackClick = popOrExit,
                onNavigateReviewEdit = { groupName, dateRange, partnerName ->
                    navController.navigate(LibraryDestinations.reviewEdit(groupId, groupName, dateRange, partnerName))
                },
            )
        }

        composable(
            route = LibraryDestinations.REVIEW_EDIT_ROUTE,
            arguments = listOf(
                navArgument(LibraryDestinations.REVIEW_EDIT_ARG_GROUP_ID) { type = NavType.IntType },
                navArgument(LibraryDestinations.REVIEW_EDIT_ARG_GROUP_NAME) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.REVIEW_EDIT_ARG_DATE_RANGE) { type = NavType.StringType; defaultValue = "" },
                navArgument(LibraryDestinations.REVIEW_EDIT_ARG_PARTNER_NAME) { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            ReviewEditRoute(
                groupId = args?.getInt(LibraryDestinations.REVIEW_EDIT_ARG_GROUP_ID) ?: -1,
                groupName = args?.getString(LibraryDestinations.REVIEW_EDIT_ARG_GROUP_NAME).orEmpty(),
                dateRange = args?.getString(LibraryDestinations.REVIEW_EDIT_ARG_DATE_RANGE).orEmpty(),
                partnerName = args?.getString(LibraryDestinations.REVIEW_EDIT_ARG_PARTNER_NAME).orEmpty(),
                onBackClick = popOrExit,
            )
        }

        composable(LibraryDestinations.BOOKMARK) {
            LibraryBookmarkRoute(
                onBackClick = popOrExit,
                onCardClick = { initialIndex, sortByLatest, cards ->
                    navController.navigate(LibraryDestinations.cardDetail(initialIndex, sortByLatest, Gson().toJson(cards)))
                },
            )
        }
    }
}
