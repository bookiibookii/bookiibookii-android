package com.bookiibookii.bookiibookii.group.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.group.ui.editor.GroupEditorRoute
import com.bookiibookii.bookiibookii.group.ui.search.GroupSearchScreen

@Composable
fun GroupNavHost(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = GroupDestinations.SEARCH,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(GroupDestinations.SEARCH) {
            GroupSearchScreen()
        }
        composable(
            route = GroupDestinations.EDITOR,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_GROUP_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            GroupEditorRoute(
                // 백스택이 있으면 이전 화면(수정 진입: DETAIL)으로,
                // 없으면(생성 진입: EDITOR가 시작점) 그룹 도메인 밖으로 나간다
                onBack = { if (!navController.popBackStack()) onExit() },
            )
        }
    }
}
