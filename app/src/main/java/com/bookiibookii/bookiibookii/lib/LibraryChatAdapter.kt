package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemLibCardChatBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
            binding.libChatTimeTv.text = calculateTimeAgo(item.createdAt)

            Glide.with(itemView.context)
                .load(item.writer.profileImageUrl)
                .placeholder(R.drawable.bg_round_20dp_gray200)
                .transform(CenterCrop(), RoundedCorners(dpToPx(itemView.context, 12)))
                .into(binding.libChatProfileIv)
        }

        // ★ 시간 계산 함수 추가 (서버 UTC -> 한국 시간 변환)
        private fun calculateTimeAgo(serverTime: String): String {
            if (serverTime.isEmpty()) return ""
            try {
                // 1. 서버 시간 포맷 (UTC)
                val format = if (serverTime.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
                val parser = SimpleDateFormat(format, Locale.getDefault())
                parser.timeZone = TimeZone.getTimeZone("UTC") // 서버는 UTC 기준

                val date = parser.parse(serverTime) ?: return serverTime.substring(0, 10)

                // 2. 현재 시간과 차이 계산
                val now = System.currentTimeMillis()
                val diff = now - date.time

                val minutes = diff / (1000 * 60)
                val hours = minutes / 60

                // 3. 조건별 포맷팅
                return when {
                    minutes < 1 -> "방금 전"
                    minutes < 60 -> "${minutes}분 전"
                    hours < 24 -> "${hours}시간 전"
                    else -> {
                        // 24시간 지났으면 날짜로 표시 (한국 시간)
                        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                        formatter.timeZone = TimeZone.getDefault()
                        formatter.format(date)
                    }
                }
            } catch (e: Exception) {
                // 에러 나면 기존처럼 앞 10자리만 잘라서 보여줌
                return if (serverTime.length >= 10) serverTime.substring(0, 10).replace("-", ".") else serverTime
            }
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