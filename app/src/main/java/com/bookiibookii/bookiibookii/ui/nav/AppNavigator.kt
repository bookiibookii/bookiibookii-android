package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.mypage.vm.GroupReviewNavTarget

// 도메인 간 이동 인터페이스. 각 그래프의 내부 이동도 루트 NavController를 공유한다.
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

    // 홈의 특정 탭을 지정하면 저장된 상태 대신 해당 탭으로 새로 진입한다.
    fun toHomeTab(tab: HomeTab? = null)
    fun toLibraryTab()

    fun back()
}
