package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.databinding.ItemLibBookBinding
import com.bookiibookii.bookiibookii.databinding.ItemLibBookGridBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class LibraryBookAdapter(
    private var items: List<LibBook>,
    private val itemClickListener: (LibBook) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_COVER = 0
        const val TYPE_SPINE = 1
    }

    var isSpineMode = false

    // 모드 전환 (그리드 <-> 리스트)
    fun toggleMode() {
        isSpineMode = !isSpineMode
        Log.d("LibraryBookAdapter", "모드 변경됨: ${if(isSpineMode) "책등 모드" else "표지 모드"}")
        notifyDataSetChanged()
    }

    // 데이터 갱신
    fun submitList(newItems: List<LibBook>) {
        Log.d("LibraryBookAdapter", "submitList 호출됨. 아이템 개수: ${newItems.size}")
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSpineMode) TYPE_SPINE else TYPE_COVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_COVER) {
            val binding = ItemLibBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            // ★ 중요: XML에서 layout_height="match_parent"면 화면에 하나만 나옵니다.
            // 혹시 몰라 코드에서 wrap_content로 강제하지는 않았으나 XML 확인 필수입니다.
            CoverViewHolder(binding)
        } else {
            val binding = ItemLibBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SpineViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        Log.d("LibraryBookAdapter", "onBindViewHolder: position=$position, title=${item.title}")

        if (holder is CoverViewHolder) {
            holder.bind(item)
        } else if (holder is SpineViewHolder) {
            holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    // [1] 표지 모드 뷰홀더
    inner class CoverViewHolder(private val binding: ItemLibBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook) {
            binding.libItemBookTitleTv.text = item.title
            binding.libItemProfileTv.text = item.hostName

            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.color.grey_300)
                .error(R.drawable.img_profile_default)
                .into(binding.libItemBookIv)

            Glide.with(itemView.context)
                .load(item.hostProfileUrl)
                .transform(CenterCrop(), RoundedCorners(dpToPx(itemView.context, 6)))
                .placeholder(R.drawable.bg_circle_gray500)
                .error(R.drawable.img_profile_default)
                .into(binding.libItemProfileIv)

            // 상태에 따른 UI 처리 (XML Constraints 이용)
            if (item.readStatus == ReadStatus.DONE) {
                binding.libRateList.visibility = View.VISIBLE
                setRatingStars(binding.libRateList, item.rating)
            } else {
                binding.libRateList.visibility = View.GONE
            }

            itemView.setOnClickListener { itemClickListener(item) }
        }

        private fun setRatingStars(starContainer: LinearLayout, rating: Double) {
            val ratingInt = rating.toInt()
            // ★ 수정됨: score -> rating 으로 변경
            val hasHalfStar = (rating - ratingInt) >= 0.5

            for (i in 0 until starContainer.childCount) {
                val starView = starContainer.getChildAt(i) as? ImageView ?: continue
                when {
                    i < ratingInt -> starView.setImageResource(R.drawable.ic_star_filled)
                    i == ratingInt && hasHalfStar -> starView.setImageResource(R.drawable.ic_star_half)
                    else -> starView.setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }    // [2] 책등 모드 뷰홀더
    inner class SpineViewHolder(private val binding: ItemLibBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook, position: Int) {
            binding.itemBookGridTv.text = item.title

            // 1. 책등 동적 높이 계산 (최대 높이를 260 -> 200으로 축소)
            val titleLength = item.title.length
            val calculatedHeight = (titleLength * 15) + 60
            val finalHeightDp = calculatedHeight.coerceIn(120, 200) // 👈 여기를 수정!
            val finalHeightPx = dpToPx(binding.root.context, finalHeightDp)

            // 2. 부모(책등 컨테이너)의 높이 적용
            val spineParams = binding.spineContainer.layoutParams
            spineParams.height = finalHeightPx
            binding.spineContainer.layoutParams = spineParams

            // 3. 텍스트뷰 너비 맞춤
            val tvParams = binding.itemBookGridTv.layoutParams
            tvParams.width = finalHeightPx
            binding.itemBookGridTv.layoutParams = tvParams

            // 4. 배경색 및 글자색 적용
            val bgColorRes = if (item.isMine) R.color.ui_main_105 else R.color.ui_main_sub_pale
            val textColorRes = if (item.isMine) R.color.pre_main else R.color.ui_main_sub

            // 👈 핵심: .mutate()를 꼭 붙여야 모양이 깨지지 않고 둥글기가 유지됩니다.
            binding.spineContainer.background.mutate().setTint(
                ContextCompat.getColor(binding.root.context, bgColorRes)
            )
            binding.itemBookGridTv.setTextColor(
                ContextCompat.getColor(binding.root.context, textColorRes)
            )

            itemView.setOnClickListener { itemClickListener(item) }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}