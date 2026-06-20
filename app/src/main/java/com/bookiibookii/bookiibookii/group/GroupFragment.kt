package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.group.nav.GroupNavHost
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class GroupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        // Fragment view 생명주기에 맞춰 컴포지션 폐기 (백스택 복귀 시 누수 방지)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val startDestination =
            arguments?.getString(ARG_START_DESTINATION) ?: GroupDestinations.SEARCH
        setContent {
            BookiiBookiiTheme {
                GroupNavHost(
                    startDestination = startDestination,
                    onExit = { parentFragmentManager.popBackStack() },
                    // 주소 미등록 시 주소지 관리 화면으로 이동
                    // 택배 → 배송지 탭(0), 직접 → 희망 교환 장소 탭(1)
                    onManageAddress = { tradeType ->
                        val initialTab = when (tradeType) {
                            ExchangeType.DELIVERY -> 0
                            ExchangeType.DIRECT -> 1
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                MypageFragment.newInstanceAtAddressManagement(initialTab),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "startDestination"

        fun newInstance(startDestination: String) = GroupFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, startDestination)
            }
        }
    }
}
