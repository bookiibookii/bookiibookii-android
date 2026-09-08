package com.bookiibookii.bookiibookii.tracker.nav

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.placesearch.ui.PlaceSearchScreen
import com.bookiibookii.bookiibookii.tracker.ui.comment.TrackerCommentRoute
import com.bookiibookii.bookiibookii.tracker.ui.detail.TrackerDetailRoute
import com.bookiibookii.bookiibookii.tracker.ui.main.TrackerMainRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerBookReviewRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerPartnerReviewRoute
import com.bookiibookii.bookiibookii.ui.nav.AppNavigator
import com.bookiibookii.bookiibookii.ui.nav.Graph

fun NavGraphBuilder.trackerGraph(
    navController: NavController,
    navigator: AppNavigator,
) {
    navigation(route = Graph.TRACKER, startDestination = TrackerDestinations.MAIN) {
        composable(TrackerDestinations.MAIN) { entry ->
            val selectedPlace by entry.savedStateHandle
                .getStateFlow<PlaceSearchResult?>(TrackerDestinations.RESULT_SELECTED_PLACE, null)
                .collectAsStateWithLifecycle()
            TrackerMainRoute(
                onProfileClick = { navigator.toProfile() },
                onAlertClick = navigator::toNotification,
                onCreateGroupClick = navigator::toGroupEditor,
                onCardClick = { groupId ->
                    navController.navigate(TrackerDestinations.detail(groupId))
                },
                onNavigateBookReview = { groupId, edit ->
                    navController.navigate(TrackerDestinations.bookReview(groupId, edit))
                },
                onNavigatePartnerReview = { groupId ->
                    navController.navigate(TrackerDestinations.partnerReview(groupId))
                },
                onNavigateComment = { groupId, title ->
                    navController.navigate(TrackerDestinations.comment(groupId, title))
                },
                onNavigatePlaceSearch = {
                    navController.navigate(TrackerDestinations.PLACE_SEARCH)
                },
                onNavigateLibraryDetail = navigator::toLibraryDetail,
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
                onBackClick = navigator::back,
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
                onNavigateLibraryDetail = navigator::toLibraryDetail,
                onNavigateLibrary = navigator::toLibraryTab,
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
                onBackClick = navigator::back,
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
                onBackClick = navigator::back,
                // 등록 완료 시 상세가 아니라 메인까지 되돌아감
                onSubmitDone = {
                    navController.popBackStack(TrackerDestinations.MAIN, inclusive = false)
                },
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
                onBackClick = navigator::back,
            )
        }
        composable(TrackerDestinations.PLACE_SEARCH) {
            PlaceSearchScreen(
                onBackClick = navigator::back,
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
