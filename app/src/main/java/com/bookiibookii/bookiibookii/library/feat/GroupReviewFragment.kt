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
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.GroupReviewScreen
import com.bookiibookii.bookiibookii.library.vm.GroupReviewViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class GroupReviewFragment : BaseLibraryFragment() {

    private val vm: GroupReviewViewModel by viewModels()

    private val groupId: Int    get() = arguments?.getInt(ARG_GROUP_ID, -1) ?: -1
    private val groupName: String get() = arguments?.getString(ARG_GROUP_NAME, "") ?: ""
    private val bookTitle: String get() = arguments?.getString(ARG_BOOK_TITLE, "") ?: ""
    private val startDate: String get() = arguments?.getString(ARG_START_DATE, "") ?: ""
    private val endDate: String   get() = arguments?.getString(ARG_END_DATE, "") ?: ""

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
                        val reviewData = state.data
                        // 후기 없으면 토스트 후 접근 차단
                        val hasReviews = (reviewData?.messages?.isNotEmpty() == true || reviewData?.bookReviews?.isNotEmpty() == true)
                        if (!hasReviews) {
                            requireContext().showCustomToast("작성된 후기가 없습니다.", false)
                            return@GroupReviewScreen
                        }

                        // 그룹 메타만 전달. 책 목록·별점·내용은 편집 화면이 GET /reviews/book/me로 직접 프리필.
                        val resolvedGroupName = reviewData?.groupName ?: groupName
                        val resolvedDateRange = reviewData?.dateRange
                            ?: run {
                                val s = startDate
                                val e = endDate
                                when {
                                    s.isBlank() -> ""
                                    e.isBlank() -> "$s ~"
                                    else        -> "$s ~ $e"
                                }
                            }
                        val resolvedPartnerName  = reviewData?.partnerUsername ?: ""

                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                ReviewEditFragment.newInstance(
                                    groupId     = groupId,
                                    groupName   = resolvedGroupName,
                                    dateRange   = resolvedDateRange,
                                    partnerName = resolvedPartnerName,
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
        if (groupId != -1) {
            vm.loadReview(
                groupId   = groupId,
                groupName = groupName,
                startDate = startDate,
                endDate   = endDate,
            )
        }
    }

    companion object {
        private const val ARG_GROUP_ID    = "arg_group_id"
        private const val ARG_GROUP_NAME  = "arg_group_name"
        private const val ARG_BOOK_TITLE  = "arg_book_title"
        private const val ARG_START_DATE  = "arg_start_date"
        private const val ARG_END_DATE    = "arg_end_date"

        fun newInstance(
            groupId: Int    = -1,
            groupName: String = "",
            bookTitle: String = "",
            startDate: String = "",
            endDate: String   = "",
        ) = GroupReviewFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putString(ARG_GROUP_NAME, groupName)
                putString(ARG_BOOK_TITLE, bookTitle)
                putString(ARG_START_DATE, startDate)
                putString(ARG_END_DATE, endDate)
            }
        }
    }
}
