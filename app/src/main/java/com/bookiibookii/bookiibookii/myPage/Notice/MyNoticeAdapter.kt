package com.bookiibookii.bookiibookii.myPage.Notice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.data.model.NoticeSummary
import com.bookiibookii.bookiibookii.databinding.ItemMypNoticeBinding // 제공해주신 XML 바인딩
import java.text.SimpleDateFormat
import java.util.Locale

class MypNoticeAdapter(
    private var items: List<NoticeSummary>,
    private val onItemClick: (Int) -> Unit // 클릭 시 ID 전달
) : RecyclerView.Adapter<MypNoticeAdapter.ViewHolder>() {

    fun submitList(newItems: List<NoticeSummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeSummary) {
            binding.itemNoticeTitleTv.text = item.title
            binding.itemNoticeContent.text = item.summary // XML ID: item_notice_content

            // 날짜 포맷팅 (2026-02-03T... -> 2026.02.03)
            binding.itemNoticeDateTv.text = formatDate(item.createdAt)

            // 아이템 클릭 시 상세 화면으로 이동
            binding.root.setOnClickListener {
                onItemClick(item.id)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
                val date = parser.parse(dateString)
                formatter.format(date ?: return dateString)
            } catch (e: Exception) {
                dateString // 변환 실패 시 원본 그대로
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