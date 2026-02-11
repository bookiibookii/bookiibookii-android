package com.bookiibookii.bookiibookii.home.notification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.KeywordItemDto

class KeywordAdapter(
    private val onDeleteClick: (KeywordItemDto) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.VH>() {

    private val items = mutableListOf<KeywordItemDto>()

    fun submitList(list: List<KeywordItemDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keyword_setting, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvKeyword: TextView = itemView.findViewById(R.id.tv_keyword)
        private val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)

        fun bind(item: KeywordItemDto) {
            tvKeyword.text = item.content
            ivDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}