package com.bookiibookii.bookiibookii.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.feat.LibraryFragment
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

    companion object {
        private const val ARG_START_DESTINATION = "arg_start_destination"

        fun newInstanceAtAddressManagement(initialTab: Int) = MypageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, MypageDestinations.addressManagement(initialTab))
            }
        }
    }
}
