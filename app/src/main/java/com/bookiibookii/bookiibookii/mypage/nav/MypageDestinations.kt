package com.bookiibookii.bookiibookii.mypage.nav

import android.net.Uri
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab

// 트래커(tracker)/서재(library) 모듈과 동일한 패턴: 라우트 상수 + 인자 키 상수 + 빌더 함수.
// 기존 각 Fragment의 newInstance(...)가 Bundle에 넣던 값을 그대로 query 인자로 옮긴다.
object MypageDestinations {
    const val MAIN = "main"
    const val PROFILE_SETTING = "profileSetting"

    // ── 주소지 관리 (구 AddressManagementFragment) ───────────────────────────
    // 그룹 모듈(GroupFragment)에서 특정 탭으로 직접 딥링크하는 외부 진입점이 있어
    // initialTab을 라우트 인자로 받는다.
    const val ADDRESS_ARG_INITIAL_TAB = "initialTab"
    const val ADDRESS_MANAGEMENT_ROUTE = "addressManagement?$ADDRESS_ARG_INITIAL_TAB={$ADDRESS_ARG_INITIAL_TAB}"
    fun addressManagement(initialTab: Int = 0): String = "addressManagement?$ADDRESS_ARG_INITIAL_TAB=$initialTab"

    const val MY_BOOKSHELF = "myBookshelf"

    // ── 후기 (구 ReviewFragment) ──────────────────────────────────────────
    const val REVIEW_ARG_TAB = "tab"
    const val REVIEW_ROUTE = "review?$REVIEW_ARG_TAB={$REVIEW_ARG_TAB}"
    fun review(tab: ReviewTab = ReviewTab.WRITTEN): String = "review?$REVIEW_ARG_TAB=${tab.name}"

    const val SETTING = "setting"
    const val NOTICE = "notice"

    // ── 공지사항 상세 (구 NoticeDetailFragment) ──────────────────────────────
    const val NOTICE_DETAIL_ARG_NOTICE_ID = "noticeId"
    const val NOTICE_DETAIL_ARG_TITLE = "title"
    const val NOTICE_DETAIL_ROUTE = "noticeDetail/{$NOTICE_DETAIL_ARG_NOTICE_ID}?$NOTICE_DETAIL_ARG_TITLE={$NOTICE_DETAIL_ARG_TITLE}"
    fun noticeDetail(noticeId: Long, title: String): String =
        "noticeDetail/$noticeId?$NOTICE_DETAIL_ARG_TITLE=${Uri.encode(title)}"

    const val FAQ = "faq"
    const val WITHDRAW = "withdraw"

    // ── 약관/개인정보 웹뷰 (구 WebViewFragment) — 현재 호출부 없음(SettingScreen은
    // 외부 브라우저로 열고 있음), 그래도 기능 자체는 보존해 라우트로 옮겨둔다.
    const val WEBVIEW_ARG_TITLE = "title"
    const val WEBVIEW_ARG_ASSET = "asset"
    const val WEBVIEW_ROUTE = "webview?$WEBVIEW_ARG_TITLE={$WEBVIEW_ARG_TITLE}&$WEBVIEW_ARG_ASSET={$WEBVIEW_ARG_ASSET}"
    fun webview(title: String, assetFileName: String): String =
        "webview?$WEBVIEW_ARG_TITLE=${Uri.encode(title)}&$WEBVIEW_ARG_ASSET=${Uri.encode(assetFileName)}"
}
