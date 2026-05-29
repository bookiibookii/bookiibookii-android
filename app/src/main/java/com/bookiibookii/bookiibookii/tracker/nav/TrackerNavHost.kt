package com.bookiibookii.bookiibookii.tracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.tracker.ui.detail.TrackerDetailRoute
import com.bookiibookii.bookiibookii.tracker.ui.main.TrackerMainRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerBookReviewRoute
import com.bookiibookii.bookiibookii.tracker.ui.review.TrackerPartnerReviewRoute

@Composable
fun TrackerNavHost(
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = TrackerDestinations.MAIN,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(TrackerDestinations.MAIN) {
            TrackerMainRoute(
                onProfileClick = {},
                onAlertClick = {},
                onCreateGroupClick = onCreateGroupClick,
                onCardClick = { groupId ->
                    navController.navigate(TrackerDestinations.detail(groupId))
                },
                onNavigateBookReview = { groupId ->
                    navController.navigate(TrackerDestinations.bookReview(groupId))
                },
                onNavigatePartnerReview = { groupId ->
                    navController.navigate(TrackerDestinations.partnerReview(groupId))
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
            TrackerDetailRoute(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onNavigateBookReview = {
                    navController.navigate(TrackerDestinations.bookReview(groupId))
                },
                onNavigatePartnerReview = {
                    navController.navigate(TrackerDestinations.partnerReview(groupId))
                },
            )
        }
        composable(
            route = TrackerDestinations.BOOK_REVIEW_ROUTE,
            arguments = listOf(
                navArgument(TrackerDestinations.BOOK_REVIEW_ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getLong(TrackerDestinations.BOOK_REVIEW_ARG_GROUP_ID) ?: return@composable
            TrackerBookReviewRoute(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
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
    }
}
