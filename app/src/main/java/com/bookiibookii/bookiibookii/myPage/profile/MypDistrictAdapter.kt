package com.bookiibookii.bookiibookii.myPage.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ItemMypDistrictBinding

class MypDistrictAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<MypDistrictAdapter.DistrictViewHolder>() {

    private var districtList: List<String> = emptyList()
    private var selectedPosition = -1 // 선택된 아이템 위치

    fun submitList(list: List<String>) {
        districtList = list
        selectedPosition = -1 // 리스트가 바뀌면 선택 초기화
        notifyDataSetChanged()
    }

    inner class DistrictViewHolder(val binding: ItemMypDistrictBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String, position: Int) {
            binding.tvDistrictName.text = name

            val context = binding.root.context

            // [선택 상태 UI 처리]
            if (position == selectedPosition) {

                binding.tvDistrictName.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke)
                binding.tvDistrictName.setTextColor(ContextCompat.getColor(context, R.color.pre_main))
            } else {
                // 선택 안됨: 흰색/회색 배경 + 검은 글씨
                binding.tvDistrictName.setBackgroundResource(R.drawable.bg_input_round_20dp_white)
                binding.tvDistrictName.setTextColor(ContextCompat.getColor(context, R.color.grey_900))
            }

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = position
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onClick(name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistrictViewHolder {
        val binding = ItemMypDistrictBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DistrictViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DistrictViewHolder, position: Int) {
        holder.bind(districtList[position], position)
    }

    override fun getItemCount(): Int = districtList.size
}