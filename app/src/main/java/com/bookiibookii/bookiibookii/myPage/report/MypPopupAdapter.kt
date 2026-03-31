package com.bookiibookii.bookiibookii.myPage.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.ItemMypDropdownBinding // 아래 XML 참조

class PopupAdapter(
    private val items: List<String>,
    private val onItemClick: (String, Int) -> Unit // 이름, 인덱스 반환
) : RecyclerView.Adapter<PopupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypDropdownBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.tvDropdownItem.text = item
            binding.root.setOnClickListener {
                onItemClick(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}