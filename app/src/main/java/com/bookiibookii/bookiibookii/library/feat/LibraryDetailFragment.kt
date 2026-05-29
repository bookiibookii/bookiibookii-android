package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.LibraryDetailScreen
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryDetailFragment : BaseLibraryFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                LibraryDetailScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onAddTextCard = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryAddCardFragment.newInstance(AddCardMode.TEXT))
                            .addToBackStack(null)
                            .commit()
                    },
                    onAddPhotoCard = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryAddCardFragment.newInstance(AddCardMode.PHOTO))
                            .addToBackStack(null)
                            .commit()
                    },
                    onCardClick = { initialIndex, sortedCards, sortByLatest ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                ReadingCardDetailFragment.newInstance(
                                    initialIndex = initialIndex,
                                    sortByLatest = sortByLatest,
                                    cards = sortedCards,
                                )
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    onReviewClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, GroupReviewFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
