package com.bookiibookii.bookiibookii.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bumptech.glide.Glide

class ExchangeProgressAdapter(
    private val onClick: (HomeExchangeItem) -> Unit
) : ListAdapter<HomeExchangeItem, ExchangeProgressAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_exchange_progress_guest, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onClick: (HomeExchangeItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivCover: ImageView = itemView.findViewById(R.id.iv_book_cover)
        private val ivProfile: com.google.android.material.imageview.ShapeableImageView =
            itemView.findViewById(R.id.iv_user_profile)

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvWithNickname: TextView = itemView.findViewById(R.id.tv_with_nickname)

        private val tvDate1: TextView = itemView.findViewById(R.id.tv_step_date_1)
        private val tvDate2: TextView = itemView.findViewById(R.id.tv_step_date_2)
        private val tvDate3: TextView = itemView.findViewById(R.id.tv_step_date_3)
        private val tvDate4: TextView = itemView.findViewById(R.id.tv_step_date_4)

        private val viewFill: View = itemView.findViewById(R.id.view_fill)
        private val guide1: View = itemView.findViewById(R.id.guide_step_1)
        private val guide2: View = itemView.findViewById(R.id.guide_step_2)
        private val guide3: View = itemView.findViewById(R.id.guide_step_3)
        private val guide4: View = itemView.findViewById(R.id.guide_step_4)

        private val tvStep1: TextView = itemView.findViewById(R.id.tv_step_1)
        private val tvStep2: TextView = itemView.findViewById(R.id.tv_step_2)
        private val tvStep3: TextView = itemView.findViewById(R.id.tv_step_3)
        private val tvStep4: TextView = itemView.findViewById(R.id.tv_step_4)

        private val trackerRoot: ConstraintLayout = itemView.findViewById(R.id.layout_tracker)

        fun bind(item: HomeExchangeItem) {
            tvTitle.text = item.bookTitle
            tvAuthor.text = item.author.orEmpty()
            tvWithNickname.text = item.withNickname

            // 프로필 이미지 (RecyclerView 재사용 대응)
            Glide.with(itemView).clear(ivProfile)
            ivProfile.setImageResource(R.drawable.img_profile_default)

            if (!item.profileUrl.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(item.profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img_profile_default)
                    .error(R.drawable.img_profile_default)
                    .into(ivProfile)
            }

            // 책 이미지 (RecyclerView 재사용 대응)
            Glide.with(itemView).clear(ivCover)
            ivCover.setImageResource(R.drawable.bg_book_cover_frame)

            if (!item.image.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(item.image)
                    .placeholder(R.drawable.bg_book_cover_frame)
                    .error(R.drawable.bg_book_cover_frame)
                    .into(ivCover)
            }

            val dates = item.stepDates.orEmpty()
            bindStepDate(tvDate1, dates, 0)
            bindStepDate(tvDate2, dates, 1)
            bindStepDate(tvDate3, dates, 2)
            bindStepDate(tvDate4, dates, 3)

            val step = mapRelayStatusToStep(item.trackerStatus)
            applyStepUi(step)

            itemView.setOnClickListener { onClick(item) }
        }

        @SuppressLint("SetTextI18n")
        private fun applyStepUi(step: Int) {
            val endTargetId = when (step) {
                1 -> guide1.id
                2 -> guide2.id
                3 -> guide3.id
                else -> guide4.id
            }

            // fill end 이동
            val fillParams = viewFill.layoutParams as ConstraintLayout.LayoutParams
            fillParams.endToEnd = endTargetId
            fillParams.endToStart = ConstraintLayout.LayoutParams.UNSET
            viewFill.layoutParams = fillParams

            // 프로필 위치 이동
            val profileParams = ivProfile.layoutParams as ConstraintLayout.LayoutParams
            profileParams.startToStart = ConstraintLayout.LayoutParams.UNSET
            profileParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
            profileParams.startToStart = endTargetId
            profileParams.endToEnd = endTargetId
            ivProfile.layoutParams = profileParams

            // 라벨 색상
            val selected = itemView.context.getColor(R.color.pre_main)
            val normal = itemView.context.getColor(R.color.grey_500)

            tvStep1.setTextColor(if (step == 1) selected else normal)
            tvStep2.setTextColor(if (step == 2) selected else normal)
            tvStep3.setTextColor(if (step == 3) selected else normal)
            tvStep4.setTextColor(if (step == 4) selected else normal)

            // 날짜는 현재 단계만
            tvDate1.visibility = if (step == 1 && tvDate1.text.isNotBlank()) View.VISIBLE else View.INVISIBLE
            tvDate2.visibility = if (step == 2 && tvDate2.text.isNotBlank()) View.VISIBLE else View.INVISIBLE
            tvDate3.visibility = if (step == 3 && tvDate3.text.isNotBlank()) View.VISIBLE else View.INVISIBLE
            tvDate4.visibility = if (step == 4 && tvDate4.text.isNotBlank()) View.VISIBLE else View.INVISIBLE

            trackerRoot.requestLayout()
        }

        private fun bindStepDate(tv: TextView, dates: List<String?>, index: Int) {
            val value = if (index < dates.size) dates[index] else null
            if (value.isNullOrBlank()) {
                tv.visibility = View.INVISIBLE
            } else {
                tv.text = value
                tv.visibility = View.VISIBLE
            }
        }

        private fun mapRelayStatusToStep(status: String?): Int {
            if (status.isNullOrBlank()) return 1
            val s = status.uppercase()
            return when {
                s.contains("HOST") && s.contains("READ") -> 1
                s.contains("SHIPPING") || s.contains("DELIVER") -> 2
                s.contains("GUEST") && s.contains("READ") -> 3
                s.contains("RETURN") || s.contains("RECOVER") -> 4
                else -> 1
            }
        }
    } // ✅ ViewHolder 닫기

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HomeExchangeItem>() {
            override fun areItemsTheSame(oldItem: HomeExchangeItem, newItem: HomeExchangeItem): Boolean {
                return oldItem.groupId == newItem.groupId && oldItem.role == newItem.role
            }

            override fun areContentsTheSame(oldItem: HomeExchangeItem, newItem: HomeExchangeItem): Boolean {
                return oldItem == newItem
            }
        }
    }
} // ✅ Adapter 닫기