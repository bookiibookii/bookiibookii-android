package com.bookiibookii.bookiibookii.mypage.nav

import android.net.Uri
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab

object MypageDestinations {
    const val MAIN = "main"
    const val PROFILE_SETTING = "profileSetting"

    const val ADDRESS_ARG_INITIAL_TAB = "initialTab"
    const val ADDRESS_MANAGEMENT_ROUTE = "addressManagement?$ADDRESS_ARG_INITIAL_TAB={$ADDRESS_ARG_INITIAL_TAB}"
    fun addressManagement(initialTab: Int = 0): String = "addressManagement?$ADDRESS_ARG_INITIAL_TAB=$initialTab"

    const val MY_BOOKSHELF = "myBookshelf"

    const val REVIEW_ARG_TAB = "tab"
    const val REVIEW_ROUTE = "review?$REVIEW_ARG_TAB={$REVIEW_ARG_TAB}"
    fun review(tab: ReviewTab = ReviewTab.WRITTEN): String = "review?$REVIEW_ARG_TAB=${tab.name}"

    const val SETTING = "setting"
    const val NOTICE = "notice"

    const val NOTICE_DETAIL_ARG_NOTICE_ID = "noticeId"
    const val NOTICE_DETAIL_ARG_TITLE = "title"
    const val NOTICE_DETAIL_ROUTE = "noticeDetail/{$NOTICE_DETAIL_ARG_NOTICE_ID}?$NOTICE_DETAIL_ARG_TITLE={$NOTICE_DETAIL_ARG_TITLE}"
    fun noticeDetail(noticeId: Long, title: String): String =
        "noticeDetail/$noticeId?$NOTICE_DETAIL_ARG_TITLE=${Uri.encode(title)}"

    const val FAQ = "faq"
    const val WITHDRAW = "withdraw"

    const val WEBVIEW_ARG_TITLE = "title"
    const val WEBVIEW_ARG_ASSET = "asset"
    const val WEBVIEW_ROUTE = "webview?$WEBVIEW_ARG_TITLE={$WEBVIEW_ARG_TITLE}&$WEBVIEW_ARG_ASSET={$WEBVIEW_ARG_ASSET}"
    fun webview(title: String, assetFileName: String): String =
        "webview?$WEBVIEW_ARG_TITLE=${Uri.encode(title)}&$WEBVIEW_ARG_ASSET=${Uri.encode(assetFileName)}"
}
