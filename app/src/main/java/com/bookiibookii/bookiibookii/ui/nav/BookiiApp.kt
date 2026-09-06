package com.bookiibookii.bookiibookii.ui.nav

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.common.ComRetryBus
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.group.nav.groupGraph
import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.home.HomeViewModel
import com.bookiibookii.bookiibookii.home.ui.HomeRoute
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.library.nav.libraryGraph
import com.bookiibookii.bookiibookii.mypage.nav.MypageDestinations
import com.bookiibookii.bookiibookii.mypage.nav.OtherProfileDestinations
import com.bookiibookii.bookiibookii.mypage.nav.mypageGraph
import com.bookiibookii.bookiibookii.mypage.nav.otherProfileGraph
import com.bookiibookii.bookiibookii.mypage.vm.GroupReviewNavTarget
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.notification.nav.notificationGraph
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations
import com.bookiibookii.bookiibookii.tracker.nav.trackerGraph
import com.bookiibookii.bookiibookii.ui.component.BottomNavBar
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick

/**
 * 앱 루트. NavController를 소유하고 바텀네비 가시성을 단일 규칙으로 판정한다.
 *
 * @param pendingRedirect 알림 딥링크. 소비 후 [onRedirectConsumed]로 알린다.
 * @param onCardDetailRequest 카드 상세는 이동 전 서버 조회가 필요해 Activity가 처리한다. (이슈 B에서 제거 예정)
 */
@Composable
fun BookiiApp(
    pendingRedirect: NotificationRedirect?,
    onRedirectConsumed: () -> Unit,
    onCardDetailRequest: (memberBookId: Long, cardId: Long) -> Unit,
    onUnsupportedRedirect: () -> Unit,
    pendingCardDetailRoute: String?,
    onCardDetailRouteConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navigator = remember(navController) { NavControllerAppNavigator(navController, context) }

    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    LaunchedEffect(pendingCardDetailRoute) {
        val route = pendingCardDetailRoute ?: return@LaunchedEffect
        navController.navigateToTab(Graph.LIBRARY)
        navController.navigate(route)
        onCardDetailRouteConsumed()
    }

    LaunchedEffect(pendingRedirect) {
        val redirect = pendingRedirect ?: return@LaunchedEffect
        navController.handleRedirect(redirect, onCardDetailRequest, onUnsupportedRedirect)
        onRedirectConsumed()
    }

    // 공통 에러 화면에서 '다시 시도' → 최상위 화면이면 해당 탭을 새로 띄운다.
    // 통합 전 MainActivity가 현재 탭 Fragment를 재생성하던 동작과 같다.
    LaunchedEffect(Unit) {
        ComRetryBus.retryFlow.collect {
            val route = navController.currentBackStackEntry?.destination?.route
            if (isTopLevelRoute(route)) {
                navController.navigateToTab(
                    when (route) {
                        TrackerDestinations.MAIN -> Graph.TRACKER
                        LibraryDestinations.MAIN -> Graph.LIBRARY
                        else -> Graph.HOME
                    },
                )
            }
        }
    }

    CompositionLocalProvider(LocalOnProfileClick provides { nickname -> navigator.toProfile(nickname) }) {
        Scaffold(
            bottomBar = {
                if (isTopLevelRoute(currentRoute)) {
                    BottomNavBar(
                        groupSelected = currentRoute == Graph.HOME,
                        trackerSelected = currentRoute == TrackerDestinations.MAIN,
                        librarySelected = currentRoute == LibraryDestinations.MAIN,
                        onGroupClick = { navigator.toHomeTab() },
                        onTrackerClick = { navController.navigateToTab(Graph.TRACKER) },
                        onLibraryClick = navigator::toLibraryTab,
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Graph.HOME,
                modifier = Modifier.padding(innerPadding),
                // 기본 크로스페이드 시 이전 화면이 잔상처럼 겹쳐 보여 제거
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable(
                    route = Graph.HOME,
                    arguments = listOf(
                        navArgument(Graph.HOME_ARG_TAB) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { backStackEntry ->
                    val vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    val startTab = backStackEntry.arguments?.getString(Graph.HOME_ARG_TAB)
                    LaunchedEffect(startTab) {
                        startTab?.let { vm.selectTab(HomeTab.valueOf(it)) }
                    }
                    HomeRoute(
                        viewModel = vm,
                        onGroupClick = navigator::toGroupDetail,
                        onSearchClick = { navigator.toGroupSearch() },
                        onCreateGroupClick = navigator::toGroupEditor,
                        onNotificationClick = navigator::toNotification,
                        onProfileClick = { navigator.toProfile() },
                        onBookClick = { keyword -> navigator.toGroupSearch(keyword) },
                    )
                }

                groupGraph(navController, navigator)
                trackerGraph(navController, navigator)
                libraryGraph(navController, navigator)
                mypageGraph(navController, navigator)
                otherProfileGraph(navController, navigator)
                notificationGraph(navController, navigator) { redirect ->
                    navController.handleRedirect(redirect, onCardDetailRequest, onUnsupportedRedirect)
                }
            }
        }
    }
}

private fun NavController.handleRedirect(
    redirect: NotificationRedirect,
    onCardDetailRequest: (Long, Long) -> Unit,
    onUnsupported: () -> Unit,
) {
    when (val target = redirect.toTarget()) {
        is RedirectTarget.ToGraph -> navigateToTab(target.graph)
        is RedirectTarget.ToDestination -> {
            navigateToTab(target.graph)
            navigate(target.route)
        }
        is RedirectTarget.ToCardDetail -> onCardDetailRequest(target.memberBookId, target.cardId)
        RedirectTarget.Unsupported -> onUnsupported()
        null -> Unit // 미구현 라우트는 무시
    }
}

/**
 * 탭 전환. 이슈 A에서는 통합 전 동작(탭을 누를 때마다 초기화)을 그대로 재현한다.
 * saveState/restoreState는 이슈 C에서 켠다.
 */
private fun NavController.navigateToTab(graphRoute: String) {
    navigate(graphRoute) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

private class NavControllerAppNavigator(
    private val navController: NavController,
    private val context: Context,
) : AppNavigator {

    override fun toGroupDetail(groupId: Long) = go(Graph.GROUP, GroupDestinations.detail(groupId))
    override fun toGroupSearch(keyword: String?) = go(Graph.GROUP, GroupDestinations.search(keyword))
    override fun toGroupEditor() = go(Graph.GROUP, GroupDestinations.editor())
    override fun toGroupJoinRequests(groupId: Long) =
        go(Graph.GROUP, GroupDestinations.joinRequests(groupId.toString()))

    override fun toTrackerDetail(groupId: Long) = go(Graph.TRACKER, TrackerDestinations.detail(groupId))
    override fun toTrackerComment(groupId: Long, title: String) =
        go(Graph.TRACKER, TrackerDestinations.comment(groupId, title))

    override fun toLibraryDetail(target: LibraryDetailTarget) = go(
        Graph.LIBRARY,
        LibraryDestinations.detail(
            groupId = target.groupId,
            memberBookId = target.memberBookId,
            groupName = target.groupName,
            bookTitle = target.bookTitle,
            author = target.author,
            genre = target.genre,
            coverUrl = target.coverUrl,
            startDate = target.startDate,
            endDate = target.endDate,
            completedAt = target.completedAt,
            rating = target.rating,
            isDone = target.isDone,
            progressRate = target.progressRate,
            totalPages = target.totalPages ?: 0,
        ),
    )

    override fun toLibraryGroupReview(target: GroupReviewNavTarget) = go(
        Graph.LIBRARY,
        LibraryDestinations.groupReview(
            groupId = target.groupId,
            groupName = target.groupName,
            bookTitle = target.bookTitle,
            startDate = target.startDate,
            endDate = target.endDate,
        ),
    )

    override fun toNotification() {
        navController.navigate(Graph.NOTIFICATION)
    }

    // 통합 전 4개 Fragment에 중복돼 있던 판정을 여기 한 곳으로 모은다.
    override fun toProfile(nickname: String?) {
        val myNickname = TokenManager.getNickname(context)
        if (nickname == null || (myNickname != null && nickname == myNickname)) {
            navController.navigate(Graph.MYPAGE)
        } else {
            navController.navigate(OtherProfileDestinations.profile(nickname))
        }
    }

    override fun toAddressManagement(initialTab: Int) =
        go(Graph.MYPAGE, MypageDestinations.addressManagement(initialTab))

    override fun toHomeTab(tab: HomeTab?) {
        navController.navigate(Graph.home(tab)) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    override fun toLibraryTab() = navController.navigateToTab(Graph.LIBRARY)

    override fun back() {
        navController.popBackStack()
    }

    // 그래프 시작 목적지를 먼저 쌓아 도메인 밖에서 진입해도 뒤로가기가 그 도메인 홈으로 가게 한다.
    private fun go(graph: String, route: String) {
        navController.navigate(graph)
        navController.navigate(route)
    }
}
