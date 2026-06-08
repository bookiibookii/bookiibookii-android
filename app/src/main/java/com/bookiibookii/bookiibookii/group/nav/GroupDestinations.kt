package com.bookiibookii.bookiibookii.group.nav

import android.net.Uri

object GroupDestinations {

    const val ARG_GROUP_ID = "groupId"
    const val ARG_KEYWORD = "keyword"

    // 다른 화면(상세 등)에서 복귀 시 검색 목록 재조회를 요청하는 savedStateHandle 키
    const val RESULT_REFRESH = "result_refresh"

    // keyword는 선택 인자 — 있으면 진입 시 해당 검색어로 검색(홈에서 책 탭)
    const val SEARCH = "search?$ARG_KEYWORD={$ARG_KEYWORD}"
    const val DETAIL = "detail/{$ARG_GROUP_ID}"
    const val EDITOR = "editor?$ARG_GROUP_ID={$ARG_GROUP_ID}"
    const val JOIN_REQUESTS = "joinRequests/{$ARG_GROUP_ID}"

    // keyword == null/blank -> 일반 진입, 아니면 검색어 프리필 진입
    fun search(keyword: String? = null) =
        if (keyword.isNullOrBlank()) "search" else "search?$ARG_KEYWORD=${Uri.encode(keyword)}"

    fun detail(groupId: Long) = "detail/$groupId"

    // groupId == null -> 생성, not null -> 수정
    fun editor(groupId: String? = null) =
        if (groupId == null) "editor" else "editor?$ARG_GROUP_ID=$groupId"

    fun joinRequests(groupId: String) = "joinRequests/$groupId"
}
