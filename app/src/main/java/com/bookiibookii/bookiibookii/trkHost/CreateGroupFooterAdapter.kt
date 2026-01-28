package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class CreateGroupFooterAdapter(
    private val mode: FooterMode,
    private val onActionClick: () -> Unit
) : RecyclerView.Adapter<CreateGroupFooterAdapter.VH>() {

    private var showEmptyText: Boolean = true

    fun setShowEmptyText(show: Boolean) {
        showEmptyText = show
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trk_create_group_footer, parent, false)
        return VH(view, mode, onActionClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(showEmptyText)
    }

    override fun getItemCount(): Int = 1

    class VH(
        itemView: View,
        private val mode: FooterMode,
        onActionClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val card: View = itemView.findViewById(R.id.card_create_group)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_empty_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_empty_desc)
        private val btnAction: MaterialButton =
            itemView.findViewById(R.id.btn_create_group)

        init {
            btnAction.setOnClickListener { onActionClick() }
        }

        fun bind(showEmptyText: Boolean) {
            card.visibility = View.VISIBLE
            btnAction.visibility = View.VISIBLE

            tvTitle.visibility = if (showEmptyText) View.VISIBLE else View.GONE

            when (mode) {
                FooterMode.HOST_CREATE -> {
                    tvTitle.text = "아직 그룹을 만들지 않았어요 😭"
                    tvDesc.text = "읽고 싶은 책을 골라 그룹을 만들어볼까요?"
                    btnAction.text = "그룹 만들기"
                }

                FooterMode.GUEST_JOIN -> {
                    tvTitle.text = "아직 참여한 그룹이 없어요 😭"
                    tvDesc.text = "독서 그룹에 참여하러 가볼까요?"
                    btnAction.text = "그룹 참여하기"
                }
            }
        }
    }
}
