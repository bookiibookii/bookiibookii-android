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
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeScreen
import com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class NoticeFragment : BaseMypageFragment() {

    private val viewModel: SettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                val notices by viewModel.notices.observeAsState(emptyList())

                NoticeScreen(
                    notices = notices,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onNoticeClick = { noticeId, title ->
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, NoticeDetailFragment.newInstance(noticeId, title))
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchNotices()
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
