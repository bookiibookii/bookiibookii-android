package com.bookiibookii.bookiibookii.group.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.group.ui.detail.GroupDetailScreen
import com.bookiibookii.bookiibookii.group.ui.editor.GroupEditorRoute
import com.bookiibookii.bookiibookii.group.ui.search.GroupSearchRoute

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
            GroupSearchRoute(
                // 백스택이 있으면 이전 화면으로, 없으면(홈에서 직접 진입) 그룹 도메인 밖으로 나감
                onBack = { if (!navController.popBackStack()) onExit() },
                onGroupClick = { groupId ->
                    navController.navigate(GroupDestinations.detail(groupId))
                },
            )
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
                // 생성 성공 → 그룹 상세로 이동. 편집 화면은 백스택에서 제거(뒤로가기 시 폼 재진입 방지)
                onCreated = { groupId ->
                    if (groupId != null) {
                        navController.navigate(GroupDestinations.detail(groupId)) {
                            popUpTo(GroupDestinations.EDITOR) { inclusive = true }
                        }
                    } else if (!navController.popBackStack()) {
                        onExit()
                    }
                },
            )
        }
        composable(
            route = GroupDestinations.DETAIL,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            // 방어용 fallback 0L
            val groupId = backStackEntry.arguments?.getLong(GroupDestinations.ARG_GROUP_ID) ?: 0L
            GroupDetailScreen(
                groupId = groupId,
                onBack = { if (!navController.popBackStack()) onExit() },
            )
        }
    }
}
