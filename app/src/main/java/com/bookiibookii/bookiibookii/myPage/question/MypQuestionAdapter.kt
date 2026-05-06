package com.bookiibookii.bookiibookii.myPage.question

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.mypage.InquirySummary
import com.bookiibookii.bookiibookii.databinding.ItemMypQuestionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MypQuestionAdapter : RecyclerView.Adapter<MypQuestionAdapter.ViewHolder>() {

    private var items: List<InquirySummary> = emptyList()

    fun submitList(list: List<InquirySummary>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InquirySummary) {
            binding.itemQuestionTitleTv.text = item.title
            binding.itemQuestionContentTv.text = item.content
            binding.itemQuestionUserTv.text = item.nickname
            binding.itemQuestionDateTv.text = DateUtils.formatDate(item.createdAt)

            // 답변 상태에 따른 UI 처리
            // 관리자 답변이 있거나 상태가 완료된 경우
            if (!item.adminReply.isNullOrEmpty() || item.supportStatus == "RESOLVED") {
                // 답변 완료 UI
                binding.itemQuestionAnswerTv.visibility = View.GONE     // "답변 대기 중" 숨김
                binding.itemQuestionAnswerCl.visibility = View.VISIBLE  // 답변창 보임

                binding.itemQuestionAnswerContentTv.text = item.adminReply ?: "답변 내용이 없습니다."
                binding.itemQuestionAnswerDateTv.text = DateUtils.formatDate(item.resolvedAt ?: "")
            } else {
                // 답변 대기 UI
                binding.itemQuestionAnswerTv.visibility = View.VISIBLE  // "답변 대기 중" 보임
                binding.itemQuestionAnswerCl.visibility = View.GONE     // 답변창 숨김
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