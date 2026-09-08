package com.bookiibookii.bookiibookii.ui.nav

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.dp
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
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

/**
 * 앱 루트. NavController를 소유하고 바텀네비 가시성을 단일 규칙으로 판정한다.
 *
 * @param pendingRedirect 알림 딥링크. 소비 후 [onRedirectConsumed]로 알린다.
 * @param onCardDetailRequest 카드 상세는 이동 전 서버 조회가 필요해 Activity가 처리한다.
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
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Graph.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
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

                    // 최초 진입은 VM init/탭 선택이 이미 로드하므로 첫 ON_RESUME은 건너뛰고,
                    // 이후 복귀(상세에서 수락 후 등) 때마다 현재 탭 재조회.
                    var skipNextResumeRefresh by rememberSaveable { mutableStateOf(true) }
                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        if (skipNextResumeRefresh) {
                            skipNextResumeRefresh = false
                        } else {
                            vm.refreshCurrentTab()
                            vm.fetchNotificationDot()
                        }
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

            // 콘텐츠 위에 떠 있다. 레이아웃 공간을 차지하지 않는다.
            if (isTopLevelRoute(currentRoute)) {
                BottomNavBar(
                    groupSelected = currentRoute == Graph.HOME,
                    trackerSelected = currentRoute == TrackerDestinations.MAIN,
                    librarySelected = currentRoute == LibraryDestinations.MAIN,
                    onGroupClick = { navigator.toHomeTab() },
                    onTrackerClick = { navController.navigateToTab(Graph.TRACKER) },
                    onLibraryClick = navigator::toLibraryTab,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp),
                )
            }

            // 엣지투엣지라 상태바가 투명해 windowBackground(@color/ui_bg)가 비친다. 흰색으로 덮는다.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(BookiiBookiiTheme.colors.white),
            )
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
        // 원본 pushDeepFragment와 동일하게 현재 스택 위에 쌓는다. 뒤로가면 보던 탭으로 돌아온다.
        is RedirectTarget.ToDestination -> navigate(target.route)
        is RedirectTarget.ToCardDetail -> onCardDetailRequest(target.memberBookId, target.cardId)
        RedirectTarget.Unsupported -> onUnsupported()
        null -> Unit // 미구현 라우트는 무시
    }
}

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

    override fun toGroupDetail(groupId: Long) = go(GroupDestinations.detail(groupId))
    override fun toGroupSearch(keyword: String?) = go(GroupDestinations.search(keyword))
    override fun toGroupEditor() = go(GroupDestinations.editor())
    override fun toGroupJoinRequests(groupId: Long) =
        go(GroupDestinations.joinRequests(groupId.toString()))

    override fun toTrackerDetail(groupId: Long) = go(TrackerDestinations.detail(groupId))
    override fun toTrackerComment(groupId: Long, title: String) =
        go(TrackerDestinations.comment(groupId, title))

    override fun toLibraryDetail(target: LibraryDetailTarget) = go(
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
        go(MypageDestinations.addressManagement(initialTab))

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

    // 목적지로 바로 이동한다. 중첩 그래프의 시작 목적지는 쌓지 않는다.
    // 통합 전 XxxFragment.newInstance(목적지)가 그 목적지를 시작점으로 띄우던 것과 같다.
    private fun go(route: String) {
        navController.navigate(route)
    }
}
