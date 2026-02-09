package com.bookiibookii.bookiibookii.home.notification.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.bookiibookii.bookiibookii.home.notification.adapter.HomNotiAdapter
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository
import com.bookiibookii.bookiibookii.home.notification.model.HomNotiItem
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModel
import com.bookiibookii.bookiibookii.home.notification.vm.NotificationViewModelFactory
import kotlinx.coroutines.launch

class HomKeywordNotiFragment : Fragment(R.layout.fragment_hom_keyword_noti) {

    private val adapter = HomNotiAdapter { item ->
        if (item.isUnread) {
            viewModel.markAsRead(item.id)
        }

        // TODO: 키워드 알림 type/payload 기반 이동 분기
        // 일단 설정 화면으로 보내거나, 그룹 상세로 보내는 식으로 임시 처리 가능
        val intent = Intent(requireContext(), HomKeywordNotiSettingActivity::class.java)
        startActivity(intent)
    }

    private val viewModel: NotificationViewModel by viewModels {
        val api = RetrofitClient.api()
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

                if (lastVisible >= total - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        // 첫 로딩
        viewModel.loadFirstPage()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { s ->
                    val uiItems = s.items.map { it.toUiItem() }
                    bind(uiItems, rv, empty)
                    // TODO: s.errorMessage 필요하면 Toast로 표시
                }
            }
        }
    }

    private fun NotificationItemDto.toUiItem(): HomNotiItem {
        return HomNotiItem(
            notification = this,
            timeText = createdAt,
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
}