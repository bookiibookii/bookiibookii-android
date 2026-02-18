package com.bookiibookii.bookiibookii.myPage.review

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.MypRelayReview
import com.bookiibookii.bookiibookii.databinding.ItemMypMyReviewBinding
import com.bumptech.glide.Glide

class MypMyReviewAdapter(private var items: List<MypRelayReview>) : RecyclerView.Adapter<MypMyReviewAdapter.ViewHolder>() {

    // 아이템별 확장 상태 저장 (GroupId를 키로 사용)
    private val expandedState = mutableMapOf<Long, Boolean>()

    fun submitList(newItems: List<MypRelayReview>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypRelayReview) {
            val context = binding.root.context

            // 1. 기본 정보 바인딩
            binding.itemMypReviewTitleTv.text = item.partnerNickname
            binding.itemMypReviewBookTitleTv.text = item.bookTitle

            // API에 작가 정보가 없으므로 공백 처리 (필요 시 View.GONE)
            binding.itemMypReviewBookAuthorTv.text = ""

            binding.itemMypReviewDateTv.text = "${item.startDate} ~ ${item.finishedDate}"

            // 이미지 로드
            Glide.with(context)
                .load(item.bookImage)
                .placeholder(R.drawable.bg_round_10dp_gray300) // 기본 이미지
                .error(R.drawable.bg_round_10dp_gray300)
                .into(binding.itemMypReviewCoverIv)

            // 태그 데이터 준비
            val tags = item.partnerBadges.map { it.description }
            val count = tags.size

            // 2. 닫힌 상태 태그 처리 (최대 2개 + 숫자)
            if (count >= 1) {
                binding.tag1.text = "#${tags[0]}"
                binding.tag1.visibility = View.VISIBLE
            } else {
                binding.tag1.visibility = View.GONE
            }

            if (count >= 2) {
                binding.tag2.text = "#${tags[1]}"
                binding.tag2.visibility = View.VISIBLE
            } else {
                binding.tag2.visibility = View.GONE
            }

            if (count > 2) {
                binding.tag3.text = "+${count - 2}"
                binding.tag3.visibility = View.VISIBLE
            } else {
                binding.tag3.visibility = View.GONE
            }

            // 3. 열린 상태 데이터 바인딩

            // 별점 설정 (책 평가)
            setStars(binding.itemMypReviewRateList, item.partnerBookRating)
            // 책 후기 내용
            binding.itemMypMyReviewTv.text = item.partnerBookComment

            // 별점 설정 (나에 대한 평가)
            setStars(binding.itemMypPartnerReviewRateList, item.partnerToMeRating)
            // 나에 대한 코멘트
            binding.itemMypPartnerCommentTv.text = item.partnerToMeComment

            // 열린 상태 태그 (전부 표시 - XML 상 3개 슬롯 매핑)
            val expandedTags = listOf(
                binding.partnerTag1, binding.partnerTag2, binding.partnerTag3,
                binding.partnerTag4, binding.partnerTag5, binding.partnerTag6
            )

// 전체 태그 리스트를 순회하며 텍스트를 넣고, 모자란 부분은 GONE 처리
            expandedTags.forEachIndexed { index, textView ->
                if (index < tags.size) {
                    textView.text = "#${tags[index]}"
                    textView.visibility = View.VISIBLE
                } else {
                    textView.visibility = View.GONE
                }
            }

            // 4. 펼침/접힘 상태 로직
            val isExpanded = expandedState[item.groupId] ?: false

            if (isExpanded) {
                binding.layoutExpanded.visibility = View.VISIBLE
                binding.layoutTags.visibility = View.GONE // 닫힌 상태 태그 숨기기
                binding.itemMypArrowIv.rotation = 180f
            } else {
                binding.layoutExpanded.visibility = View.GONE
                binding.layoutTags.visibility = View.VISIBLE // 닫힌 상태 태그 보이기
                binding.itemMypArrowIv.rotation = 0f
            }

            // 화살표 클릭 리스너
            binding.itemMypArrowIv.setOnClickListener {
                val newState = !isExpanded
                expandedState[item.groupId] = newState
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        // 태그 텍스트 바인딩 헬퍼
        private fun bindPartnerTag(textView: TextView, text: String?) {
            if (!text.isNullOrEmpty()) {
                textView.text = "#$text"
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }

        // 별점 아이콘 설정 헬퍼 (LinearLayout 내의 ImageView들을 순회)
        private fun setStars(container: LinearLayout, rating: Double) {
            val ratingInt = rating.toInt()
            val hasHalfStar = (rating - ratingInt) >= 0.5

            for (i in 0 until container.childCount) {
                val star = container.getChildAt(i) as? ImageView ?: continue
                when {
                    i < ratingInt -> star.setImageResource(R.drawable.ic_star_filled)
                    i == ratingInt && hasHalfStar -> star.setImageResource(R.drawable.ic_star_half)
                    else -> star.setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}