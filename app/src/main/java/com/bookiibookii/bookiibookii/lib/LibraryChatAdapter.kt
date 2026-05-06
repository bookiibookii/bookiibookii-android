package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.library.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemLibCardChatBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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

            // ★ [수정됨] 단순 문자열 자르기 -> UTC 시간 계산 로직 적용
            binding.libChatTimeTv.text = DateUtils.calculateTimeAgo(item.createdAt)

            Glide.with(itemView.context)
                .load(item.writer.profileImageUrl)
                .placeholder(R.drawable.bg_round_20dp_gray200)
                .transform(CenterCrop(), RoundedCorners(dpToPx(itemView.context, 12)))
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

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemCount(): Int = items.size
}