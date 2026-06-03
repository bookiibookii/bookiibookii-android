package com.bookiibookii.bookiibookii.mypage.feat.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.detail.MyBookshelfScreen
import com.bookiibookii.bookiibookii.mypage.vm.BookshelfViewModel
import com.bookiibookii.bookiibookii.mypage.vm.SortOrder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class MyBookshelfFragment : BaseMypageFragment() {

    private val viewModel: BookshelfViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                val sortedCompletedBooks by viewModel.sortedCompletedBooks.observeAsState(emptyList())
                val favoriteBooks by viewModel.favoriteBooks.observeAsState(emptyList())
                val representativeBooks by viewModel.representativeBooks.observeAsState(emptyList())
                val representativeTitles by viewModel.representativeTitles.observeAsState(emptySet())
                val sortOrder by viewModel.sortOrder.observeAsState(SortOrder.LATEST)
                val bookSearchState by viewModel.bookSearchState.observeAsState()

                MyBookshelfScreen(
                    sortedCompletedBooks = sortedCompletedBooks,
                    favoriteBooks = favoriteBooks,
                    representativeBooks = representativeBooks,
                    representativeTitles = representativeTitles,
                    sortOrder = sortOrder,
                    bookSearchState = bookSearchState,
                    onBack = { parentFragmentManager.popBackStack() },
                    onSortOrderChange = { order -> viewModel.setSortOrder(order) },
                    onSearchBooks = { query -> viewModel.searchBooks(query) },
                    onClearBookSearch = { viewModel.clearBookSearch() },
                    onDeleteRepresentativeBook = { userBookId -> viewModel.deleteRepresentativeBook(userBookId) },
                    onReorderRepresentativeBook = { userBookId, newOrder -> viewModel.reorderRepresentativeBook(userBookId, newOrder) },
                    onAddFavoriteBook = { isbn13 -> viewModel.addFavoriteBook(isbn13) },
                    onDeleteFavoriteBook = { userBookId -> viewModel.deleteFavoriteBook(userBookId) },
                    onReplaceFavoriteBook = { oldId, isbn13 -> viewModel.replaceFavoriteBook(oldId, isbn13) },
                    onAddRepresentativeBook = { memberBookId -> viewModel.addRepresentativeBook(memberBookId) },
                    onRemoveRepresentativeBook = { userBookId -> viewModel.deleteRepresentativeBook(userBookId) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectEvents()
    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is BookshelfViewModel.Event.ShowToast ->
                        requireContext().showCustomToast(event.message, !event.message.contains("실패") && !event.message.contains("오류"))
                }
            }
        }
    }
}
