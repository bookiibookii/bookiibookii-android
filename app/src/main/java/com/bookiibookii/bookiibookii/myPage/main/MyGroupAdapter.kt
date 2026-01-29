package com.bookiibookii.bookiibookii.myPage.main

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypGroup
import com.bookiibookii.bookiibookii.databinding.ItemMypGroupsBinding

class MypGroupAdapter(private val items: List<MypGroup>) :
    RecyclerView.Adapter<MypGroupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypGroupsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypGroup) {
            binding.itemMypGroupBookTitle.text = item.title
            binding.itemMypGroupBookAuthor.text = item.author

            // 태그 설정 (단순화를 위해 3개 고정된 뷰에 매핑한다고 가정하거나 동적으로 추가)
            // 여기서는 XML에 있는 TextView들을 활용하여 첫 번째 태그만 예시로 넣습니다.
            // 실제 구현 시에는 FlexboxLayout이나 RecyclerView를 중첩해 쓰는 것이 좋습니다.
            // 여기서는 텍스트만 변경하는 것으로 처리합니다.
            val tagViews = listOf(
                binding.itemMypGroupTagsLl.getChildAt(0) as? TextView,
                binding.itemMypGroupTagsLl.getChildAt(1) as? TextView,
                binding.itemMypGroupTagsLl.getChildAt(2) as? TextView
            )

            tagViews.forEach { it?.visibility = View.GONE } // 일단 다 숨김

            item.tags.forEachIndexed { index, tag ->
                if(index < tagViews.size) {
                    tagViews[index]?.text = "#$tag"
                    tagViews[index]?.visibility = View.VISIBLE
                }
            }

            // 배지 상태 변경 로직
            if (item.isRecruiting) {
                // 모집 중 (기본 색상: pre_main 배경 / white 글자)
                binding.itemMypGroupBadgeTv.text = "모집 중"
                binding.itemMypGroupBadgeTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                binding.itemMypGroupBadgeTv.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.pre_main)
                )
            } else {
                // 모집 완료 (gray_200 배경 / gray_500 글자)
                binding.itemMypGroupBadgeTv.text = "모집 완료"
                binding.itemMypGroupBadgeTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey_500))
                binding.itemMypGroupBadgeTv.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.grey_200)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}