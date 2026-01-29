package com.bookiibookii.bookiibookii.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.ItemGroupJoinManagementBinding
import com.bookiibookii.bookiibookii.R

class GroupJoinAdapter(
    private var dataList: MutableList<GroupJoinData>,
    private val onItemClick: (GroupJoinData, Boolean) -> Unit
) : RecyclerView.Adapter<GroupJoinAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemGroupJoinManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    inner class Holder(private val binding: ItemGroupJoinManagementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupJoinData) {
            with(binding) {
                //기본 텍스트 및 이미지 설정
                itemGrpJoinManageNicknameTv.text = item.nickname
                itemGrpJoinManageDateTv.text = item.date
                itemGrpJoinManageContentTv.text = item.intro

                // 프로필 이미지
                if (item.profileResId != null) {
                    itemGrpJoinManageProfileIv.setImageResource(item.profileResId)
                } else {
                    itemGrpJoinManageProfileIv.setImageResource(R.drawable.ic_profile)
                }

                // 칩 처리 로직
                val chipViews = listOf(
                    grpItemJoinMgHash1Cp,
                    grpItemJoinMgHash2Cp,
                    grpItemJoinMgHash3Cp,
                    grpItemJoinMgHash4Cp,
                    grpItemJoinMgHash5Cp
                )

                chipViews.forEachIndexed { index, chipView ->
                    if (index < item.tags.size) {
                        // 데이터가 있는 경우 -> 텍스트 설정하고 보이게 함
                        chipView.text = item.tags[index]
                        chipView.visibility = View.VISIBLE
                    } else {
                        // 데이터가 없는 슬롯: 숨김
                        chipView.visibility = View.GONE
                    }
                }

                // 버튼 클릭 리스너
                itemGrpJoinManageYesBtn.setOnClickListener {
                    onItemClick(item, true)
                }

                itemGrpJoinManageNoBtn.setOnClickListener {
                    onItemClick(item, false)
                }
            }
        }
    }

    fun updateData(newItemList: List<GroupJoinData>) {
        dataList.clear()
        dataList.addAll(newItemList)
        notifyDataSetChanged()
    }

    fun removeItem(item: GroupJoinData) {
        val position = dataList.indexOf(item)
        if (position != -1) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size)
        }
    }
}