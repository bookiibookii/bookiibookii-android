package com.bookiibookii.bookiibookii.tracker.nav

object TrackerDestinations {
    const val MAIN = "main"

    const val DETAIL_ARG_GROUP_ID = "groupId"
    const val DETAIL_ROUTE = "detail/{$DETAIL_ARG_GROUP_ID}"
    fun detail(groupId: Long): String = "detail/$groupId"

    const val BOOK_REVIEW_ARG_GROUP_ID = "groupId"
    const val BOOK_REVIEW_ROUTE = "bookReview/{$BOOK_REVIEW_ARG_GROUP_ID}"
    fun bookReview(groupId: Long): String = "bookReview/$groupId"
}
