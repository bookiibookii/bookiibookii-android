package com.bookiibookii.bookiibookii.mypage.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.mypage.nav.OtherUserProfileNavHost
import com.bookiibookii.bookiibookii.mypage.vm.OtherUserProfileViewModel
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class OtherUserProfileFragment : Fragment() {

    private val nickname: String
        get() = requireArguments().getString(ARG_NICKNAME) ?: ""

    private val viewModel: OtherUserProfileViewModel by lazy {
        ViewModelProvider(this, OtherUserProfileViewModel.Factory(nickname))[OtherUserProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                CompositionLocalProvider(
                    LocalOnProfileClick provides { nick ->
                        val myNickname = TokenManager.getNickname(requireContext())
                        val fragment = if (myNickname != null && nick == myNickname) MypageFragment()
                                       else OtherUserProfileFragment.newInstance(nick)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                ) {
                    OtherUserProfileNavHost(
                        viewModel = viewModel,
                        onExit = { parentFragmentManager.popBackStack() },
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_NICKNAME = "nickname"

        fun newInstance(nickname: String) = OtherUserProfileFragment().apply {
            arguments = Bundle().apply { putString(ARG_NICKNAME, nickname) }
        }
    }
}
