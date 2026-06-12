package com.bookiibookii.bookiibookii.notification.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
        // 화면 전환 애니메이션 제거(기본 크로스페이드 시 이전 화면이 잔상처럼 겹쳐 보이는 현상 방지)
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
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
