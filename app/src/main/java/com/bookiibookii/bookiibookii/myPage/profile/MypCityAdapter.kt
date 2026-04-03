package com.bookiibookii.bookiibookii.myPage.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.City
import com.bookiibookii.bookiibookii.databinding.ItemMypCityBinding

class MypCityAdapter(
    private val items: List<City>,
    private val onClick: (City) -> Unit
) : RecyclerView.Adapter<MypCityAdapter.CityViewHolder>() {

    var selectedPosition = 0

    inner class CityViewHolder(val binding: ItemMypCityBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: City, position: Int) {
            binding.cityNameTv.text = city.name

            val isSelected = (position == selectedPosition)
            val context = binding.root.context

            if (isSelected) {
                // [선택 상태] 글씨 주황색, 우측 탭 보이기
                binding.cityNameTv.setTextColor(ContextCompat.getColor(context, R.color.pre_main))
                binding.viewIndicator.visibility = View.VISIBLE
            } else {
                // [비선택 상태] 글씨 검은색, 우측 탭 숨기기
                binding.cityNameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))
                binding.viewIndicator.visibility = View.INVISIBLE
            }

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onClick(city)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemMypCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}