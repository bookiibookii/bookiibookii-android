package com.bookiibookii.bookiibookii.myPage.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.ItemMypDistrictBinding

class MypDistrictAdapter  : RecyclerView.Adapter<MypDistrictAdapter.DistrictViewHolder>() {

    private var districtList: List<String> = emptyList()

    fun submitList(list: List<String>) {
        districtList = list
        notifyDataSetChanged()
    }

    // ViewHolder에 Binding 전달
    inner class DistrictViewHolder(val binding: ItemMypDistrictBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String) {
            binding.tvDistrictName.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistrictViewHolder {
        val binding = ItemMypDistrictBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DistrictViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DistrictViewHolder, position: Int) {
        holder.bind(districtList[position])
    }

    override fun getItemCount(): Int = districtList.size
}