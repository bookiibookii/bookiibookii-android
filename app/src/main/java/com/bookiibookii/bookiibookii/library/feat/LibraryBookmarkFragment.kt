package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.LibraryBookmarkScreen
import com.bookiibookii.bookiibookii.library.vm.LibraryBookmarkViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryBookmarkFragment : BaseLibraryFragment() {

    private val vm: LibraryBookmarkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.uiState.collectAsState()
            BookiiBookiiTheme {
                LibraryBookmarkScreen(
                    cards       = state.cards,
                    isLoading   = state.isLoading,
                    onSortChange = { isLatest -> vm.sortByLatest(isLatest) },
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onMoveToLibrary = { parentFragmentManager.popBackStack() },
                    onCardClick = { index, bookmarkedCards ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                ReadingCardDetailFragment.newInstance(
                                    initialIndex = index,
                                    sortByLatest = true,
                                    cards        = bookmarkedCards,
                                )
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
        vm.fetchBookmarkedCards()
    }
}
