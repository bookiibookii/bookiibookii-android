package com.bookiibookii.bookiibookii.myPage.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.data.model.ReportSummary
import com.bookiibookii.bookiibookii.databinding.ItemMypReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MypReportAdapter(
    private val onItemClick: (ReportSummary) -> Unit // ★ 클릭 시 데이터를 통째로 전달
) : RecyclerView.Adapter<MypReportAdapter.ViewHolder>() {

    private var items: List<ReportSummary> = emptyList()

    fun submitList(list: List<ReportSummary>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportSummary) {
            val typeTitle = convertReportType(item.reportType)

            // ★ 요청하신 UI 데이터 매핑
            binding.itemReportNameTv.text = item.reporterNickname // 신고자 이름
            binding.itemReportDateTv.text = formatDate(item.createdAt) // 신고일
            binding.itemReportTitleTv.text = "${item.groupName} ($typeTitle)" // 책 제목(그룹명) + 신고유형
            binding.itemReportContentTv.text = item.content // 신고 내용

            // 상태에 따른 UI 처리
            if (item.supportStatus == "PENDING" && item.adminReply.isNullOrEmpty()) {
                binding.itemReportStateEdTv.visibility = View.GONE
                binding.itemReportAnswerTv.visibility = View.VISIBLE
                binding.itemReportAnswerCl.visibility = View.GONE
            } else {
                binding.itemReportStateEdTv.visibility = View.VISIBLE
                binding.itemReportAnswerTv.visibility = View.GONE
                binding.itemReportAnswerCl.visibility = View.VISIBLE
                binding.itemReportAnswerContentTv.text = item.adminReply ?: ""
            }

            // ★ 화살표 클릭 시 상세 화면으로 이동 이벤트
            binding.itemReportArrowIv.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun convertReportType(type: String): String {
            return when (type) {
                "ABUSE" -> "욕설/비방"
                "SPAM" -> "스팸/광고"
                "NO_SHOW" -> "책 미발송/노쇼/연락두절"
                "DAMAGED_BOOK" -> "책 파손/낙서"
                "OTHER" -> "기타"
                else -> type
            }
        }

        private fun formatDate(dateString: String): String {
            if (dateString.isEmpty()) return ""
            return try {
                // 1. 서버에서 오는 UTC 시간 형식 (Swagger 기준: yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                // ★ 핵심: 들어오는 시간이 UTC 기준이라고 파서에게 알려줌
                parser.timeZone = java.util.TimeZone.getTimeZone("UTC")

                // 2. 앱 화면에 보여줄 시간 형식 (예: 2026. 02. 12.)
                val formatter = java.text.SimpleDateFormat("yyyy. MM. dd.", java.util.Locale.getDefault())
                // ★ 핵심: 출력할 때는 현재 스마트폰의 시간대(한국, KST)로 자동 변환(+9시간)
                formatter.timeZone = java.util.TimeZone.getDefault()

                val date = parser.parse(dateString)
                formatter.format(date ?: return dateString)

            } catch (e: Exception) {
                // 만약 백엔드에서 밀리초(.SSS)를 빼고 "yyyy-MM-dd'T'HH:mm:ss" 형태로만 보낼 경우를 대비한 2차 파싱
                try {
                    val fallbackParser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    fallbackParser.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val date = fallbackParser.parse(dateString)

                    val formatter = java.text.SimpleDateFormat("yyyy. MM. dd.", java.util.Locale.getDefault())
                    formatter.timeZone = java.util.TimeZone.getDefault()
                    formatter.format(date ?: return dateString)
                } catch (e2: Exception) {
                    dateString // 변환에 모두 실패하면 원본 문자열 그대로 출력
                }
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