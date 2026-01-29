package com.bookiibookii.bookiibookii.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class GroupRecommendAdapter :
    ListAdapter<Any, GroupRecommendAdapter.ViewHolder>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_group_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: API 연결 시 처리
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private val diff = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Any, newItem: Any) = oldItem == newItem
        }
    }
}