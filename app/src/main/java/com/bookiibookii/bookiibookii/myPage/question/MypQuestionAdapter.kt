package com.bookiibookii.bookiibookii.myPage.question

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.MypQuestion
import com.bookiibookii.bookiibookii.databinding.ItemMypQuestionBinding

class MypQuestionAdapter : RecyclerView.Adapter<MypQuestionAdapter.ViewHolder>() {

    private var items: List<MypQuestion> = emptyList()

    fun submitList(list: List<MypQuestion>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypQuestion) {
            binding.itemQuestionTitleTv.text = item.title
            binding.itemQuestionContentTv.text = item.content
            binding.itemQuestionDateTv.text = item.date
            binding.itemQuestionUserTv.text = "noshel"

            // 답변 상태에 따른 UI 처리
            if (item.answer != null) {
                // 답변 완료 시
                binding.itemQuestionAnswerTv.visibility = View.GONE
                binding.itemQuestionAnswerCl.visibility = View.VISIBLE
                binding.itemQuestionAnswerContentTv.text = item.answer
                binding.itemQuestionAnswerDateTv.text = item.answerDate
            } else {
                // 답변 대기 시
                binding.itemQuestionAnswerTv.visibility = View.VISIBLE
                binding.itemQuestionAnswerCl.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}