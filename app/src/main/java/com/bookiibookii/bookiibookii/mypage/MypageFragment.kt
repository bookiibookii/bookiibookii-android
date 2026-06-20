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

// 마이페이지 모듈의 단일 진입점 — 트래커/서재 모듈과 동일한 패턴.
// 내부 화면 전환은 모두 MypageNavHost(Compose Navigation)가 담당한다.
// MypageViewModel은 메인/프로필수정/후기/탈퇴 화면에서 공유되어야 해서(구 activityViewModels())
// 이 Fragment 레벨에서 activityViewModels()로 생성해 NavHost에 단일 인스턴스로 넘긴다.
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

        // 그룹 모듈(GroupFragment) 등 외부에서 주소지 관리 화면(특정 탭)으로 직접 진입할 때 사용
        // (구 AddressManagementFragment.newInstance를 직접 호출하던 자리를 대체)
        fun newInstanceAtAddressManagement(initialTab: Int) = MypageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, MypageDestinations.addressManagement(initialTab))
            }
        }
    }
}
