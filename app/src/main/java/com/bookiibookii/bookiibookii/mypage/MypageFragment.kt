package com.bookiibookii.bookiibookii.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.mypage.ui.main.MypageScreen
import com.bookiibookii.bookiibookii.mypage.feat.main.ProfileSettingFragment
import com.bookiibookii.bookiibookii.mypage.feat.main.AddressManagementFragment
import com.bookiibookii.bookiibookii.mypage.feat.detail.MyBookshelfFragment
import com.bookiibookii.bookiibookii.mypage.feat.detail.ReviewFragment
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab
// import com.bookiibookii.bookiibookii.mypage.feat.setting.SettingFragment

class MypageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                MypageScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onSettingClick = {
                        // TODO: 설정 화면 연결 예정
                        // parentFragmentManager.beginTransaction()
                        //     .replace(R.id.fragmentContainer, SettingFragment())
                        //     .addToBackStack(null)
                        //     .commit()
                    },
                    onProfileSettingClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ProfileSettingFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onAddressManagementClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddressManagementFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookshelfClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MyBookshelfFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onWrittenReviewClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewFragment.newInstance(ReviewTab.WRITTEN))
                            .addToBackStack(null)
                            .commit()
                    },
                    onReceivedReviewClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewFragment.newInstance(ReviewTab.RECEIVED))
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
