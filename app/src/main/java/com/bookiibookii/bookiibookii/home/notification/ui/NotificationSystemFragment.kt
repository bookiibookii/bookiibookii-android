package com.bookiibookii.bookiibookii.home.notification.ui

import android.content.Intent
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
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.ComRetryBus
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.notification.NotificationCategory
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.group.GroupJoinManagementActivity
import com.bookiibookii.bookiibookii.home.notification.adapter.SystemAdapter
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository
import com.bookiibookii.bookiibookii.home.notification.model.NotificationUiItem
import com.bookiibookii.bookiibookii.home.notification.model.NotificationType
import com.bookiibookii.bookiibookii.home.notification.util.NotificationPayloadParser
import com.bookiibookii.bookiibookii.home.notification.util.TimeAgoFormatter
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModel
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModelFactory
import kotlinx.coroutines.launch

class NotificationSystemFragment : Fragment(R.layout.fragment_notification_system) {

    private val adapter = SystemAdapter { item ->
        if (item.isUnread) {
            viewModel.markAsRead(item.id)
        }
        handleNotificationClick(item.notification)
    }

    private val viewModel: NotificationViewModel by viewModels {
        val api = RetrofitClient.api()
        val repo = NotificationRepository(api)
        NotificationViewModelFactory(
            repo,
            NotificationCategory.SYSTEM.name
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_system_noti)
        val empty = view.findViewById<View>(R.id.include_empty_system)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 무한 스크롤: 바닥 근처 도달 시 다음 페이지 로드
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount

                // 마지막에서 3개 전쯤 도달하면 다음 페이지 요청
                if (lastVisible >= total - 3 && !viewModel.state.value.isLoadingMore) {
                    viewModel.loadNextPage()
                }
            }
        })

        // retry 이벤트 받으면 첫 페이지 다시 로드
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ComRetryBus.retryFlow.collect {
                    viewModel.loadFirstPage()
                }
            }
        }

        // 첫 로딩
        viewModel.loadFirstPage()

        // state 수신해서 리스트/empty 처리
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
            timeText = TimeAgoFormatter.format(createdAt ?: ""),
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

    private fun handleNotificationClick(dto: NotificationItem) {

        val type = NotificationType.from(dto.type)

        when (type) {

            // GRP-030 (요청관리)
            NotificationType.GROUP_JOIN_REQUEST -> {
                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(requireContext(), GroupJoinManagementActivity::class.java).apply {
                    putExtra("GROUP_ID", groupId)
                }
                startActivity(intent)
            }

            // GRP-001 (리스트)
            NotificationType.GROUP_MATCH_REJECTED,
            NotificationType.GROUP_MATCH_AUTO_REJECTED,
            NotificationType.GROUP_MATCH_FAILED_BY_EXPIRE,
            NotificationType.GROUP_MATCH_FAILED_BY_CAPACITY -> {
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("NAV_ACTION", "OPEN_GROUP")
                }
                startActivity(intent)
                requireActivity().finish()
            }

            // 문의하기 or GRP-001
            NotificationType.GROUP_DELETED -> {
                // TODO: 문의하기 화면 있으면 문의하기로, 없으면 GRP-001로 fallback
                Toast.makeText(requireContext(), "TODO: 문의하기(or GRP-001) 이동", Toast.LENGTH_SHORT).show()
            }

            // GRP-010 (그룹 상세/댓글)
            NotificationType.GROUP_COMMENT_CREATED -> {
                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
                    putExtra("GROUP_ID", groupId)
                }
                startActivity(intent)
            }

            // TRK-010 (트래커)
            NotificationType.GROUP_MATCH_SUCCESS,
            NotificationType.TRACKER_READING_STARTED,
            NotificationType.TRACKER_PERIOD_EXTENDED,
            NotificationType.TRACKER_READING_FINISHED,
            NotificationType.TRACKER_SHIPMENT_REGISTERED,
            NotificationType.TRACKER_DELIVERY_CONFIRMED,
            NotificationType.TRACKER_RETURN_SHIPMENT_REGISTERED -> {

                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // TODO: TRK-010 트래커 화면으로 이동
                Toast.makeText(requireContext(), "TODO: TRK-010(트래커) 이동 groupId=$groupId", Toast.LENGTH_SHORT).show()
            }

            // TRK-030 (후기작성)
            NotificationType.TRACKER_EXCHANGE_COMPLETED -> {
                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // TODO: TRK-030 후기작성 화면으로 이동
                Toast.makeText(requireContext(), "TODO: TRK-030(후기작성) 이동 groupId=$groupId", Toast.LENGTH_SHORT).show()
            }

            NotificationType.KEYWORD_GROUP_CREATED -> {
                // 시스템 탭에서는 원래 안 들어와야 함 (category=KEYWORD)
                Toast.makeText(requireContext(), "키워드 알림은 키워드 탭에서 확인해주세요.", Toast.LENGTH_SHORT).show()
            }

            NotificationType.UNKNOWN -> {
                Toast.makeText(requireContext(), "지원하지 않는 알림입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
