package com.bookiibookii.bookiibookii.notification.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bookiibookii.bookiibookii.notification.ui.KeywordSettingRoute
import com.bookiibookii.bookiibookii.notification.ui.NotificationRoute

@Composable
fun NotificationNavHost(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = NotificationDestinations.MAIN,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(NotificationDestinations.MAIN) {
            NotificationRoute(
                onBackClick = { if (!navController.popBackStack()) onExit() },
                onAddClick = { navController.navigate(NotificationDestinations.KEYWORD_SETTING) },
            )
        }
        composable(NotificationDestinations.KEYWORD_SETTING) {
            KeywordSettingRoute(
                onBackClick = { if (!navController.popBackStack()) onExit() },
            )
        }
    }
}
