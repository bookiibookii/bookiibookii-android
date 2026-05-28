package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.ui.setting.SettingScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class SettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                SettingScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onNoticeClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, NoticeFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onQuestionClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, FaqFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onTermsClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, WebViewFragment.newInstance("서비스 이용 약관", "service_terms.html"))
                            .addToBackStack(null)
                            .commit()
                    },
                    onPrivacyClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, WebViewFragment.newInstance("개인정보 처리 방침", "privacy_policy.html"))
                            .addToBackStack(null)
                            .commit()
                    },
                    onWithdrawClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, WithdrawFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onLogoutClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }
}
