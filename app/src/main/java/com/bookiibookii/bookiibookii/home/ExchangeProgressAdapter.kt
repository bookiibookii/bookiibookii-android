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
        private val ivProfile: com.google.android.material.imageview.ShapeableImageView = itemView.findViewById(R.id.iv_user_profile)

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
            setFillEndToStep(step)

            itemView.setOnClickListener { onClick(item) }
        }

        private fun bindStepDate(tv: TextView, dates: List<String?>, index: Int) {
            val value = if (index < dates.size) dates[index] else null
            if (value.isNullOrBlank()) tv.visibility = View.INVISIBLE
            else {
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

        private fun setFillEndToStep(step: Int) {
            val endTargetId = when (step) {
                1 -> guide1.id
                2 -> guide2.id
                3 -> guide3.id
                else -> guide4.id
            }
            val params = viewFill.layoutParams as ConstraintLayout.LayoutParams
            params.endToEnd = endTargetId
            viewFill.layoutParams = params
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HomeExchangeItem>() {

            override fun areItemsTheSame(oldItem: HomeExchangeItem, newItem: HomeExchangeItem): Boolean {
                // 혹시라도 같은 groupId가 다른 role로 들어올 수 있으니 role까지 포함
                return oldItem.groupId == newItem.groupId && oldItem.role == newItem.role
            }

            override fun areContentsTheSame(oldItem: HomeExchangeItem, newItem: HomeExchangeItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}