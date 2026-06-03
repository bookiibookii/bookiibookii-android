package com.bookiibookii.bookiibookii.mypage.feat.setting

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
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeDetailScreen
import com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class NoticeDetailFragment : BaseMypageFragment() {

    companion object {
        private const val ARG_NOTICE_ID = "notice_id"
        private const val ARG_TITLE = "title"

        fun newInstance(noticeId: Long, title: String) = NoticeDetailFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_NOTICE_ID, noticeId)
                putString(ARG_TITLE, title)
            }
        }
    }

    private val viewModel: SettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val title = arguments?.getString(ARG_TITLE) ?: ""
        setContent {
            BookiiBookiiTheme {
                val noticeDetail by viewModel.noticeDetail.observeAsState()

                NoticeDetailScreen(
                    title = title,
                    content = noticeDetail?.content ?: "",
                    createdAt = noticeDetail?.createdAt ?: "",
                    onBackClick = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noticeId = arguments?.getLong(ARG_NOTICE_ID) ?: return
        viewModel.fetchNoticeDetail(noticeId)
        collectEvents()
    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is SettingViewModel.Event.ShowToast ->
                        requireContext().showCustomToast(event.message, !event.message.contains("실패") && !event.message.contains("오류"))
                    else -> Unit
                }
            }
        }
    }
}
