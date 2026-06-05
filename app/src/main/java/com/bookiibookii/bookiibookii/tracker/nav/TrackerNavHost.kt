package com.bookiibookii.bookiibookii.tracker.nav

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.placesearch.ui.PlaceSearchScreen
import com.bookiibookii.bookiibookii.tracker.model.ReadingCardTarget
import com.bookiibookii.bookiibookii.tracker.ui.comment.TrackerCommentRoute
import com.bookiibookii.bookiibookii.tracker.ui.detail.TrackerDetailRoute
import com.bookiibookii.bookiibookii.tracker.ui.main.TrackerMainRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerBookReviewRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerPartnerReviewRoute

@Composable
fun TrackerNavHost(
    onCreateGroupClick: () -> Unit,
    onNavigateLibraryDetail: (ReadingCardTarget) -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = TrackerDestinations.MAIN,
) {
    val navController = rememberNavController()

    // 바텀 네비 표시는 여기서만 제어
    // 현재 라우트 기준으로만 판단하여 dispose 순서와 무관하게 결정 — MAIN에서만 표시
    val context = LocalContext.current
    val currentRoute by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentRoute) {
        val route = currentRoute?.destination?.route ?: return@LaunchedEffect
        val bottomNav = (context as? Activity)?.findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (route == TrackerDestinations.MAIN) View.VISIBLE else View.GONE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(TrackerDestinations.MAIN) { entry ->
            val selectedPlace by entry.savedStateHandle
                .getStateFlow<PlaceSearchResult?>(TrackerDestinations.RESULT_SELECTED_PLACE, null)
                .collectAsStateWithLifecycle()
            TrackerMainRoute(
                onProfileClick = {},
                onAlertClick = {},
                onCreateGroupClick = onCreateGroupClick,
                onCardClick = { groupId ->
                    navController.navigate(TrackerDestinations.detail(groupId))
                },
                onNavigateBookReview = { groupId, edit ->
                    navController.navigate(TrackerDestinations.bookReview(groupId, edit))
                },
                onNavigatePartnerReview = { groupId ->
                    navController.navigate(TrackerDestinations.partnerReview(groupId))
                },
                onNavigatePlaceSearch = {
                    navController.navigate(TrackerDestinations.PLACE_SEARCH)
                },
                onNavigateLibraryDetail = onNavigateLibraryDetail,
                selectedPlace = selectedPlace,
                onPlaceConsumed = {
                    entry.savedStateHandle[TrackerDestinations.RESULT_SELECTED_PLACE] = null
                },
            )
        }
        composable(
            route = TrackerDestinations.DETAIL_ROUTE,
            arguments = listOf(
                navArgument(TrackerDestinations.DETAIL_ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getLong(TrackerDestinations.DETAIL_ARG_GROUP_ID) ?: return@composable
            val selectedPlace by backStackEntry.savedStateHandle
                .getStateFlow<PlaceSearchResult?>(TrackerDestinations.RESULT_SELECTED_PLACE, null)
                .collectAsStateWithLifecycle()
            TrackerDetailRoute(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onNavigateBookReview = { edit ->
                    navController.navigate(TrackerDestinations.bookReview(groupId, edit))
                },
                onNavigatePartnerReview = {
                    navController.navigate(TrackerDestinations.partnerReview(groupId))
                },
                onNavigateComment = { title ->
                    navController.navigate(TrackerDestinations.comment(groupId, title))
                },
                onNavigatePlaceSearch = {
                    navController.navigate(TrackerDestinations.PLACE_SEARCH)
                },
                onNavigateLibraryDetail = onNavigateLibraryDetail,
                selectedPlace = selectedPlace,
                onPlaceConsumed = {
                    backStackEntry.savedStateHandle[TrackerDestinations.RESULT_SELECTED_PLACE] = null
                },
            )
        }
        composable(
            route = TrackerDestinations.BOOK_REVIEW_ROUTE,
            arguments = listOf(
                navArgument(TrackerDestinations.BOOK_REVIEW_ARG_GROUP_ID) {
                    type = NavType.LongType
                },
                navArgument(TrackerDestinations.BOOK_REVIEW_ARG_EDIT) {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getLong(TrackerDestinations.BOOK_REVIEW_ARG_GROUP_ID) ?: return@composable
            val isEdit = backStackEntry.arguments
                ?.getBoolean(TrackerDestinations.BOOK_REVIEW_ARG_EDIT) ?: false
            TrackerBookReviewRoute(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                isEdit = isEdit,
            )
        }
        composable(
            route = TrackerDestinations.PARTNER_REVIEW_ROUTE,
            arguments = listOf(
                navArgument(TrackerDestinations.PARTNER_REVIEW_ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getLong(TrackerDestinations.PARTNER_REVIEW_ARG_GROUP_ID) ?: return@composable
            TrackerPartnerReviewRoute(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(
            route = TrackerDestinations.COMMENT_ROUTE,
            arguments = listOf(
                navArgument(TrackerDestinations.COMMENT_ARG_GROUP_ID) {
                    type = NavType.LongType
                },
                navArgument(TrackerDestinations.COMMENT_ARG_TITLE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getLong(TrackerDestinations.COMMENT_ARG_GROUP_ID) ?: return@composable
            val title = backStackEntry.arguments
                ?.getString(TrackerDestinations.COMMENT_ARG_TITLE).orEmpty()
            TrackerCommentRoute(
                groupId = groupId,
                title = title,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(TrackerDestinations.PLACE_SEARCH) {
            PlaceSearchScreen(
                onBackClick = { navController.popBackStack() },
                onPlaceClick = { result ->
                    // 선택 결과를 이전 화면(약속 다이얼로그)으로 반환하고 복귀
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(TrackerDestinations.RESULT_SELECTED_PLACE, result)
                    navController.popBackStack()
                },
            )
        }
    }
}
