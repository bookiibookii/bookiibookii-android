package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations

/**
 * 루트 NavHost의 목적지 route.
 *
 * 홈은 화면이 하나뿐이라 중첩 그래프가 아니라 목적지 하나다.
 * 나머지는 도메인별 navigation {} 블록의 route다.
 */
object Graph {

    const val HOME_ARG_TAB = "tab"

    /**
     * 홈 목적지 패턴. `composable()`에 넘기고 `destination.route` 비교에도 쓴다.
     * 이동할 때는 [home] 빌더를 쓴다 — 패턴 문자열로는 navigate 할 수 없다.
     */
    const val HOME = "home?$HOME_ARG_TAB={$HOME_ARG_TAB}"

    const val GROUP = "graph_group"
    const val TRACKER = "graph_tracker"
    const val LIBRARY = "graph_library"
    const val MYPAGE = "graph_mypage"
    const val OTHER_PROFILE = "graph_other_profile"
    const val NOTIFICATION = "graph_notification"

    /**
     * 홈으로 이동할 route를 만든다.
     *
     * tab이 주어지면 진입 시 해당 탭이 선택된다.
     */
    fun home(tab: HomeTab? = null): String =
        if (tab == null) "home" else "home?$HOME_ARG_TAB=${tab.name}"
}

/**
 * 바텀네비를 보여줄 목적지.
 */
val TOP_LEVEL_ROUTES: Set<String> = setOf(
    Graph.HOME,
    TrackerDestinations.MAIN,
    LibraryDestinations.MAIN,
)

fun isTopLevelRoute(route: String?): Boolean = route in TOP_LEVEL_ROUTES
