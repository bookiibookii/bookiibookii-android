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
import com.bookiibookii.bookiibookii.library.ui.GroupReviewScreen
import com.bookiibookii.bookiibookii.library.vm.GroupReviewViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class GroupReviewFragment : BaseLibraryFragment() {

    private val vm: GroupReviewViewModel by viewModels()

    private val groupName: String get() = arguments?.getString(ARG_GROUP_NAME, "") ?: ""
    private val bookTitle: String get() = arguments?.getString(ARG_BOOK_TITLE, "") ?: ""
    private val startDate: String get() = arguments?.getString(ARG_START_DATE, "") ?: ""
    private val endDate: String get() = arguments?.getString(ARG_END_DATE, "") ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.uiState.collectAsState()
            BookiiBookiiTheme {
                GroupReviewScreen(
                    data        = state.data,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onEditClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewEditFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.loadReview(
            groupName = groupName,
            bookTitle = bookTitle,
            startDate = startDate,
            endDate   = endDate,
        )
    }

    companion object {
        private const val ARG_GROUP_NAME = "arg_group_name"
        private const val ARG_BOOK_TITLE = "arg_book_title"
        private const val ARG_START_DATE = "arg_start_date"
        private const val ARG_END_DATE   = "arg_end_date"

        fun newInstance(
            groupName: String = "",
            bookTitle: String = "",
            startDate: String = "",
            endDate: String = "",
        ) = GroupReviewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GROUP_NAME, groupName)
                putString(ARG_BOOK_TITLE, bookTitle)
                putString(ARG_START_DATE, startDate)
                putString(ARG_END_DATE, endDate)
            }
        }
    }
}
