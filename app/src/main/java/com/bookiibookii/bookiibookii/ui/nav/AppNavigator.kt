package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.mypage.vm.GroupReviewNavTarget

// 도메인 내부 이동은 각 그래프가 자기 navController로 처리하므로 여기 넣지 않는다.
interface AppNavigator {

    fun toGroupDetail(groupId: Long)
    fun toGroupSearch(keyword: String? = null)
    fun toGroupEditor()
    fun toGroupJoinRequests(groupId: Long)

    fun toTrackerDetail(groupId: Long)
    fun toTrackerComment(groupId: Long, title: String)

    fun toLibraryDetail(target: LibraryDetailTarget)
    fun toLibraryGroupReview(target: GroupReviewNavTarget)

    fun toNotification()

    // nickname이 null이거나 내 닉네임이면 마이페이지, 아니면 타 유저 프로필
    fun toProfile(nickname: String? = null)

    fun toAddressManagement(initialTab: Int)

    // 바텀네비 탭을 누른 것과 동일하게 이동
    fun toHomeTab(tab: HomeTab? = null)
    fun toLibraryTab()

    fun back()
}
