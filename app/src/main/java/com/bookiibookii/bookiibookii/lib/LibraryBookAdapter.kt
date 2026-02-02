package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
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

    fun toggleMode() {
        isSpineMode = !isSpineMode
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSpineMode) TYPE_SPINE else TYPE_COVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_COVER) {
            val binding = ItemLibBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CoverViewHolder(binding)
        } else {
            val binding = ItemLibBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SpineViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is CoverViewHolder) {
            holder.bind(item)
        } else if (holder is SpineViewHolder) {
            holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<LibBook>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class CoverViewHolder(private val binding: ItemLibBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook) {
            binding.libItemBookTitleTv.text = item.title
            binding.libItemProfileTv.text = item.author

            // (이미지 로드 및 상태 표시 로직은 기존 코드 유지)
            when (item.readStatus) {
                ReadStatus.READING -> {
                    binding.libItemStateTv.visibility = View.VISIBLE
                    binding.libRateList.visibility = View.GONE
                    binding.libItemStateTv.text = item.progress ?: "독서 중"
                }
                ReadStatus.DONE -> {
                    binding.libItemStateTv.visibility = View.GONE
                    binding.libRateList.visibility = View.VISIBLE
                }
            }
            itemView.setOnClickListener { itemClickListener(item) }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt() // .toInt() 추가
    }

    inner class SpineViewHolder(private val binding: ItemLibBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBook, position: Int) {
            binding.itemBookGridTv.text = item.title

            // 1. 글자 수에 따른 책등 높이 계산
            val titleLength = item.title.length
            val calculatedHeight = (titleLength * 15) + 60 // 15는 폰트 크기에 따른 배율
            val finalHeightDp = calculatedHeight.coerceIn(120, 260)

            // 2. 🔹 핵심: root가 아닌 'spine_container'의 높이만 변경
            val spineParams = binding.spineContainer.layoutParams
            spineParams.height = dpToPx(binding.root.context, finalHeightDp)
            binding.spineContainer.layoutParams = spineParams

            // 3. 배경색 (기존 로직)
            val colors = listOf(R.color.ui_main_105, R.color.ui_main_sub_pale)
            binding.spineContainer.background.setTint(
                ContextCompat.getColor(binding.root.context, colors[position % colors.size])
            )

            itemView.setOnClickListener { itemClickListener(item) }
        }


    }}