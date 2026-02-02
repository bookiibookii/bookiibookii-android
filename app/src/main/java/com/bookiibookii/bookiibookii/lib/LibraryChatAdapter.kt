package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.ItemLibCardChatBinding

class LibraryChatAdapter(
    private val items: List<String> // 실제로는 ChatData 객체 리스트
) : RecyclerView.Adapter<LibraryChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: ItemLibCardChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String) {
            binding.libChatChatTv.text = content
            binding.libChatNickTv.text = "User${bindingAdapterPosition}" // 더미 닉네임
            // 프로필 이미지, 시간 등 바인딩
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