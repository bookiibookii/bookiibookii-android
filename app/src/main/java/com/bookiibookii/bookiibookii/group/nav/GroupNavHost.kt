package com.bookiibookii.bookiibookii.group.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.ui.detail.GroupDetailRoute
import com.bookiibookii.bookiibookii.group.ui.editor.GroupEditorRoute
import com.bookiibookii.bookiibookii.group.ui.joinrequest.GroupJoinRequestRoute
import com.bookiibookii.bookiibookii.group.ui.search.GroupSearchRoute
import com.bookiibookii.bookiibookii.ui.nav.AppNavigator
import com.bookiibookii.bookiibookii.ui.nav.Graph

fun NavGraphBuilder.groupGraph(
    navController: NavController,
    navigator: AppNavigator,
) {
    // 주소 미등록 안내 → 교환 유형에 맞는 주소지 관리 탭.
    val onManageAddress: (ExchangeType) -> Unit = { tradeType ->
        navigator.toAddressManagement(
            when (tradeType) {
                ExchangeType.DELIVERY -> 0
                ExchangeType.DIRECT -> 1
            },
        )
    }

    navigation(route = Graph.GROUP, startDestination = GroupDestinations.SEARCH) {
        composable(
            route = GroupDestinations.SEARCH,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_KEYWORD) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            // keyword 인자는 GroupSearchViewModel이 SavedStateHandle로 직접 수신
            GroupSearchRoute(
                onBack = navigator::back,
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
                onBack = navigator::back,
                // 생성 성공 -> 그룹 상세로 이동. 편집 화면은 백스택에서 제거(뒤로가기 시 폼 재진입 방지)
                onCreated = { groupId ->
                    if (groupId != null) {
                        navController.navigate(GroupDestinations.detail(groupId)) {
                            popUpTo(GroupDestinations.EDITOR) { inclusive = true }
                        }
                    } else {
                        navigator.back()
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
                navArgument(GroupDestinations.ARG_GROUP_ID) { type = NavType.LongType },
            ),
        ) {
            // groupId는 SavedStateHandle을 통해 GroupDetailViewModel이 직접 수신
            GroupDetailRoute(
                onBack = navigator::back,
                onManage = { groupId ->
                    navController.navigate(GroupDestinations.joinRequests(groupId.toString()))
                },
                onEdit = { groupId ->
                    navController.navigate(GroupDestinations.editor(groupId.toString()))
                },
                // 삭제 성공 → 그룹 목록으로 이동, 삭제된 상세는 백스택에서 제거
                onDeleted = {
                    navController.navigate(GroupDestinations.search()) {
                        popUpTo(GroupDestinations.SEARCH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onManageAddress = onManageAddress,
            )
        }
        composable(
            route = GroupDestinations.JOIN_REQUESTS,
            arguments = listOf(
                navArgument(GroupDestinations.ARG_GROUP_ID) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong(GroupDestinations.ARG_GROUP_ID) ?: 0L
            GroupJoinRequestRoute(
                groupId = groupId,
                onBack = navigator::back,
                // 수락 성공 → 상세(+명단)를 백스택에서 제거하고 그 전 화면으로.
                onAccepted = {
                    navController.popBackStack(GroupDestinations.DETAIL, inclusive = true)
                    // 복귀한 검색 화면에 재조회 신호
                    navController.currentBackStackEntry
                        ?.savedStateHandle?.set(GroupDestinations.RESULT_REFRESH, true)
                },
            )
        }
    }
}
