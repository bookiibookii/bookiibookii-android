package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// TrackerDestinations.comment()이 Uri.encode를 써서 Robolectric 필요
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class NotificationRedirectMapperTest {

    private fun redirect(
        type: String,
        groupId: Long? = null,
        memberBookId: Long? = null,
        cardId: Long? = null,
        title: String? = null,
    ) = NotificationRedirect(type, groupId, memberBookId, cardId, title)

    // --- 탭 이동 ---

    @Test fun `EXPLORE_HOME은 홈 그래프로`() =
        assertEquals(RedirectTarget.ToGraph(Graph.HOME), redirect("EXPLORE_HOME").toTarget())

    @Test fun `TRACKER_HOME은 트래커 그래프로`() =
        assertEquals(RedirectTarget.ToGraph(Graph.TRACKER), redirect("TRACKER_HOME").toTarget())

    // --- 상세 이동 ---

    @Test fun `GROUP_DETAIL은 그룹 상세로`() =
        assertEquals(
            RedirectTarget.ToDestination(Graph.GROUP, GroupDestinations.detail(7L)),
            redirect("GROUP_DETAIL", groupId = 7L).toTarget(),
        )

    @Test fun `APPLICATION_MANAGEMENT는 가입신청 목록으로`() =
        assertEquals(
            RedirectTarget.ToDestination(Graph.GROUP, GroupDestinations.joinRequests("7")),
            redirect("APPLICATION_MANAGEMENT", groupId = 7L).toTarget(),
        )

    @Test fun `TRACKER_DETAIL은 트래커 상세로`() =
        assertEquals(
            RedirectTarget.ToDestination(Graph.TRACKER, TrackerDestinations.detail(7L)),
            redirect("TRACKER_DETAIL", groupId = 7L).toTarget(),
        )

    @Test fun `TRACKER_COMMENT는 제목을 함께 넘긴다`() =
        assertEquals(
            RedirectTarget.ToDestination(Graph.TRACKER, TrackerDestinations.comment(7L, "책 제목")),
            redirect("TRACKER_COMMENT", groupId = 7L, title = "책 제목").toTarget(),
        )

    @Test fun `TRACKER_COMMENT의 title이 null이면 빈 문자열로`() =
        assertEquals(
            RedirectTarget.ToDestination(Graph.TRACKER, TrackerDestinations.comment(7L, "")),
            redirect("TRACKER_COMMENT", groupId = 7L).toTarget(),
        )

    // --- groupId 누락: 기존 동작대로 조용히 무시 (토스트 없음) ---

    @Test fun `GROUP_DETAIL에 groupId 없으면 null`() =
        assertNull(redirect("GROUP_DETAIL").toTarget())

    @Test fun `TRACKER_DETAIL에 groupId 없으면 null`() =
        assertNull(redirect("TRACKER_DETAIL").toTarget())

    @Test fun `APPLICATION_MANAGEMENT에 groupId 없으면 null`() =
        assertNull(redirect("APPLICATION_MANAGEMENT").toTarget())

    @Test fun `TRACKER_COMMENT에 groupId 없으면 null`() =
        assertNull(redirect("TRACKER_COMMENT", title = "제목").toTarget())

    // --- 카드 상세: 인자가 없으면 토스트를 띄워야 하므로 null이 아닌 Unsupported ---

    @Test fun `BOOK_CARD_DETAIL은 카드 상세 타겟`() =
        assertEquals(
            RedirectTarget.ToCardDetail(memberBookId = 3L, cardId = 9L),
            redirect("BOOK_CARD_DETAIL", memberBookId = 3L, cardId = 9L).toTarget(),
        )

    @Test fun `BOOK_CARD_DETAIL에 memberBookId 없으면 Unsupported - 구버전 알림`() =
        assertEquals(
            RedirectTarget.Unsupported,
            redirect("BOOK_CARD_DETAIL", cardId = 9L).toTarget(),
        )

    @Test fun `BOOK_CARD_DETAIL에 cardId 없으면 Unsupported`() =
        assertEquals(
            RedirectTarget.Unsupported,
            redirect("BOOK_CARD_DETAIL", memberBookId = 3L).toTarget(),
        )

    // --- 미구현 / 미지 타입: 조용히 무시 ---

    @Test fun `NOTICE_DETAIL은 미구현이라 null`() =
        assertNull(redirect("NOTICE_DETAIL").toTarget())

    @Test fun `모르는 타입은 null`() =
        assertNull(redirect("WHATEVER").toTarget())
}
