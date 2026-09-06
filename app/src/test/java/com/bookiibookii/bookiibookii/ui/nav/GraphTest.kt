package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.mypage.nav.MypageDestinations
import com.bookiibookii.bookiibookii.notification.nav.NotificationDestinations
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphTest {

    // --- 최상위 판정: 바텀네비를 보여줄 목적지 ---

    @Test fun `홈은 최상위다`() =
        assertTrue(isTopLevelRoute(Graph.HOME))

    @Test fun `트래커 메인은 최상위다`() =
        assertTrue(isTopLevelRoute(TrackerDestinations.MAIN))

    @Test fun `서재 메인은 최상위다`() =
        assertTrue(isTopLevelRoute(LibraryDestinations.MAIN))

    @Test fun `트래커 상세는 최상위가 아니다`() =
        assertFalse(isTopLevelRoute(TrackerDestinations.DETAIL_ROUTE))

    @Test fun `서재 상세는 최상위가 아니다`() =
        assertFalse(isTopLevelRoute(LibraryDestinations.DETAIL_ROUTE))

    // 마이페이지는 현재도 바텀네비가 숨겨진다 (MypageNavHost의 DisposableEffect)
    @Test fun `마이페이지 메인은 최상위가 아니다`() =
        assertFalse(isTopLevelRoute(MypageDestinations.MAIN))

    @Test fun `알림은 최상위가 아니다`() =
        assertFalse(isTopLevelRoute(NotificationDestinations.MAIN))

    @Test fun `null이면 최상위가 아니다`() =
        assertFalse(isTopLevelRoute(null))

    @Test fun `모르는 라우트는 최상위가 아니다`() =
        assertFalse(isTopLevelRoute("whatever"))

    // --- 그래프 route ---

    @Test fun `그래프 route는 서로 중복되지 않는다`() {
        val all = listOf(
            Graph.HOME, Graph.GROUP, Graph.TRACKER, Graph.LIBRARY,
            Graph.MYPAGE, Graph.OTHER_PROFILE, Graph.NOTIFICATION,
        )
        assertEquals(all.size, all.toSet().size)
    }

    // --- 홈 라우트 빌더: HomeFragment.newInstanceAtMyGroups()를 대체한다 ---

    @Test fun `탭 없이 홈으로 가면 선택 인자가 없다`() =
        assertEquals("home", Graph.home())

    @Test fun `내 그룹 탭으로 홈에 진입한다`() =
        assertEquals("home?tab=MY_GROUPS", Graph.home(HomeTab.MY_GROUPS))

    @Test fun `홈 패턴은 탭을 선택 인자로 갖는다`() =
        assertEquals("home?tab={tab}", Graph.HOME)
}
