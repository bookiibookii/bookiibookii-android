package com.bookiibookii.bookiibookii.myPage.main

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.MypageGroup
import com.bookiibookii.bookiibookii.databinding.ItemMypGroupsBinding

class MypGroupAdapter(private val items: List<MypageGroup>) :
    RecyclerView.Adapter<MypGroupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypGroupsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypageGroup) {
            val context = itemView.context

            // 1. 책 제목
            binding.itemMypGroupBookTitle.text = item.bookTitle

            // 2. 작가 및 장르 (예: 스즈키 유이 (소설/장르))
            // ★ 장르 한글 변환 적용
            val translatedGenre = translateGenre(item.GENRE)
            binding.itemMypGroupBookAuthor.text = "${item.auth} ($translatedGenre)"

            // 3. 그룹 상태 배지 (모집 중 vs 모집 완료) 색상 처리
            val isRecruiting = item.group_status == "RECRUITING" || item.group_status == "모집 중"

            if (isRecruiting) {
                // 모집 중: 배경 pre_main / 글자 white
                binding.itemMypGroupBadgeTv.text = "모집 중"
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.pre_main)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            } else {
                // 그 외(모집 완료 등): 배경 grey_200 / 글자 grey_500
                binding.itemMypGroupBadgeTv.text = "모집완료"
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.grey_200)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.grey_500)
                )
            }

            // 4. 태그 동적 생성 (#인사이트, #깔끔 등)
            binding.itemMypGroupTagsLl.removeAllViews() // [중요] 뷰 재사용 시 기존 태그 삭제

            item.groupTags.forEach { tagText ->
                // ★ 영어 뱃지 텍스트를 한글로 변환
                val translatedText = translateBadge(tagText)

                val textView = TextView(context).apply {
                    text = "#$translatedText"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f) // 글자 크기 11sp
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))

                    // 배경 설정 (둥근 회색 배경)
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.pre_sub_pale)

                    // 내부 패딩 (Horizontal 10dp, Vertical 6dp)
                    val paddingH = dpToPx(8)
                    val paddingV = dpToPx(4)
                    setPadding(paddingH, paddingV, paddingH, paddingV)

                    // 외부 마진 (오른쪽 8dp)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dpToPx(8)
                    }
                    layoutParams = params
                }
                binding.itemMypGroupTagsLl.addView(textView)
            }
        }

        private fun translateGenre(englishGenre: String): String {
            return when (englishGenre.uppercase()) {
                "ECON_BIZ" -> "경제/경영"
                "SCI_IT" -> "과학/IT"
                "NOVEL_GENRE" -> "소설/장르"
                "POEM_ESSAY" -> "시/에세이"
                "HOME_HOBBY" -> "가정/취미"
                "ART_CULTURE" -> "예술/문화"
                "HUMAN_HISTORY" -> "인문/역사"
                "SELF_DEV" -> "자기계발"
                "POL_SOC" -> "정치/사회"
                "ESC" -> "기타"
                else -> englishGenre // 매핑되는 단어가 없으면 원래 데이터 그대로 출력
            }
        }

        private fun translateBadge(englishText: String): String {
            return when (englishText.uppercase()) {
                "MEMO" -> "메모환영"
                "POSTIT" -> "포스트잇"
                "CLEAN" -> "깔끔"
                "SERIOUS" -> "진지함"
                "LIGHT_FUN" -> "재미있게"
                "INSIGHT" -> "인사이트"
                else -> englishText // 매핑되는 단어가 없으면 원래 영어 그대로 출력
            }
        }

        // dp 값을 px로 변환하는 헬퍼 함수
        private fun dpToPx(dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                itemView.resources.displayMetrics
            ).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMypGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}