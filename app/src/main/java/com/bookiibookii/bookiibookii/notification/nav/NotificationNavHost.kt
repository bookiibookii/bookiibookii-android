package com.bookiibookii.bookiibookii.notification.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bookiibookii.bookiibookii.notification.ui.KeywordSettingRoute
import com.bookiibookii.bookiibookii.notification.ui.NotificationRoute
import com.bookiibookii.bookiibookii.ui.nav.AppNavigator
import com.bookiibookii.bookiibookii.ui.nav.Graph

fun NavGraphBuilder.notificationGraph(
    navController: NavController,
    navigator: AppNavigator,
    onRedirect: (NotificationRedirect) -> Unit,
) {
    navigation(route = Graph.NOTIFICATION, startDestination = NotificationDestinations.MAIN) {
        composable(NotificationDestinations.MAIN) {
            NotificationRoute(
                onBackClick = navigator::back,
                onAddClick = { navController.navigate(NotificationDestinations.KEYWORD_SETTING) },
                onRedirect = onRedirect,
            )
        }
        composable(NotificationDestinations.KEYWORD_SETTING) {
            KeywordSettingRoute(onBackClick = navigator::back)
        }
    }
}
