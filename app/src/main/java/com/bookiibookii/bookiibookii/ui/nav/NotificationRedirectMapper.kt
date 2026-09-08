package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations

sealed interface RedirectTarget {
    // 그래프 시작 목적지로 이동
    data class ToGraph(val graph: String) : RedirectTarget

    // 그래프 내부 목적지로 이동
    data class ToDestination(val graph: String, val route: String) : RedirectTarget

    // 서재 카드 상세 — 이동 전에 서버 조회가 필요하다
    data class ToCardDetail(val memberBookId: Long, val cardId: Long) : RedirectTarget

    // 필수 인자가 빠진 구버전 알림. 호출부가 토스트로 알린다
    data object Unsupported : RedirectTarget
}

// 백엔드 redirectType을 이동 대상으로 변환한다.
// null은 '조용히 무시' — 미구현 타입이거나 groupId가 없는 경우이며 기존 동작과 같다.
fun NotificationRedirect.toTarget(): RedirectTarget? = when (redirectType) {
    "EXPLORE_HOME" -> RedirectTarget.ToGraph(Graph.HOME)
    "TRACKER_HOME" -> RedirectTarget.ToGraph(Graph.TRACKER)

    "APPLICATION_MANAGEMENT" -> groupId?.let {
        RedirectTarget.ToDestination(Graph.GROUP, GroupDestinations.joinRequests(it.toString()))
    }
    "GROUP_DETAIL" -> groupId?.let {
        RedirectTarget.ToDestination(Graph.GROUP, GroupDestinations.detail(it))
    }
    "TRACKER_DETAIL" -> groupId?.let {
        RedirectTarget.ToDestination(Graph.TRACKER, TrackerDestinations.detail(it))
    }
    "TRACKER_COMMENT" -> groupId?.let {
        RedirectTarget.ToDestination(Graph.TRACKER, TrackerDestinations.comment(it, title.orEmpty()))
    }

    "BOOK_CARD_DETAIL" -> {
        val mb = memberBookId
        val c = cardId
        if (mb != null && c != null) RedirectTarget.ToCardDetail(mb, c)
        else RedirectTarget.Unsupported
    }

    else -> null
}
