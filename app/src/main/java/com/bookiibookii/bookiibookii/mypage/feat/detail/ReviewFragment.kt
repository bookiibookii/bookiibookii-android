package com.bookiibookii.bookiibookii.mypage.feat.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewScreen
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class ReviewFragment : BaseMypageFragment() {

    companion object {
        private const val ARG_INITIAL_TAB = "initial_tab"

        fun newInstance(tab: ReviewTab = ReviewTab.WRITTEN): ReviewFragment {
            return ReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_TAB, tab.name)
                }
            }
        }
    }

    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val tabName = arguments?.getString(ARG_INITIAL_TAB) ?: ReviewTab.WRITTEN.name
        val initialTab = ReviewTab.valueOf(tabName)
        setContent {
            BookiiBookiiTheme {
                val profile by mypageViewModel.profileData.observeAsState()

                ReviewScreen(
                    initialTab = initialTab,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    bookReviewCount = profile?.bookReviewCount ?: 0,
                    writtenReviews = profile?.recentBookReviews ?: emptyList(),
                    boomUpCount = profile?.boomUpCount ?: 0,
                    receivedReviews = profile?.recentReceivedReviews ?: emptyList(),
                    nickname = profile?.nickname ?: "",
                )
            }
        }
    }
}
