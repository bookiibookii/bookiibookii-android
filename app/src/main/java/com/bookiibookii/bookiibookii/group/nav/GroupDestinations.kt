package com.bookiibookii.bookiibookii.group.nav

object GroupDestinations {

    const val ARG_GROUP_ID = "groupId"

    const val SEARCH = "search"
    const val DETAIL = "detail/{$ARG_GROUP_ID}"
    const val EDITOR = "editor?$ARG_GROUP_ID={$ARG_GROUP_ID}"
    const val JOIN_REQUESTS = "joinRequests/{$ARG_GROUP_ID}"

    fun detail(groupId: Long) = "detail/$groupId"

    // groupId == null -> 생성, not null -> 수정
    fun editor(groupId: String? = null) =
        if (groupId == null) "editor" else "editor?$ARG_GROUP_ID=$groupId"

    fun joinRequests(groupId: String) = "joinRequests/$groupId"
}
