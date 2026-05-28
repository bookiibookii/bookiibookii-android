package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class NoticeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                NoticeScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onNoticeClick = { title ->
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, NoticeDetailFragment.newInstance(title))
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
