package com.bookiibookii.bookiibookii.myPage.Notice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.MypNotice
import com.bookiibookii.bookiibookii.databinding.ItemMypNoticeBinding

class MypNoticeAdapter(
    private val onClick: (MypNotice) -> Unit
) : RecyclerView.Adapter<MypNoticeAdapter.ViewHolder>() {

    private var items: List<MypNotice> = emptyList()

    fun submitList(list: List<MypNotice>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypNotice) {
            binding.itemNoticeTitleTv.text = item.title
            binding.itemNoticeContent.text = item.content
            binding.itemNoticeDateTv.text = item.date

            // 'N' 배지 표시 여부
            binding.itemNoticeNoticeV.visibility = if (item.isNew) View.VISIBLE else View.INVISIBLE

            // 클릭 리스너 연결
            binding.root.setOnClickListener { onClick(item) }
            binding.itemNoticeArrowIv.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}