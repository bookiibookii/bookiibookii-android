package com.bookiibookii.bookiibookii.myPage.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.MyReceivedReview
import com.bookiibookii.bookiibookii.databinding.ItemMypMyReviewBinding

// 생성자에서 items의 타입을 List<MyReceivedReview>로 확실하게 지정
class MypMyReviewAdapter(private var items: List<MyReceivedReview>) : RecyclerView.Adapter<MypMyReviewAdapter.ViewHolder>() {

    private val expandedState = mutableMapOf<Long, Boolean>()

    fun submitList(newItems: List<MyReceivedReview>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyReceivedReview) {
            binding.itemMypReviewTitleTv.text = item.writerName
            binding.itemMypReviewBookTitleTv.text = item.bookTitle
            binding.itemMypReviewBookAuthorTv.text = item.bookAuthor
            binding.itemMypReviewDateTv.text = item.datePeriod

            // 태그 예외처리
            binding.tag1.text = item.tags.getOrNull(0) ?: ""
            binding.tag2.text = item.tags.getOrNull(1) ?: ""
            binding.tag3.visibility = if(item.tags.size > 2) View.VISIBLE else View.GONE

            binding.itemMypMyReviewTv.text = item.content
            binding.itemMypPartnerCommentTv.text = item.partnerContent

            val isExpanded = expandedState[item.id] ?: false
            binding.layoutExpanded.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.itemMypArrowIv.rotation = if (isExpanded) 180f else 0f

            binding.itemMypArrowIv.setOnClickListener {
                val newState = !isExpanded
                expandedState[item.id] = newState
                notifyItemChanged(adapterPosition)
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