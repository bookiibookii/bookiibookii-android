package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ItemTrackerNoneBinding
import com.bookiibookii.bookiibookii.databinding.ItemTrkBinding
import com.bumptech.glide.Glide
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrackerAdapter(
    private val onItemClicked: (TrackerData) -> Unit
) : ListAdapter<TrackerData, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).exchangeType) {
            ExchangeType.NONE -> VIEW_TYPE_NONE
            else -> VIEW_TYPE_EXCHANGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_NONE -> {
                val binding = ItemTrackerNoneBinding.inflate(inflater, parent, false)
                NoneViewHolder(binding)
            }
            else -> {
                val binding = ItemTrkBinding.inflate(inflater, parent, false)
                ExchangeViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is NoneViewHolder -> holder.bind(item, onItemClicked)
            is ExchangeViewHolder -> holder.bind(item, onItemClicked)
        }
    }

    class ExchangeViewHolder(
        private val binding: ItemTrkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrackerData, onItemClicked: (TrackerData) -> Unit) {
            binding.tvBookTitle.text = item.bookTitle
            binding.tvBookAuthor.text = item.bookAuthor
            binding.tvWithUser.text = item.withUserName ?: ""
            binding.tvBookCategory.text = mapCategoryToKo(item.bookCategory)

            Glide.with(binding.ivBookCover)
                .load(item.coverImageUrl)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(binding.ivBookCover)

            binding.updateStepUI(
                step = progressStatusFromDates(item.stepDates),
                stepDates = item.stepDates,
                hostProfileUrl = item.hostProfileImageUrl,
                guestProfileUrl = item.guestProfileImageUrl
            )

            binding.root.setOnClickListener { onItemClicked(item) }
        }

        private fun progressStatusFromDates(stepDates: List<String?>): TrackerStatus {
            val lastFilled = stepDates.indexOfLast { !it.isNullOrBlank() }
            return when (lastFilled) {
                0 -> TrackerStatus.HOST_READING
                1 -> TrackerStatus.SHIPPING_TO_GUEST
                2 -> TrackerStatus.GUEST_READING
                3 -> TrackerStatus.SHIPPING_TO_HOST
                else -> TrackerStatus.READY
            }
        }

        private fun ItemTrkBinding.updateStepUI(
            step: TrackerStatus,
            stepDates: List<String?>,
            hostProfileUrl: String?,
            guestProfileUrl: String?
        ) {
            val ctx = root.context
            val inactiveColor = ContextCompat.getColor(ctx, R.color.grey_400)
            val accentColor = ContextCompat.getColor(ctx, R.color.pre_main)

            tvDateStep1.text = formatStepDate(stepDates.getOrNull(0))
            tvDateStep2.text = formatStepDate(stepDates.getOrNull(1))
            tvDateStep3.text = formatStepDate(stepDates.getOrNull(2))
            tvDateStep4.text = formatStepDate(stepDates.getOrNull(3))

            val idx = stepIndex(step)

            val percent = trackPercentForStep(idx)
            setWidthPercent(viewProgressTrackActive, percent)

            tvLabelStep1.setTextColor(if (idx == 0) accentColor else inactiveColor)
            tvLabelStep2.setTextColor(if (idx == 1) accentColor else inactiveColor)
            tvLabelStep3.setTextColor(if (idx == 2) accentColor else inactiveColor)
            tvLabelStep4.setTextColor(if (idx == 3) accentColor else inactiveColor)

            bindStepProfiles(hostProfileUrl, guestProfileUrl, idx)
        }

        private fun trackPercentForStep(index: Int): Float {
            val trackWidth = 332f
            val edgeInset = 20f
            val dotSize = 6f

            val firstCenter = edgeInset + dotSize / 2f
            val lastCenter = trackWidth - edgeInset - dotSize / 2f
            val gap = (lastCenter - firstCenter) / 3f

            return when (index) {
                0 -> 0f
                1 -> (firstCenter + gap) / trackWidth
                2 -> (firstCenter + gap * 2f) / trackWidth
                3 -> lastCenter / trackWidth
                else -> 0f
            }.coerceIn(0f, 1f)
        }

        private fun stepIndex(status: TrackerStatus): Int = when (status) {
            TrackerStatus.READY,
            TrackerStatus.HOST_READING,
            TrackerStatus.HOST_EXTENSION,
            TrackerStatus.HOST_DONE -> 0

            TrackerStatus.SHIPPING_TO_GUEST,
            TrackerStatus.RECEIVED -> 1

            TrackerStatus.GUEST_READING,
            TrackerStatus.GUEST_EXTENSION,
            TrackerStatus.GUEST_DONE -> 2

            TrackerStatus.SHIPPING_TO_HOST,
            TrackerStatus.RETURNED,
            TrackerStatus.COMPLETED -> 3

            TrackerStatus.UNKNOWN -> 0
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

        private fun setWidthPercent(view: View, percent: Float) {
            val lp = view.layoutParams as ConstraintLayout.LayoutParams
            lp.matchConstraintPercentWidth = percent.coerceIn(0f, 1f)
            view.layoutParams = lp
            view.requestLayout()
        }

        private fun setDot(dotView: View, color: Int) {
            dotView.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        }

        private fun formatStepDate(raw: String?): String {
            if (raw.isNullOrBlank()) return ""
            return try {
                val dateOnly = raw.substring(0, 10)
                val date = LocalDate.parse(dateOnly)
                date.format(DateTimeFormatter.ofPattern("M. d."))
            } catch (_: Exception) {
                raw
            }
        }

        private fun ItemTrkBinding.bindStepProfiles(hostUrl: String?, guestUrl: String?, currentIndex: Int) {
            setProfile(ivStep1Profile, hostUrl, isVisible = (currentIndex == 0))
            setProfile(ivStep2Profile, hostUrl, isVisible = (currentIndex == 1))
            setProfile(ivStep3Profile, guestUrl, isVisible = (currentIndex == 2))
            setProfile(ivStep4Profile, guestUrl, isVisible = (currentIndex == 3))
        }

        private fun ItemTrkBinding.setProfile(view: ImageView, url: String?, isVisible: Boolean) {
            if (!url.isNullOrBlank() && isVisible) {
                view.visibility = View.VISIBLE
                Glide.with(view)
                    .load(url)
                    .placeholder(R.color.grey_200)
                    .error(R.color.grey_200)
                    .into(view)
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    class NoneViewHolder(
        private val binding: ItemTrackerNoneBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrackerData, onItemClicked: (TrackerData) -> Unit) {
            binding.tvBookTitle.text = item.bookTitle
            binding.tvBookAuthor.text = item.bookAuthor
            binding.tvBookCategory.text = mapCategoryToKo(item.bookCategory)
            binding.tvWithUser.text = item.withUserName ?: ""

            Glide.with(binding.ivBookCover)
                .load(item.coverImageUrl)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(binding.ivBookCover)

            val myRate = (item.myReadingRate ?: 0).coerceIn(0, 100)
            val groupRate = (item.groupReadingRate ?: 0).coerceIn(0, 100)

            binding.tvMyPercent.text = "${myRate}%"
            binding.tvGroupPercent.text = "${groupRate}%"

            val myPercent = (myRate / 100f).coerceIn(0f, 1f)
            val groupPercent = (groupRate / 100f).coerceIn(0f, 1f)

            setWidthPercent(binding.viewTrackFill, myPercent)
            setWidthPercent(binding.viewTrackGroup, groupPercent)
            setGuidelinePercent(binding.guidelineGroup, groupPercent)

            if (myPercent <= groupPercent) {
                binding.viewTrackFill.bringToFront()
                binding.dotMy.bringToFront()
            } else {
                binding.viewTrackGroup.bringToFront()
                binding.dotGroup.bringToFront()
            }

            binding.layoutProgressCompare.invalidate()

            binding.root.setOnClickListener { onItemClicked(item) }
        }

        private fun setWidthPercent(view: View, percent: Float) {
            val lp = view.layoutParams as ConstraintLayout.LayoutParams
            lp.matchConstraintPercentWidth = percent.coerceIn(0f, 1f)
            view.layoutParams = lp
            view.requestLayout()
        }

        private fun setGuidelinePercent(guideline: View, percent: Float) {
            val lp = guideline.layoutParams as ConstraintLayout.LayoutParams
            lp.guidePercent = percent.coerceIn(0f, 1f)
            guideline.layoutParams = lp
            guideline.requestLayout()
        }
    }

    companion object {
        private const val VIEW_TYPE_EXCHANGE = 0
        private const val VIEW_TYPE_NONE = 1

        private fun mapCategoryToKo(raw: String?): String {
            val key = raw?.trim().orEmpty()
            return when (key) {
                "ECON_BIZ" -> "경제/경영"
                "SCI_IT" -> "과학/IT"
                "NOVEL_GENRE" -> "소설"
                "POEM_ESSAY" -> "시/에세이"
                "HOME_HOBBY" -> "가정/취미"
                "ART_CULTURE" -> "예술/문화"
                "HUMAN_HISTORY" -> "인문/역사"
                "SELF_DEV" -> "자기계발"
                "POL_SOC" -> "정치/사회"
                "ETC" -> "기타"
                else -> {
                    key
                }
            }
        }

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
