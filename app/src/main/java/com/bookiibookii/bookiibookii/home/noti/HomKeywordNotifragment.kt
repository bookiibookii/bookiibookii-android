package com.bookiibookii.bookiibookii.home.noti

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class HomKeywordNotiFragment : Fragment(R.layout.fragment_hom_keyword_noti) {

    private val adapter = HomNotiAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_keyword_noti)
        val empty = view.findViewById<View>(R.id.include_empty_keyword)

        // RecyclerView 기본 세팅
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // TODO: 서버 연결 전 더미 데이터
        val items = listOf(
            HomNotiItem(
                title = "관심 키워드 알림이에요",
                body = "‘책 제목’이 관심 키워드와 일치해요. 지금 확인해보세요!",
                timeText = "10분 전",
                bookTitle = "책 제목",
                isUnread = true
            )
        )

        bind(items, rv, empty)
    }

    private fun bind(items: List<HomNotiItem>, rv: RecyclerView, empty: View) {
        val hasData = items.isNotEmpty()

        rv.visibility = if (hasData) View.VISIBLE else View.GONE
        empty.visibility = if (hasData) View.GONE else View.VISIBLE

        if (hasData) {
            adapter.setItems(items)
        }
    }
}