package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.ItemLibBookDetailReviewBinding // 새로 만든 XML

class LibraryCardReviewAdapter : RecyclerView.Adapter<LibraryCardReviewAdapter.ViewHolder>() {
    private var items = listOf<Any>() // 실제 데이터 모델로 변경하세요

    fun submitList(newList: List<Any>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLibBookDetailReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        // 예: holder.binding.libDetailReviewNameTv.text = item.userName
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemLibBookDetailReviewBinding) : RecyclerView.ViewHolder(binding.root)
}