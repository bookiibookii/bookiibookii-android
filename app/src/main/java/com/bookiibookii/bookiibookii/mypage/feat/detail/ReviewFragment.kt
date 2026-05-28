package com.bookiibookii.bookiibookii.mypage.feat.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewScreen
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab

class ReviewFragment : Fragment() {

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
                ReviewScreen(
                    initialTab = initialTab,
                    onBackClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }
}
