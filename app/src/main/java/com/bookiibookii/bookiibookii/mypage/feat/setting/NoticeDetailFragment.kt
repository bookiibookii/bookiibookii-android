package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeDetailScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class NoticeDetailFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(title: String) = NoticeDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_TITLE, title) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                NoticeDetailScreen(
                    title = arguments?.getString(ARG_TITLE) ?: "공지사항",
                    onBackClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }
}
