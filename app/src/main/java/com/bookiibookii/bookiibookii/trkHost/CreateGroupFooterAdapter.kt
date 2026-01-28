package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class CreateGroupFooterAdapter(
    private val onCreateGroupClick: () -> Unit
) : RecyclerView.Adapter<CreateGroupFooterAdapter.VH>() {

    private var showEmptyText: Boolean = true

    fun setShowEmptyText(show: Boolean) {
        showEmptyText = show
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trk_create_group_footer, parent, false)
        return VH(view, onCreateGroupClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(showEmptyText)
    }

    override fun getItemCount(): Int = 1

    class VH(
        itemView: View,
        onCreateGroupClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvEmptyTitle: TextView = itemView.findViewById(R.id.tv_empty_title)
        private val btnCreate: View = itemView.findViewById(R.id.btn_create_group)

        init {
            btnCreate.setOnClickListener { onCreateGroupClick() }
        }

        fun bind(showEmptyText: Boolean) {
            tvEmptyTitle.visibility = if (showEmptyText) View.VISIBLE else View.GONE
        }
    }
}
