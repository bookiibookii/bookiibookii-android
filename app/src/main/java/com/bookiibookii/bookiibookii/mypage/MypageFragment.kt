package com.bookiibookii.bookiibookii.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.feat.LibraryFragment
import com.bookiibookii.bookiibookii.mypage.feat.OtherUserProfileFragment
import com.bookiibookii.bookiibookii.mypage.nav.MypageDestinations
import com.bookiibookii.bookiibookii.mypage.nav.MypageNavHost
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
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
                CompositionLocalProvider(
                    LocalOnProfileClick provides { nickname ->
                        val myNickname = TokenManager.getNickname(requireContext())
                        val fragment = if (myNickname != null && nickname == myNickname) MypageFragment()
                                       else OtherUserProfileFragment.newInstance(nickname)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                ) {
                MypageNavHost(
                    mypageViewModel = viewModel,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onLibraryDetailClick = { book ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                LibraryFragment.newInstanceAtDetail(
                                    groupId = book.groupId.toInt(),
                                    memberBookId = book.memberBookId.toInt(),
                                    bookTitle = book.title,
                                    author = book.author ?: "",
                                    genre = book.category?.trim('(', ')') ?: "",
                                    coverUrl = book.image ?: "",
                                    completedAt = book.completedAt ?: "",
                                    rating = book.rating,
                                    isDone = true,
                                ),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    onGroupReviewClick = { target ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                LibraryFragment.newInstanceAtGroupReview(
                                    groupId = target.groupId,
                                    groupName = target.groupName,
                                    bookTitle = target.bookTitle,
                                    startDate = target.startDate,
                                    endDate = target.endDate,
                                ),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    startDestination = startDestination,
                )
                }
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
