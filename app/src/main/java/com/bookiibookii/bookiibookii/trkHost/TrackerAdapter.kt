package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ItemTrkBinding
import com.bumptech.glide.Glide
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrackerAdapter(
    private val onItemClicked: (TrackerData) -> Unit
) : ListAdapter<TrackerData, TrackerAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class ViewHolder(
        private val binding: ItemTrkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TrackerData,
            onItemClicked: (TrackerData) -> Unit
        ) {
            binding.tvBookTitle.text = item.bookTitle
            binding.tvBookAuthor.text = item.bookAuthor

            binding.tvWithUser.text = item.withUserName ?: ""

            binding.tvBookAuthor.text = item.bookAuthor
            binding.tvBookCategory.text = item.bookCategory.orEmpty()

            Glide.with(binding.ivBookCover)
                .load(item.coverImageUrl)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(binding.ivBookCover)

            binding.updateStepUI(
                step = item.currentStep,
                stepDates = item.stepDates,
                hostProfileUrl = item.hostProfileImageUrl,
                guestProfileUrl = item.guestProfileImageUrl
            )

            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }

        private fun ItemTrkBinding.updateStepUI(
            step: TrackerStep,
            stepDates: List<String?>,
            hostProfileUrl: String?,
            guestProfileUrl: String?
        ) {
            val ctx = root.context
            val activeColor = ContextCompat.getColor(ctx, R.color.grey_900)
            val inactiveColor = ContextCompat.getColor(ctx, R.color.grey_400)
            val accentColor = ContextCompat.getColor(ctx, R.color.pre_main)

            tvDateStep1.text = formatStepDate(stepDates.getOrNull(0))
            tvDateStep2.text = formatStepDate(stepDates.getOrNull(1))
            tvDateStep3.text = formatStepDate(stepDates.getOrNull(2))
            tvDateStep4.text = formatStepDate(stepDates.getOrNull(3))

            val idx = stepIndex(step)

            // 프로그레스바 채우기, 나중에 상세 조정
            val percent = when (idx) {
                0 -> 0.1f
                1 -> 1.0f / 3.0f
                2 -> 2.0f / 3.0f
                3 -> 1.0f
                else -> 0.0f
            }
            setWidthPercent(viewProgressTrackActive, percent)

            // 기본은 inactive, 현재만 accent
            tvLabelStep1.setTextColor(if (idx == 0) accentColor else inactiveColor)
            tvLabelStep2.setTextColor(if (idx == 1) accentColor else inactiveColor)
            tvLabelStep3.setTextColor(if (idx == 2) accentColor else inactiveColor)
            tvLabelStep4.setTextColor(if (idx == 3) accentColor else inactiveColor)

            // 완료(현재보다 이전) = active, 현재 = accent, 이후 = inactive
            setDot(dotStep1, dotColorForStep(0, idx, activeColor, accentColor, inactiveColor))
            setDot(dotStep2, dotColorForStep(1, idx, activeColor, accentColor, inactiveColor))
            setDot(dotStep3, dotColorForStep(2, idx, activeColor, accentColor, inactiveColor))
            setDot(dotStep4, dotColorForStep(3, idx, activeColor, accentColor, inactiveColor))

            bindStepProfiles(hostProfileUrl, guestProfileUrl)
        }

        private fun stepIndex(step: TrackerStep): Int = when (step) {
            TrackerStep.HOST_READING -> 0
            TrackerStep.SHIPPING -> 1
            TrackerStep.GUEST_READING -> 2
            TrackerStep.RETURNING -> 3
        }

        private fun dotColorForStep(
            dotIndex: Int,
            currentIndex: Int,
            doneColor: Int,
            currentColor: Int,
            todoColor: Int
        ): Int {
            return when {
                dotIndex < currentIndex -> doneColor
                dotIndex == currentIndex -> currentColor
                else -> todoColor
            }
        }

        private fun setWidthPercent(view: android.view.View, percent: Float) {
            val lp = view.layoutParams as ConstraintLayout.LayoutParams
            lp.matchConstraintPercentWidth = percent.coerceIn(0f, 1f)
            view.layoutParams = lp
            view.requestLayout()
        }

        private fun setDot(dotView: android.widget.ImageView, color: Int) {
            dotView.setColorFilter(color)
        }

        private fun formatStepDate(raw: String?): String {
            if (raw.isNullOrBlank()) return ""

            return try {
                val dateOnly = raw.substring(0, 10)
                val date = LocalDate.parse(dateOnly)
                val mmdd = date.format(DateTimeFormatter.ofPattern("M. d."))
                "$mmdd"
            } catch (_: Exception) {
                raw
            }
        }

        private fun ItemTrkBinding.bindStepProfiles(hostUrl: String?, guestUrl: String?) {
            setProfile(ivStep1Profile, hostUrl)
            setProfile(ivStep2Profile, hostUrl)

            setProfile(ivStep3Profile, guestUrl)
            setProfile(ivStep4Profile, guestUrl)
        }

        private fun ItemTrkBinding.setProfile(view: com.google.android.material.imageview.ShapeableImageView, url: String?) {
            if (url.isNullOrBlank()) {
                view.visibility = android.view.View.GONE
                return
            }
            view.visibility = android.view.View.VISIBLE
            Glide.with(view)
                .load(url)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(view)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TrackerData>() {
            override fun areItemsTheSame(old: TrackerData, new: TrackerData): Boolean {
                return old.id == new.id
            }

            override fun areContentsTheSame(old: TrackerData, new: TrackerData): Boolean {
                return old == new
            }
        }
    }
}
