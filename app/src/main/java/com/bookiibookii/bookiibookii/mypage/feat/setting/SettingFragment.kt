package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.common.openPrivacyPolicy
import com.bookiibookii.bookiibookii.common.openTermsOfService
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.ui.setting.SettingScreen
import com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar
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
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, FaqFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onTermsClick = { requireContext().openTermsOfService() },
                    onPrivacyClick = { requireContext().openPrivacyPolicy() },
                    onWithdrawClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, WithdrawFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onLogoutClick = {
                        val ctx = requireContext().applicationContext
                        // 인증 토큰이 살아있는 동안 FCM 토큰 해제 → 완료 후 clear + 로그인 화면
                        FcmTokenRegistrar.deactivateCurrentToken {
                            TokenManager.clear(ctx)
                            val intent = Intent(ctx, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            ctx.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}
