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
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.uiState.collectAsState()
            BookiiBookiiTheme {
                ReviewEditScreen(
                    groupName       = groupName,
                    dateRange       = dateRange,
                    partnerName     = partnerName,
                    books                 = state.books,
                    initialRatings        = state.initialRatings,
                    initialComments       = state.initialComments,
                    initialIsPartnerGood  = state.initialIsPartnerGood,
                    initialPartnerComment = state.initialPartnerComment,
                    onBackClick           = { parentFragmentManager.popBackStack() },
                    onSubmit        = { ratings, bookComments, isPartnerGood, partnerComment ->
                        if (groupId == -1) {
                            requireContext().showCustomToast("그룹 정보를 찾을 수 없습니다.", false)
                            return@ReviewEditScreen
                        }
                        vm.submit(
                            groupId        = groupId,
                            ratings        = ratings,
                            bookComments   = bookComments,
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
        vm.load(groupId)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.event.collect { event ->
                when (event) {
                    is ReviewEditViewModel.ReviewEditEvent.Success -> {
                        requireContext().showCustomToast("후기가 저장되었습니다.", true)
                        parentFragmentManager.popBackStack()
                    }
                    is ReviewEditViewModel.ReviewEditEvent.Error -> {
                        requireContext().showCustomToast(event.message, false)
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

        fun newInstance(
            groupId: Int = -1,
            groupName: String = "",
            dateRange: String = "",
            partnerName: String = "",
        ) = ReviewEditFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putString(ARG_GROUP_NAME, groupName)
                putString(ARG_DATE_RANGE, dateRange)
                putString(ARG_PARTNER_NAME, partnerName)
            }
        }
    }
}
