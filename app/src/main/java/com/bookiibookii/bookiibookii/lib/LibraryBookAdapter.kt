package com.bookiibookii.bookiibookii.lib

import android.content.Context
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
        notifyDataSetChanged()
    }

    // 데이터 갱신
    fun submitList(newItems: List<LibBook>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    // 뷰 타입 결정
    override fun getItemViewType(position: Int): Int {
        return if (isSpineMode) TYPE_SPINE else TYPE_COVER
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_COVER) {
            val binding = ItemLibBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CoverViewHolder(binding)
        } else {
            val binding = ItemLibBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SpineViewHolder(binding)
        }
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is CoverViewHolder) {
            holder.bind(item)
        } else if (holder is SpineViewHolder) {
            holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    // ==========================================
    // [1] 표지 모드 (CoverViewHolder)
    // ==========================================
    inner class CoverViewHolder(private val binding: ItemLibBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook) {
            binding.libItemBookTitleTv.text = item.title
            binding.libItemProfileTv.text = item.author

            // 1. 책 표지 이미지 로드
            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.color.grey_300) // 로딩 중 색상
                .error(R.color.grey_300)       // 에러 시 색상
                .into(binding.libItemBookIv)

            // 2. 호스트 프로필 이미지 로드 (원형)
            Glide.with(itemView.context)
                .load(item.hostProfileUrl)
                .circleCrop() // ★ 원형으로 자르기
                .placeholder(R.drawable.bg_circle_gray500)
                .error(R.drawable.bg_circle_gray500)
                .into(binding.libItemProfileIv)

            // 3. 상태(읽는중/완독)에 따른 UI 처리
            when (item.readStatus) {
                ReadStatus.READING -> {
                    // 읽는 중: 진행도 표시, 별점 숨김
                    binding.libItemStateTv.visibility = View.VISIBLE
                    binding.libRateList.visibility = View.GONE
                    binding.libItemStateTv.text = item.progress ?: "독서 중"
                }
                ReadStatus.DONE -> {
                    // 완독: 진행도 숨김, 별점 표시
                    binding.libItemStateTv.visibility = View.GONE
                    binding.libRateList.visibility = View.VISIBLE

                    // ★ 별점 채우기 로직 실행
                    setRatingStars(binding.libRateList, item.rating)
                }
            }

            // 아이템 클릭
            itemView.setOnClickListener { itemClickListener(item) }
        }

        // 별점 아이콘 세팅 함수
        private fun setRatingStars(starContainer: LinearLayout, rating: Double) {
            val ratingInt = rating.toInt() // 소수점 버림 (ex: 3.8 -> 3개 채움)

            // XML에 정의된 5개의 별 아이콘을 순회
            for (i in 0 until starContainer.childCount) {
                val starView = starContainer.getChildAt(i) as? ImageView ?: continue

                if (i < ratingInt) {
                    // 평점보다 인덱스가 작으면 '채워진 별'
                    starView.setImageResource(R.drawable.ic_star_filled)
                } else {
                    // 나머지는 '빈 별' (리소스가 없으면 ic_star_filled에 회색 tint를 줘도 됨)
                    starView.setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }

    // ==========================================
    // [2] 책등 모드 (SpineViewHolder)
    // ==========================================
    inner class SpineViewHolder(private val binding: ItemLibBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook, position: Int) {
            binding.itemBookGridTv.text = item.title

            // 1. 글자 수에 따른 책등 높이 계산 (동적 높이)
            val titleLength = item.title.length
            val calculatedHeight = (titleLength * 15) + 60
            // 최소 120dp, 최대 260dp 제한
            val finalHeightDp = calculatedHeight.coerceIn(120, 260)

            // 2. 높이 적용
            val spineParams = binding.spineContainer.layoutParams
            spineParams.height = dpToPx(binding.root.context, finalHeightDp)
            binding.spineContainer.layoutParams = spineParams

            // 3. 배경색 랜덤(순차) 적용
            val colors = listOf(R.color.ui_main_105, R.color.ui_main_sub_pale)
            binding.spineContainer.background.setTint(
                ContextCompat.getColor(binding.root.context, colors[position % colors.size])
            )

            itemView.setOnClickListener { itemClickListener(item) }
        }
    }

    // dp -> px 변환 유틸 함수
    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}