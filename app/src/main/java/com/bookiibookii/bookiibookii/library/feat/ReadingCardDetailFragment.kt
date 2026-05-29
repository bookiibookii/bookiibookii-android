package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardDetailScreen
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class ReadingCardDetailFragment : BaseLibraryFragment() {

    private val initialIndex get() = arguments?.getInt(ARG_INDEX, 0) ?: 0
    private val sortByLatest get() = arguments?.getBoolean(ARG_SORT, true) ?: true

    // Bundle에서 카드 목록 복원 (직렬화: usernames, contents, pages, types, bookmarks, dates)
    private val cards: List<ReadingCard>
        get() {
            val args = arguments ?: return emptyList()
            val usernames  = args.getStringArray(ARG_USERNAMES)  ?: return emptyList()
            val contents   = args.getStringArray(ARG_CONTENTS)   ?: return emptyList()
            val pages      = args.getStringArray(ARG_PAGES)       ?: return emptyList()
            val types      = args.getIntArray(ARG_TYPES)          ?: return emptyList()
            val bookmarks  = args.getBooleanArray(ARG_BOOKMARKS)  ?: return emptyList()
            val dates      = args.getStringArray(ARG_DATES)       ?: return emptyList()
            return usernames.indices.map { i ->
                ReadingCard(
                    username    = usernames[i],
                    content     = contents[i],
                    page        = pages[i],
                    type        = if (types[i] == 0) ReadingCardType.PHOTO else ReadingCardType.QUOTE,
                    isBookmarked = bookmarks[i],
                    date        = dates[i],
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                ReadingCardDetailScreen(
                    cards = cards,
                    initialIndex = initialIndex,
                    sortByLatest = sortByLatest,
                    onBackClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }

    companion object {
        private const val ARG_INDEX     = "arg_index"
        private const val ARG_SORT      = "arg_sort"
        private const val ARG_USERNAMES = "arg_usernames"
        private const val ARG_CONTENTS  = "arg_contents"
        private const val ARG_PAGES     = "arg_pages"
        private const val ARG_TYPES     = "arg_types"
        private const val ARG_BOOKMARKS = "arg_bookmarks"
        private const val ARG_DATES     = "arg_dates"

        fun newInstance(
            initialIndex: Int,
            sortByLatest: Boolean,
            cards: List<ReadingCard>,
        ) = ReadingCardDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, initialIndex)
                putBoolean(ARG_SORT, sortByLatest)
                putStringArray(ARG_USERNAMES,  cards.map { it.username }.toTypedArray())
                putStringArray(ARG_CONTENTS,   cards.map { it.content }.toTypedArray())
                putStringArray(ARG_PAGES,      cards.map { it.page }.toTypedArray())
                putIntArray   (ARG_TYPES,      cards.map { if (it.type == ReadingCardType.PHOTO) 0 else 1 }.toIntArray())
                putBooleanArray(ARG_BOOKMARKS, cards.map { it.isBookmarked }.toBooleanArray())
                putStringArray(ARG_DATES,      cards.map { it.date }.toTypedArray())
            }
        }
    }
}
