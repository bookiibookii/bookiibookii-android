package com.bookiibookii.bookiibookii.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.library.feat.LibraryFragment
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.notification.NotificationFragment
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations
import com.bookiibookii.bookiibookii.tracker.nav.TrackerNavHost
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class TrackerFragment : Fragment() {

    // 알림 클릭 등 딥링크 진입 시작점. 없으면 트래커 홈(MAIN).
    private val startDestination: String
        get() = arguments?.getString(ARG_START_DESTINATION) ?: TrackerDestinations.MAIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                TrackerNavHost(
                    startDestination = startDestination,
                    // "서재로 이동" → 바텀네비 서재 탭을 누른 것처럼 전환
                    onNavigateLibrary = {
                        (activity as? MainActivity)?.moveToLibraryTab()
                    },
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onAlertClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, NotificationFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onCreateGroupClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                GroupFragment.newInstance(GroupDestinations.EDITOR),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    // "독서카드 작성" → 해석된 책 정보로 서재 상세 진입 (FAB로 카드 작성)
                    onNavigateLibraryDetail = { target ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                LibraryFragment.newInstanceAtDetail(
                                    groupId = target.groupId,
                                    memberBookId = target.memberBookId,
                                    groupName = target.groupName,
                                    bookTitle = target.bookTitle,
                                    author = target.author,
                                    genre = target.genre,
                                    coverUrl = target.coverUrl,
                                    startDate = target.startDate,
                                    endDate = target.endDate,
                                    completedAt = target.completedAt,
                                    rating = target.rating,
                                    isDone = target.isDone,
                                    progressRate = target.progressRate,
                                    totalPages = target.totalPages ?: 0,
                                ),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "arg_start_destination"

        // 알림 클릭 딥링크 진입 (예: 트래커 상세/댓글)
        fun newInstance(startDestination: String) = TrackerFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, startDestination)
            }
        }
    }
}
