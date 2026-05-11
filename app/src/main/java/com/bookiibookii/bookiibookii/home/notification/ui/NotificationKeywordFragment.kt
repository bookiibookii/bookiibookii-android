package com.bookiibookii.bookiibookii.home.notification.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.ComRetryBus
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.notification.NotificationCategory
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.home.notification.adapter.SystemAdapter
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository
import com.bookiibookii.bookiibookii.home.notification.model.NotificationUiItem
import com.bookiibookii.bookiibookii.home.notification.model.NotificationType
import com.bookiibookii.bookiibookii.home.notification.util.NotificationPayloadParser
import com.bookiibookii.bookiibookii.home.notification.util.TimeAgoFormatter
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModel
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModelFactory
import kotlinx.coroutines.launch

class HomKeywordNotiFragment : Fragment(R.layout.fragment_notification_keyword) {

    private val adapter = SystemAdapter { item ->
        if (item.isUnread) {
            viewModel.markAsRead(item.id)
        }
        handleKeywordNotificationClick(item.notification)
    }

    private val viewModel: NotificationViewModel by viewModels {
        val api = RetrofitClient.notiApi()
        val repo = NotificationRepository(api)
        NotificationViewModelFactory(repo, NotificationCategory.KEYWORD.name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_keyword_noti)
        val empty = view.findViewById<View>(R.id.include_empty_keyword)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 무한 스크롤
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount

                if (lastVisible >= total - 3 && !viewModel.state.value.isLoadingMore) {
                    viewModel.loadNextPage()
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ComRetryBus.retryFlow.collect {
                    viewModel.loadFirstPage()
                }
            }
        }

        // 첫 로딩
        viewModel.loadFirstPage()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { s ->
                    val uiItems = s.items.map { it.toUiItem() }
                    bind(uiItems, rv, empty)
                }
            }
        }
    }

    private fun NotificationItem.toUiItem(): NotificationUiItem {
        return NotificationUiItem(
            notification = this,
            timeText = TimeAgoFormatter.format(createdAt),
            bookTitle = "",
            isUnread = !isRead
        )
    }

    private fun bind(items: List<NotificationUiItem>, rv: RecyclerView, empty: View) {
        val hasData = items.isNotEmpty()
        rv.visibility = if (hasData) View.VISIBLE else View.GONE
        empty.visibility = if (hasData) View.GONE else View.VISIBLE
        if (hasData) adapter.setItems(items)
    }

    private fun handleKeywordNotificationClick(dto: NotificationItem) {
        val type = NotificationType.from(dto.type)

        when (type) {
            NotificationType.KEYWORD_GROUP_CREATED -> {
                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // TODO: GRP-010 해당 그룹 상세로 이동
                Toast.makeText(requireContext(), "TODO: GRP-010(해당 그룹) 이동 groupId=$groupId", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(requireContext(), "지원하지 않는 키워드 알림입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
