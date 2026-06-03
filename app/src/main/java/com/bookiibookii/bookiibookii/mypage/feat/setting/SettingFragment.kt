package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.ui.setting.SettingScreen
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class SettingFragment : BaseMypageFragment() {
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
                        startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("http://pf.kakao.com/_cIxlxjX")
                            )
                        )
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
                    onLogoutClick = {
                        TokenManager.clear(requireContext())
                        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    },
                )
            }
        }
    }
}
