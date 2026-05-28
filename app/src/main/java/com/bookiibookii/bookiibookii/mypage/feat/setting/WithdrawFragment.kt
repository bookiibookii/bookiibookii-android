package com.bookiibookii.bookiibookii.mypage.feat.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.setting.WithdrawScreen
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class WithdrawFragment : BaseMypageFragment() {

    private val viewModel: SettingViewModel by viewModels()
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                val withdrawFailed by viewModel.withdrawFailed.observeAsState(false)
                val profile by mypageViewModel.profileData.observeAsState()

                WithdrawScreen(
                    userName = profile?.nickname ?: "",
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onWithdraw = { reason, customReason -> viewModel.withdraw(reason, customReason) },
                    showWithdrawFailedDialog = withdrawFailed,
                    onFailureDialogDismiss = { viewModel.clearWithdrawFailed() },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectEvents()
    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is SettingViewModel.Event.WithdrawSuccess -> {
                        TokenManager.clear(requireContext())
                        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    else -> Unit
                }
            }
        }
    }
}
