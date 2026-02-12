package com.bookiibookii.bookiibookii.myPage.Notice

import android.content.Context
import android.view.LayoutInflater
import android.view.View // ★ View 추가
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.data.model.NoticeSummary
import com.bookiibookii.bookiibookii.databinding.ItemMypNoticeBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MypNoticeAdapter(
    private var items: List<NoticeSummary>,
    private val onItemClick: (Int) -> Unit // 클릭 시 ID 전달
) : RecyclerView.Adapter<MypNoticeAdapter.ViewHolder>() {

    fun submitList(newItems: List<NoticeSummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeSummary) {
            binding.itemNoticeTitleTv.text = item.title
            binding.itemNoticeContent.text = item.summary // XML ID: item_notice_content

            // 날짜 포맷팅 (2026-02-03T... -> 2026.02.03)
            binding.itemNoticeDateTv.text = formatDate(item.createdAt)

            // ★ 1. SharedPreferences에서 해당 공지사항 ID가 '읽음' 처리되어 있는지 확인
            val prefs = binding.root.context.getSharedPreferences("NoticePrefs", Context.MODE_PRIVATE)
            val isRead = prefs.getBoolean("notice_read_${item.id}", false)

            // ★ 2. 읽음 여부에 따라 빨간 점(item_notice_notice_v) 표시 / 숨김
            if (isRead) {
                binding.itemNoticeNoticeV.visibility = View.GONE
            } else {
                binding.itemNoticeNoticeV.visibility = View.VISIBLE
            }

            // 아이템 클릭 시 상세 화면으로 이동
            binding.root.setOnClickListener {
                // ★ 3. 클릭하는 순간 SharedPreferences에 '읽음(true)'으로 기록
                prefs.edit().putBoolean("notice_read_${item.id}", true).apply()

                // 화면상에서도 즉시 빨간 점 숨기기 (새로고침 없이 바로 적용되도록)
                binding.itemNoticeNoticeV.visibility = View.GONE

                // 기존 기능: 아이디 넘겨서 프래그먼트에서 상세 이동 처리
                onItemClick(item.id)
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
        val binding = ItemMypNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}