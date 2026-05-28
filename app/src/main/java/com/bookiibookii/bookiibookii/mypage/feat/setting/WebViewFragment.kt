package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.setting.WebViewScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class WebViewFragment : BaseMypageFragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_ASSET = "asset"

        fun newInstance(title: String, assetFileName: String) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_ASSET, assetFileName)
            }
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
                WebViewScreen(
                    title = arguments?.getString(ARG_TITLE) ?: "",
                    assetFileName = arguments?.getString(ARG_ASSET) ?: "",
                    onBackClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }
}
