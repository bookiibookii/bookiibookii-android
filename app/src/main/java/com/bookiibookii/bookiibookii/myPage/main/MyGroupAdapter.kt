package com.bookiibookii.bookiibookii.myPage.main

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.MypageGroup
import com.bookiibookii.bookiibookii.databinding.ItemMypGroupsBinding
import com.google.android.flexbox.FlexboxLayout // ★ 추가 임포트

class MypGroupAdapter(private val items: List<MypageGroup>) :
    RecyclerView.Adapter<MypGroupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypGroupsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypageGroup) {
            val context = itemView.context

            binding.itemMypGroupBookTitle.text = item.bookTitle
            val translatedGenre = translateGenre(item.GENRE)
            binding.itemMypGroupBookAuthor.text = "${item.auth} ($translatedGenre)"

            val isRecruiting = item.group_status == "RECRUITING" || item.group_status == "모집 중"

            if (isRecruiting) {
                binding.itemMypGroupBadgeTv.text = "모집 중"
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.pre_main)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            } else {
                binding.itemMypGroupBadgeTv.text = "모집완료"
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.grey_200)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.grey_500)
                )
            }

            binding.itemMypGroupTagsLl.removeAllViews()

            item.groupTags.forEach { tagText ->
                val translatedText = translateBadge(tagText)

                val textView = TextView(context).apply {
                    text = "#$translatedText"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))

                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.pre_sub_pale)

                    val paddingH = dpToPx(8)
                    val paddingV = dpToPx(4)
                    setPadding(paddingH, paddingV, paddingH, paddingV)

                    // ★ LayoutParams를 FlexboxLayout용으로 수정
                    val params = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dpToPx(8)
                        bottomMargin = dpToPx(8) // 여러 줄이 생길 경우 아래 간격 확보
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
                else -> englishGenre
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
                else -> englishText
            }
        }

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