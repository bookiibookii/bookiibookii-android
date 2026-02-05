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
            // 1. 책 제목
            binding.itemMypGroupBookTitle.text = item.bookTitle

            // 2. 작가 및 장르 (예: 소설 | 스즈키 유이)
            // 데이터에 작가(auth)와 장르(GENRE)가 있으므로 이를 조합
            binding.itemMypGroupBookAuthor.text = "${item.GENRE} | ${item.auth}"

            // 3. 그룹 상태 배지 (모집 중 vs 모집 완료) 색상 처리
            binding.itemMypGroupBadgeTv.text = item.group_status

            // API에서 "RECRUITING"으로 오는지, 한글 "모집 중"으로 오는지에 따라 조건문 조정 필요
            // 여기서는 둘 다 체크하도록 작성함
            val isRecruiting = item.group_status == "RECRUITING" || item.group_status == "모집 중"

            val context = itemView.context
            if (isRecruiting) {
                // 모집 중: 배경 pre_main / 글자 white
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.pre_main)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            } else {
                // 그 외(모집 완료 등): 배경 grey_200 / 글자 grey_500
                binding.itemMypGroupBadgeTv.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.grey_200)
                binding.itemMypGroupBadgeTv.setTextColor(
                    ContextCompat.getColor(context, R.color.grey_500)
                )
            }

            // 4. 태그 동적 생성 (#인사이트, #깔끔 등)
            binding.itemMypGroupTagsLl.removeAllViews() // [중요] 뷰 재사용 시 기존 태그 삭제

            item.groupTags.forEach { tagText ->
                val textView = TextView(context).apply {
                    text = "#$tagText"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f) // 글자 크기 11sp
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))

                    // 배경 설정 (둥근 회색 배경)
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.pre_sub_pale)

                    // 내부 패딩 (Horizontal 10dp, Vertical 6dp)
                    val paddingH = dpToPx(16)
                    val paddingV = dpToPx(16)
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