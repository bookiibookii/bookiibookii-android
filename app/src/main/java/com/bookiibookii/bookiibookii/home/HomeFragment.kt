package com.bookiibookii.bookiibookii.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.home.ui.HomeRoute
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class HomeFragment : Fragment() {

    private val vm: HomeViewModel by viewModels()

    // 최초 진입은 ViewModel init/탭 선택이 이미 로드하므로 첫 onResume은 건너뜀.
    // 이후 복귀(상세에서 수락 후 등) 때마다 현재 탭 재조회.
    private var skipNextResumeRefresh = true

    override fun onResume() {
        super.onResume()
        if (skipNextResumeRefresh) {
            skipNextResumeRefresh = false
            return
        }
        vm.refreshCurrentTab()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                HomeRoute(
                    viewModel = vm,
                    onGroupClick = { groupId ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                GroupFragment.newInstance(GroupDestinations.detail(groupId)),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    onSearchClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                GroupFragment.newInstance(GroupDestinations.SEARCH),
                            )
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
                    onNotificationClick = { },
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    // 책 탭 → 해당 책 제목으로 그룹 검색 진입
                    onBookClick = { keyword ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                GroupFragment.newInstance(GroupDestinations.search(keyword)),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
