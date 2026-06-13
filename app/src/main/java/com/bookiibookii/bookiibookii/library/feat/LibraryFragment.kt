package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.ui.LibraryScreen
import com.bookiibookii.bookiibookii.library.vm.LibraryMainViewModel
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryFragment : Fragment() {

    private val vm: LibraryMainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.uiState.collectAsState()
            BookiiBookiiTheme {
                LibraryScreen(
                    readingBooks  = state.readingBooks,
                    doneBooks     = state.doneBooks,
                    sortType      = state.sortType,
                    isLoading     = state.isLoading,
                    onSortChange  = { vm.setSortType(it) },
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookmarkClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryBookmarkFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookClick = { book ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                LibraryDetailFragment.newInstance(
                                    groupId      = book.groupId,
                                    memberBookId = book.memberBookId,
                                    groupName    = book.groupName,
                                    bookTitle    = book.title,
                                    author       = book.author,
                                    genre        = book.genre,
                                    coverUrl     = book.coverUrl ?: "",
                                    startDate    = book.startDate,
                                    endDate      = book.endDate ?: "",
                                    completedAt  = book.completedAt ?: "",
                                    rating       = book.rating?.toDouble() ?: 0.0,
                                    isDone       = book.rating != null,
                                    progressRate = ((book.progress ?: 0f) * 100).toInt(),
                                    totalPages   = book.totalPages ?: 0,
                                ),
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.fetchBooks()
    }
}
