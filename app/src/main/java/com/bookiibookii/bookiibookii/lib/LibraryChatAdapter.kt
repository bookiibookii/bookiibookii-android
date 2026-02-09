package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemLibCardChatBinding

class LibraryChatAdapter(
    private var items: List<CommentItem>
) : RecyclerView.Adapter<LibraryChatAdapter.ChatViewHolder>() {

    fun submitList(newItems: List<CommentItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemLibCardChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentItem) {
            binding.libChatChatTv.text = item.content
            binding.libChatNickTv.text = item.writer.name

            // 시간 표시 (예: 2026.02.06)
            if (item.createdAt.length >= 10) {
                binding.libChatTimeTv.text = item.createdAt.substring(0, 10).replace("-", ".")
            }

            Glide.with(itemView.context)
                .load(item.writer.profileImage)
                .placeholder(R.drawable.bg_round_20dp_gray200)
                .circleCrop()
                .into(binding.libChatProfileIv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemLibCardChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}