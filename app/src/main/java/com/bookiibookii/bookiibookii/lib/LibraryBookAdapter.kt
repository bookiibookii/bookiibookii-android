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
            binding.libItemProfileTv.text = item.author // 기획에 따라 작성자/호스트 이름 변경

            // 표지 이미지
            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.color.grey_300)
                .error(R.color.grey_300)
                .into(binding.libItemBookIv)

            // 호스트 프로필
            Glide.with(itemView.context)
                .load(item.hostProfileUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_circle_gray500)
                .error(R.drawable.bg_circle_gray500)
                .into(binding.libItemProfileIv)

            // 상태 표시
            when (item.readStatus) {
                ReadStatus.READING -> {
                    binding.libItemStateTv.visibility = View.VISIBLE
                    binding.libRateList.visibility = View.GONE
                    binding.libItemStateTv.text = item.progress ?: "독서 중"
                }
                ReadStatus.DONE -> {
                    binding.libItemStateTv.visibility = View.GONE
                    binding.libRateList.visibility = View.VISIBLE
                    setRatingStars(binding.libRateList, item.rating)
                }
            }

            itemView.setOnClickListener { itemClickListener(item) }
        }

        private fun setRatingStars(starContainer: LinearLayout, rating: Double) {
            val ratingInt = rating.toInt()
            for (i in 0 until starContainer.childCount) {
                val starView = starContainer.getChildAt(i) as? ImageView ?: continue
                if (i < ratingInt) {
                    starView.setImageResource(R.drawable.ic_star_filled)
                } else {
                    starView.setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }

    // [2] 책등 모드 뷰홀더
    inner class SpineViewHolder(private val binding: ItemLibBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook, position: Int) {
            binding.itemBookGridTv.text = item.title

            // 동적 높이 계산
            val titleLength = item.title.length
            val calculatedHeight = (titleLength * 15) + 60
            val finalHeightDp = calculatedHeight.coerceIn(120, 260)

            val spineParams = binding.spineContainer.layoutParams
            spineParams.height = dpToPx(binding.root.context, finalHeightDp)
            binding.spineContainer.layoutParams = spineParams

            // 배경색 (짝/홀 다르게)
            val colors = listOf(R.color.ui_main_105, R.color.ui_main_sub_pale)
            binding.spineContainer.background.setTint(
                ContextCompat.getColor(binding.root.context, colors[position % colors.size])
            )

            itemView.setOnClickListener { itemClickListener(item) }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}