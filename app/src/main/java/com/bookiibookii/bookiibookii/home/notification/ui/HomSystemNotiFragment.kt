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
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.NotificationCategory
import com.bookiibookii.bookiibookii.data.model.NotificationItemDto
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.home.notification.adapter.HomNotiAdapter
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository
import com.bookiibookii.bookiibookii.home.notification.model.HomNotiItem
import com.bookiibookii.bookiibookii.home.notification.model.NotificationType
import com.bookiibookii.bookiibookii.home.notification.util.NotificationPayloadParser
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModel
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModelFactory
import kotlinx.coroutines.launch

class HomSystemNotiFragment : Fragment(R.layout.fragment_hom_system_noti) {

    private val adapter = HomNotiAdapter { item ->
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

        // 첫 로딩
        viewModel.loadFirstPage()

        // state 수신해서 리스트/empty 처리
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { s ->
                    val uiItems = s.items.map { it.toUiItem() }
                    bind(uiItems, rv, empty)
                    // s.errorMessage 필요하면 Toast/스낵바로 노출
                }
            }
        }
    }

    private fun NotificationItemDto.toUiItem(): HomNotiItem {
        return HomNotiItem(
            notification = this,
            timeText = createdAt, // TODO: createdAt → "10분 전" 형태로 포맷
            bookTitle = "",
            isUnread = !isRead
        )
    }

    private fun bind(items: List<HomNotiItem>, rv: RecyclerView, empty: View) {
        val hasData = items.isNotEmpty()
        rv.visibility = if (hasData) View.VISIBLE else View.GONE
        empty.visibility = if (hasData) View.GONE else View.VISIBLE
        if (hasData) adapter.setItems(items)
    }

    private fun handleNotificationClick(dto: com.bookiibookii.bookiibookii.data.model.NotificationItemDto) {

        val type = NotificationType.from(dto.type)

        when (type) {
            // 지금 데이터로 확정된 시스템 알림: 모두 groupId 기반으로 그룹 상세 이동 가능
            NotificationType.GROUP_JOIN_REQUEST,
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

                // TODO: 실제 그룹 상세 화면으로 교체
                // 예) startActivity(Intent(requireContext(), GroupDetailActivity::class.java).putExtra("groupId", groupId))
                Toast.makeText(requireContext(), "TODO: 그룹 상세로 이동 (groupId=$groupId)", Toast.LENGTH_SHORT).show()
            }

            // 교환 완료: 후기 화면이 있으면 후기 작성으로 이동, 없으면 그룹 상세 fallback
            NotificationType.TRACKER_EXCHANGE_COMPLETED -> {
                val groupId = NotificationPayloadParser.getGroupId(dto)
                if (groupId == null) {
                    Toast.makeText(requireContext(), "알림 이동에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // TODO: 후기 작성 화면이 있으면 여기로 이동
                // 예) startActivity(Intent(requireContext(), ReviewWriteActivity::class.java).putExtra("groupId", groupId))

                // TODO: 후기 화면이 없으면 그룹 상세로 이동
                Toast.makeText(requireContext(), "TODO: 후기 작성(or 그룹 상세)로 이동 (groupId=$groupId)", Toast.LENGTH_SHORT).show()
            }

            // TODO: 스샷에 있는 문의 답변 알림 - payload에 inquiryId 필요
            NotificationType.INQUIRY_ANSWERED -> {
                val inquiryId = NotificationPayloadParser.getInquiryId(dto)
                if (inquiryId == null) {
                    Toast.makeText(requireContext(), "TODO: inquiryId 스펙 필요", Toast.LENGTH_SHORT).show()
                    return
                }
                // TODO: 문의 상세 화면으로 이동
            }

            // TODO: 신고 처리 결과 알림 - payload에 reportId 필요
            NotificationType.REPORT_RESULT -> {
                val reportId = NotificationPayloadParser.getReportId(dto)
                if (reportId == null) {
                    Toast.makeText(requireContext(), "TODO: reportId 스펙 필요", Toast.LENGTH_SHORT).show()
                    return
                }
                // TODO: 신고 상세/결과 화면으로 이동
            }

            // TODO: 공지 알림 - payload에 noticeId 필요
            NotificationType.NOTICE_CREATED -> {
                val noticeId = NotificationPayloadParser.getNoticeId(dto)
                if (noticeId == null) {
                    Toast.makeText(requireContext(), "TODO: noticeId 스펙 필요", Toast.LENGTH_SHORT).show()
                    return
                }
                // TODO: 공지 상세 화면으로 이동
            }

            // TODO: 댓글 알림 - payload에 cardId 필요
            NotificationType.COMMENT_CREATED -> {
                val cardId = NotificationPayloadParser.getCardId(dto)
                if (cardId == null) {
                    Toast.makeText(requireContext(), "TODO: cardId 스펙 필요", Toast.LENGTH_SHORT).show()
                    return
                }
                // TODO: 카드 상세 화면으로 이동
            }

            // TODO: 그룹 신청 승인/거절 - applyId 또는 groupId만으로 가능한지 스펙 확인 필요
            NotificationType.GROUP_APPLY_ACCEPTED,
            NotificationType.GROUP_APPLY_REJECTED -> {
                // TODO: payload 스펙 확인 후 이동 화면 결정
                Toast.makeText(requireContext(), "TODO: 그룹 신청 결과 이동 경로/스펙 필요", Toast.LENGTH_SHORT).show()
            }

            NotificationType.UNKNOWN -> {
                // 새 타입이 추가돼도 크래시 안 나게 방어
                Toast.makeText(requireContext(), "지원하지 않는 알림입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
