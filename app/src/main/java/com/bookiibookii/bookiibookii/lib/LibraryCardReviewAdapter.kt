package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.data.model.TogetherComment
import com.bookiibookii.bookiibookii.databinding.ItemLibBookDetailReviewBinding

class LibraryCardReviewAdapter : RecyclerView.Adapter<LibraryCardReviewAdapter.ViewHolder>() {
    private var items = listOf<TogetherComment>()

    fun submitList(newList: List<TogetherComment>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLibBookDetailReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 1. 닉네임
        holder.binding.libDetailReviewNameTv.text = item.nickname

        // 2. 내용 세팅 (따옴표 추가 및 빈 값 처리)
        val commentText = if (item.comment.isNullOrBlank()) {
            "\"아직 한줄 평을 남기지 않았어요.\""
        } else {
            "\"${item.comment}\""
        }

        holder.binding.libDetailReviewTextTv.text = commentText
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemLibBookDetailReviewBinding) : RecyclerView.ViewHolder(binding.root)
}