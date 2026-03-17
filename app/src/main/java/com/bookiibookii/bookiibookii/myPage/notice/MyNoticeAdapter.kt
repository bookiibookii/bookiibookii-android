package com.bookiibookii.bookiibookii.myPage.notice

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.NoticeSummary
import com.bookiibookii.bookiibookii.databinding.ItemMypNoticeBinding

class MypNoticeAdapter(
    private var items: List<NoticeSummary>,
    private val prefs: SharedPreferences, // ★ 프래그먼트에서 한 번만 전달받음 (스크롤 성능 개선)
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MypNoticeAdapter.ViewHolder>() {

    fun submitList(newItems: List<NoticeSummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeSummary) {
            binding.itemNoticeTitleTv.text = item.title
            binding.itemNoticeContent.text = item.summary

            // ★ 중복 코드 삭제하고 DateUtils 재사용!
            binding.itemNoticeDateTv.text = DateUtils.formatDate(item.createdAt)

            // 이미 객체화된 prefs를 사용하므로 스크롤 시 딜레이가 없습니다.
            val isRead = prefs.getBoolean("notice_read_${item.id}", false)
            binding.itemNoticeNoticeV.visibility = if (isRead) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                if (!isRead) {
                    prefs.edit().putBoolean("notice_read_${item.id}", true).apply()
                    binding.itemNoticeNoticeV.visibility = View.GONE
                }
                onItemClick(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}