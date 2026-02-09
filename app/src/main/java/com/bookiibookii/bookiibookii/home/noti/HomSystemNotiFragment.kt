package com.bookiibookii.bookiibookii.home.noti

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.home.notiSetting.HomKeywordNotiSettingActivity

class HomSystemNotiFragment : Fragment(R.layout.fragment_hom_system_noti) {

    private val adapter = HomNotiAdapter { item ->
        // TODO: 알림 타입별 분기 처리
        val intent = Intent(requireContext(), HomKeywordNotiSettingActivity::class.java)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_system_noti)
        val empty = view.findViewById<View>(R.id.include_empty_system)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val items = listOf(
            HomNotiItem(
                title = "똑딱! 새로운 참여 요청이 왔어요",
                body = "닉네임 님이 책 제목 그룹에 함께하고 싶어 해요. 프로필을 확인해볼까요?",
                timeText = "5분 전",
                bookTitle = "책 제목",
                isUnread = true
            ),
            HomNotiItem(
                title = "새로운 댓글이 달렸어요",
                body = "닉네임 님이 책 제목 그룹에 댓글을 남겼어요. 확인해볼까요?",
                timeText = "30분 전",
                bookTitle = "책 제목",
                isUnread = false
            )
        )

        bind(items, rv, empty)
    }

    private fun bind(items: List<HomNotiItem>, rv: RecyclerView, empty: View) {
        val hasData = items.isNotEmpty()
        rv.visibility = if (hasData) View.VISIBLE else View.GONE
        empty.visibility = if (hasData) View.GONE else View.VISIBLE
        if (hasData) adapter.setItems(items)
    }
}