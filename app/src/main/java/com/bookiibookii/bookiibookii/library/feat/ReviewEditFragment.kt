package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.ReviewBookInfo
import com.bookiibookii.bookiibookii.library.ui.ReviewEditScreen
import com.bookiibookii.bookiibookii.library.vm.ReviewEditViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class ReviewEditFragment : BaseLibraryFragment() {

    private val vm: ReviewEditViewModel by viewModels()

    private val groupId: Int         get() = arguments?.getInt(ARG_GROUP_ID, -1) ?: -1
    private val groupName: String    get() = arguments?.getString(ARG_GROUP_NAME, "") ?: ""
    private val dateRange: String    get() = arguments?.getString(ARG_DATE_RANGE, "") ?: ""
    private val partnerName: String  get() = arguments?.getString(ARG_PARTNER_NAME, "") ?: ""
    private val bookTitles: List<String>
        get() = arguments?.getStringArrayList(ARG_BOOK_TITLES) ?: emptyList()
    private val bookAuthors: List<String>
        get() = arguments?.getStringArrayList(ARG_BOOK_AUTHORS) ?: emptyList()
    private val initialRatings: List<Double>
        get() = arguments?.getDoubleArray(ARG_INITIAL_RATINGS)?.toList() ?: emptyList()
    private val initialComments: List<String>
        get() = arguments?.getStringArrayList(ARG_INITIAL_COMMENTS) ?: emptyList()

    private fun buildBooks(): List<ReviewBookInfo> =
        bookTitles.mapIndexed { i, title ->
            ReviewBookInfo(
                title  = title,
                author = bookAuthors.getOrElse(i) { "" },
                genre  = "",
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                ReviewEditScreen(
                    groupName       = groupName,
                    dateRange       = dateRange,
                    partnerName     = partnerName,
                    books           = buildBooks(),
                    initialRatings  = initialRatings,
                    initialComments = initialComments,
                    onBackClick     = { parentFragmentManager.popBackStack() },
                    onSubmit        = { ratings, bookComments, isPartnerGood, partnerComment ->
                        if (groupId == -1) {
                            Toast.makeText(requireContext(), "그룹 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return@ReviewEditScreen
                        }
                        val star    = ratings.firstOrNull() ?: 0.0
                        val comment = bookComments.firstOrNull().orEmpty()
                        vm.submit(
                            groupId        = groupId,
                            bookStar       = star,
                            bookComment    = comment,
                            isPartnerGood  = isPartnerGood,
                            partnerComment = partnerComment,
                        )
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.event.collect { event ->
                when (event) {
                    is ReviewEditViewModel.ReviewEditEvent.Success -> {
                        Toast.makeText(requireContext(), "후기가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    is ReviewEditViewModel.ReviewEditEvent.Error -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_GROUP_ID        = "arg_group_id"
        private const val ARG_GROUP_NAME      = "arg_group_name"
        private const val ARG_DATE_RANGE      = "arg_date_range"
        private const val ARG_PARTNER_NAME    = "arg_partner_name"
        private const val ARG_BOOK_TITLES     = "arg_book_titles"
        private const val ARG_BOOK_AUTHORS    = "arg_book_authors"
        private const val ARG_INITIAL_RATINGS = "arg_initial_ratings"
        private const val ARG_INITIAL_COMMENTS = "arg_initial_comments"

        fun newInstance(
            groupId: Int = -1,
            groupName: String = "",
            dateRange: String = "",
            partnerName: String = "",
            bookTitles: List<String> = emptyList(),
            bookAuthors: List<String> = emptyList(),
            initialRatings: List<Double> = emptyList(),
            initialComments: List<String> = emptyList(),
        ) = ReviewEditFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putString(ARG_GROUP_NAME, groupName)
                putString(ARG_DATE_RANGE, dateRange)
                putString(ARG_PARTNER_NAME, partnerName)
                putStringArrayList(ARG_BOOK_TITLES, ArrayList(bookTitles))
                putStringArrayList(ARG_BOOK_AUTHORS, ArrayList(bookAuthors))
                putDoubleArray(ARG_INITIAL_RATINGS, initialRatings.toDoubleArray())
                putStringArrayList(ARG_INITIAL_COMMENTS, ArrayList(initialComments))
            }
        }
    }
}
