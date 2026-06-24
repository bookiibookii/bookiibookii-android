package com.bookiibookii.bookiibookii.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.mypage.nav.MypageDestinations
import com.bookiibookii.bookiibookii.mypage.nav.MypageNavHost
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class MypageFragment : Fragment() {

    private val viewModel: MypageViewModel by activityViewModels()

    private val startDestination: String
        get() = arguments?.getString(ARG_START_DESTINATION) ?: MypageDestinations.MAIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                MypageNavHost(
                    mypageViewModel = viewModel,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    startDestination = startDestination,
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "arg_start_destination"

        fun newInstanceAtAddressManagement(initialTab: Int) = MypageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, MypageDestinations.addressManagement(initialTab))
            }
        }

        // 알림 클릭 딥링크 진입 (예: 공지 상세)
        fun newInstance(startDestination: String) = MypageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, startDestination)
            }
        }
    }
}
