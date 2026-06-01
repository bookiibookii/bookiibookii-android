package com.bookiibookii.bookiibookii.tracker.nav

import android.net.Uri

object TrackerDestinations {
    const val MAIN = "main"

    const val DETAIL_ARG_GROUP_ID = "groupId"
    const val DETAIL_ROUTE = "detail/{$DETAIL_ARG_GROUP_ID}"
    fun detail(groupId: Long): String = "detail/$groupId"

    const val BOOK_REVIEW_ARG_GROUP_ID = "groupId"
    const val BOOK_REVIEW_ROUTE = "bookReview/{$BOOK_REVIEW_ARG_GROUP_ID}"
    fun bookReview(groupId: Long): String = "bookReview/$groupId"

    const val PARTNER_REVIEW_ARG_GROUP_ID = "groupId"
    const val PARTNER_REVIEW_ROUTE = "partnerReview/{$PARTNER_REVIEW_ARG_GROUP_ID}"
    fun partnerReview(groupId: Long): String = "partnerReview/$groupId"

    // 댓글 — 그룹 댓글과 동일 API라 groupId 기반
    const val COMMENT_ARG_GROUP_ID = "groupId"
    const val COMMENT_ARG_TITLE = "title"
    const val COMMENT_ROUTE = "comment/{$COMMENT_ARG_GROUP_ID}?$COMMENT_ARG_TITLE={$COMMENT_ARG_TITLE}"
    fun comment(groupId: Long, title: String): String =
        "comment/$groupId?$COMMENT_ARG_TITLE=${Uri.encode(title)}"
}
