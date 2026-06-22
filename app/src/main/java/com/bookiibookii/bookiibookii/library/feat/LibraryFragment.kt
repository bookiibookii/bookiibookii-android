package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.library.nav.LibraryNavHost
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryFragment : Fragment() {

    private val startDestination: String
        get() = arguments?.getString(ARG_START_DESTINATION) ?: LibraryDestinations.MAIN

    private var currentRoute: String = LibraryDestinations.MAIN

    fun isAtMainRoute(): Boolean = currentRoute == LibraryDestinations.MAIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        currentRoute = startDestination
        setContent {
            BookiiBookiiTheme {
                LibraryNavHost(
                    onExitLibrary = { parentFragmentManager.popBackStack() },
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onRouteChanged = { currentRoute = it },
                    startDestination = startDestination,
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "arg_start_destination"

        fun newInstanceAtDetail(
            groupId: Int,
            memberBookId: Int,
            groupName: String = "",
            bookTitle: String = "",
            author: String = "",
            genre: String = "",
            coverUrl: String = "",
            startDate: String = "",
            endDate: String = "",
            completedAt: String = "",
            rating: Double = 0.0,
            isDone: Boolean = false,
            progressRate: Int = 0,
            totalPages: Int = 0,
        ) = LibraryFragment().apply {
            arguments = Bundle().apply {
                putString(
                    ARG_START_DESTINATION,
                    LibraryDestinations.detail(
                        groupId = groupId,
                        memberBookId = memberBookId,
                        groupName = groupName,
                        bookTitle = bookTitle,
                        author = author,
                        genre = genre,
                        coverUrl = coverUrl,
                        startDate = startDate,
                        endDate = endDate,
                        completedAt = completedAt,
                        rating = rating,
                        isDone = isDone,
                        progressRate = progressRate,
                        totalPages = totalPages,
                    ),
                )
            }
        }
    }
}
