package com.bookiibookii.bookiibookii.myPage.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.MypReport
import com.bookiibookii.bookiibookii.databinding.ItemMypReportBinding

class MypReportAdapter : RecyclerView.Adapter<MypReportAdapter.ViewHolder>() {

    private var items: List<MypReport> = emptyList()

    fun submitList(list: List<MypReport>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypReport) {
            binding.itemReportTitleTv.text = item.title
            binding.itemReportContentTv.text = item.content
            binding.itemReportDateTv.text = item.date
            binding.itemReportNameTv.text = item.targetName

            if (item.state == "답변 대기 중") {
                binding.itemReportStateIngTv.visibility = View.VISIBLE
                binding.itemReportStateEdTv.visibility = View.GONE

                binding.itemReportAnswerTv.visibility = View.VISIBLE
                binding.itemReportAnswerCl.visibility = View.GONE
            } else {
                binding.itemReportStateIngTv.visibility = View.GONE
                binding.itemReportStateEdTv.visibility = View.VISIBLE

                binding.itemReportAnswerTv.visibility = View.GONE
                binding.itemReportAnswerCl.visibility = View.VISIBLE
                binding.itemReportAnswerContentTv.text = item.answer
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}