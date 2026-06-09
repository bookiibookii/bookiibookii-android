package com.bookiibookii.bookiibookii.library.feat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.LibraryDetailBook
import com.bookiibookii.bookiibookii.library.ui.LibraryDetailScreen
import com.bookiibookii.bookiibookii.library.vm.LibraryDetailViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class LibraryDetailFragment : BaseLibraryFragment() {

    private val vm: LibraryDetailViewModel by viewModels()

    private val groupId: Int       get() = arguments?.getInt(ARG_GROUP_ID, -1) ?: -1
    private val memberBookId: Int   get() = arguments?.getInt(ARG_MEMBER_BOOK_ID, -1) ?: -1
    private val groupName: String   get() = arguments?.getString(ARG_GROUP_NAME, "") ?: ""
    private val bookTitle: String   get() = arguments?.getString(ARG_BOOK_TITLE, "") ?: ""
    private val author: String      get() = arguments?.getString(ARG_AUTHOR, "") ?: ""
    private val coverUrl: String    get() = arguments?.getString(ARG_COVER_URL, "") ?: ""
    private val startDate: String   get() = arguments?.getString(ARG_START_DATE, "") ?: ""
    private val endDate: String     get() = arguments?.getString(ARG_END_DATE, "") ?: ""
    private val rating: Double      get() = arguments?.getDouble(ARG_RATING, 0.0) ?: 0.0
    private val isDone: Boolean     get() = arguments?.getBoolean(ARG_IS_DONE, false) ?: false
    private val progressRate: Int   get() = arguments?.getInt(ARG_PROGRESS_RATE, 0) ?: 0

    private fun buildBook() = if (bookTitle.isNotBlank()) LibraryDetailBook(
        groupId      = groupId,
        memberBookId = memberBookId,
        groupName    = groupName,
        title        = bookTitle,
        author       = author,
        coverUrl     = coverUrl.ifBlank { null },
        isDone       = isDone,
        progressRate = progressRate,
        rating       = rating,
        startDate    = startDate,
        endDate      = endDate.ifBlank { null },
    ) else null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.uiState.collectAsState()
            BookiiBookiiTheme {
                LibraryDetailScreen(
                    book             = buildBook(),
                    cards            = state.cards,
                    isRepresentative = state.isRepresentative,
                    onBackClick      = { parentFragmentManager.popBackStack() },
                    onAddTextCard    = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryAddCardFragment.newInstance(AddCardMode.TEXT, memberBookId, bookTitle))
                            .addToBackStack(null)
                            .commit()
                    },
                    onAddPhotoCard   = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryAddCardFragment.newInstance(AddCardMode.PHOTO, memberBookId, bookTitle))
                            .addToBackStack(null)
                            .commit()
                    },
                    onCardClick      = { initialIndex, sortedCards, sortByLatest ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                ReadingCardDetailFragment.newInstance(
                                    initialIndex = initialIndex,
                                    sortByLatest = sortByLatest,
                                    cards        = sortedCards,
                                )
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    onReviewClick          = {
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                GroupReviewFragment.newInstance(
                                    groupId   = groupId,
                                    groupName = groupName,
                                    bookTitle = bookTitle,
                                    startDate = startDate,
                                    endDate   = endDate,
                                )
                            )
                            .addToBackStack(null)
                            .commit()
                    },
                    onRepresentativeAdd    = { vm.addRepresentative(memberBookId) },
                    onRepresentativeRemove = { vm.removeRepresentative(state.representativeUserBookId) },
                    onAladinClick          = { title ->
                        val url = "https://www.aladin.co.kr/search/wsearchresult.aspx?SearchWord=${Uri.encode(title)}"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    onDeleteBook           = {
                        vm.deleteMemberBook(memberBookId) { parentFragmentManager.popBackStack() }
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel 이벤트 구독 — viewLifecycleOwner 스코프로 누수 방지
        viewLifecycleOwner.lifecycleScope.launch {
            vm.event.collect { msg ->
                requireContext().showCustomToast(msg, !msg.contains("실패") && !msg.contains("오류"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (groupId != -1) vm.fetchGroupCards(groupId, bookTitle)
        // 대표책 상태 조회 (bookTitle로 북쉘프 매칭)
        if (memberBookId != -1 && bookTitle.isNotBlank()) {
            vm.checkRepresentativeStatus(memberBookId, bookTitle)
        }
    }

    companion object {
        private const val ARG_GROUP_ID       = "arg_group_id"
        private const val ARG_MEMBER_BOOK_ID = "arg_member_book_id"
        private const val ARG_GROUP_NAME     = "arg_group_name"
        private const val ARG_BOOK_TITLE     = "arg_book_title"
        private const val ARG_AUTHOR         = "arg_author"
        private const val ARG_COVER_URL      = "arg_cover_url"
        private const val ARG_START_DATE     = "arg_start_date"
        private const val ARG_END_DATE       = "arg_end_date"
        private const val ARG_RATING         = "arg_rating"
        private const val ARG_IS_DONE        = "arg_is_done"
        private const val ARG_PROGRESS_RATE  = "arg_progress_rate"

        fun newInstance(
            groupId: Int,
            memberBookId: Int,
            groupName: String = "",
            bookTitle: String = "",
            author: String = "",
            coverUrl: String = "",
            startDate: String = "",
            endDate: String = "",
            rating: Double = 0.0,
            isDone: Boolean = false,
            progressRate: Int = 0,
        ) = LibraryDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putInt(ARG_MEMBER_BOOK_ID, memberBookId)
                putString(ARG_GROUP_NAME, groupName)
                putString(ARG_BOOK_TITLE, bookTitle)
                putString(ARG_AUTHOR, author)
                putString(ARG_COVER_URL, coverUrl)
                putString(ARG_START_DATE, startDate)
                putString(ARG_END_DATE, endDate)
                putDouble(ARG_RATING, rating)
                putBoolean(ARG_IS_DONE, isDone)
                putInt(ARG_PROGRESS_RATE, progressRate)
            }
        }
    }
}
