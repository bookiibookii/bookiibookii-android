package com.bookiibookii.bookiibookii.myPage.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.data.model.ReportSummary
import com.bookiibookii.bookiibookii.databinding.ItemMypReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MypReportAdapter : RecyclerView.Adapter<MypReportAdapter.ViewHolder>() {

    private var items: List<ReportSummary> = emptyList()

    fun submitList(list: List<ReportSummary>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportSummary) {
            // 신고 유형 한글 변환
            val typeTitle = convertReportType(item.reportType)
            binding.itemReportTitleTv.text = typeTitle // 예: "욕설/비방"

            binding.itemReportContentTv.text = item.content
            binding.itemReportDateTv.text = formatDate(item.createdAt)
            binding.itemReportNameTv.text = item.groupName // 타겟 이름 대신 그룹명 표시

            // 상태에 따른 UI 처리
            if (item.supportStatus == "PENDING" && item.adminReply.isNullOrEmpty()) {
                // 답변 대기 중
                binding.itemReportStateIngTv.visibility = View.VISIBLE
                binding.itemReportStateEdTv.visibility = View.GONE

                binding.itemReportAnswerTv.visibility = View.VISIBLE
                binding.itemReportAnswerCl.visibility = View.GONE
            } else {
                // 답변 완료
                binding.itemReportStateIngTv.visibility = View.GONE
                binding.itemReportStateEdTv.visibility = View.VISIBLE

                binding.itemReportAnswerTv.visibility = View.GONE
                binding.itemReportAnswerCl.visibility = View.VISIBLE
                binding.itemReportAnswerContentTv.text = item.adminReply ?: ""
            }
        }

        private fun convertReportType(type: String): String {
            return when (type) {
                "ABUSE" -> "욕설/비방"
                "SPAM" -> "스팸/광고"
                "NOSHOW" -> "책 미발송/노쇼"
                "DAMAGE" -> "책 파손/낙서"
                "OTHER" -> "기타"
                else -> type
            }
        }

        private fun formatDate(dateString: String): String {
            if (dateString.isEmpty()) return ""
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
                val date = parser.parse(dateString)
                formatter.format(date ?: return dateString)
            } catch (e: Exception) {
                dateString
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