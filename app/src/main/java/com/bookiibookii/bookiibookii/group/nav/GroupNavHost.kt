package com.bookiibookii.bookiibookii.group.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.ui.detail.GroupDetailRoute
import com.bookiibookii.bookiibookii.group.ui.editor.GroupEditorRoute
import com.bookiibookii.bookiibookii.group.ui.joinrequest.GroupJoinRequestRoute
import com.bookiibookii.bookiibookii.group.ui.search.GroupSearchRoute

@Composable
fun GroupNavHost(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = GroupDestinations.SEARCH,
    onManageAddress: (ExchangeType) -> Unit = {},
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
            // 수정 모드 판별(groupId)은 GroupEditorViewModel이 SavedStateHandle로 직접 수신
            GroupEditorRoute(
                // 백스택이 있으면 이전 화면(수정 진입: DETAIL)으로,
                // 없으면(생성 진입: EDITOR가 시작점) 그룹 도메인 밖으로 나감
                onBack = { if (!navController.popBackStack()) onExit() },
                // 생성 성공 -> 그룹 상세로 이동. 편집 화면은 백스택에서 제거(뒤로가기 시 폼 재진입 방지)
                onCreated = { groupId ->
                    if (groupId != null) {
                        navController.navigate(GroupDestinations.detail(groupId)) {
                            popUpTo(GroupDestinations.EDITOR) { inclusive = true }
                        }
                    } else if (!navController.popBackStack()) {
                        onExit()
                    }
                },
                // 수정 성공 -> 갱신된 상세로 교체. 기존 상세 화면으로 되돌아갈 스택 삭제. 서치 스택은 유지
                onUpdated = { groupId ->
                    navController.navigate(GroupDestinations.detail(groupId)) {
                        popUpTo(GroupDestinations.DETAIL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onManageAddress = onManageAddress,
            )
        }
        composable(
            route = GroupDestinations.DETAIL,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) {
            // groupId는 SavedStateHandle을 통해 GroupDetailViewModel이 직접 수신
            GroupDetailRoute(
                onBack = { if (!navController.popBackStack()) onExit() },
                // MANAGE 버튼 → 참여 요청 관리 화면으로 이동
                onManage = { groupId ->
                    navController.navigate(GroupDestinations.joinRequests(groupId.toString()))
                },
                // 미트볼 -> 수정하기 -> 그룹 수정(editor) 진입
                onEdit = { groupId ->
                    navController.navigate(GroupDestinations.editor(groupId.toString()))
                },
                // 삭제 성공 → 그룹 목록으로 이동, 삭제된 상세는 백스택에서 제거
                onDeleted = {
                    navController.navigate(GroupDestinations.SEARCH) {
                        popUpTo(GroupDestinations.SEARCH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = GroupDestinations.JOIN_REQUESTS,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_GROUP_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong(GroupDestinations.ARG_GROUP_ID) ?: 0L
            GroupJoinRequestRoute(
                groupId = groupId,
                onBack = { if (!navController.popBackStack()) onExit() },
            )
        }
    }
}
