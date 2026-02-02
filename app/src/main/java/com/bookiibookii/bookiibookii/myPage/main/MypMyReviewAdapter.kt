package com.bookiibookii.bookiibookii.myPage.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.MyReceivedReview
import com.bookiibookii.bookiibookii.databinding.ItemMypMyReviewBinding

class MypMyReviewAdapter(private var items: List<MyReceivedReview>) : RecyclerView.Adapter<MypMyReviewAdapter.ViewHolder>() {

    private val expandedState = mutableMapOf<Long, Boolean>()

    fun submitList(newItems: List<MyReceivedReview>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyReceivedReview) {
            // 기본 정보 바인딩
            binding.itemMypReviewTitleTv.text = item.writerName
            binding.itemMypReviewBookTitleTv.text = item.bookTitle
            binding.itemMypReviewBookAuthorTv.text = item.bookAuthor
            binding.itemMypReviewDateTv.text = item.datePeriod
            binding.itemMypMyReviewTv.text = item.content
            binding.itemMypPartnerCommentTv.text = item.partnerContent

            val tags = item.tags
            val count = tags.size


            if (count >= 1) {
                binding.tag1.text = tags[0]
                binding.tag1.visibility = View.VISIBLE
            } else {
                binding.tag1.visibility = View.GONE
            }

            // 두 번째 태그
            if (count >= 2) {
                binding.tag2.text = tags[1]
                binding.tag2.visibility = View.VISIBLE
            } else {
                binding.tag2.visibility = View.GONE
            }

            // 세 번째 태그 자리에 '+N' 표시
            if (count > 2) {
                binding.tag3.text = "+${count - 2}"
                binding.tag3.visibility = View.VISIBLE
            } else {
                binding.tag3.visibility = View.GONE
            }

            bindTag(binding.partnerTag1, tags.getOrNull(0))
            bindTag(binding.partnerTag2, tags.getOrNull(1))
            bindTag(binding.partnerTag3, tags.getOrNull(2))


            // 펼침/접힘 상태 로직
            val isExpanded = expandedState[item.id] ?: false

            if (isExpanded) {
                binding.layoutExpanded.visibility = View.VISIBLE
                binding.layoutTags.visibility = View.GONE // 위쪽 태그 숨기기
                binding.itemMypArrowIv.rotation = 180f
            } else {
                binding.layoutExpanded.visibility = View.GONE
                binding.layoutTags.visibility = View.VISIBLE // 위쪽 태그 보이기
                binding.itemMypArrowIv.rotation = 0f
            }

            binding.itemMypArrowIv.setOnClickListener {
                val newState = !isExpanded
                expandedState[item.id] = newState
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        // 단순 텍스트 바인딩 헬퍼 함수
        private fun bindTag(textView: TextView, text: String?) {
            if (!text.isNullOrEmpty()) {
                textView.text = text
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}